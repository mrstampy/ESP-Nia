/*
 * ESP-Nia Copyright (C) 2014 Burton Alexander
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 */
package com.github.mrstampy.esp.nia;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.usb.UsbClaimException;
import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbPipe;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;

import javolution.util.FastList;

import org.apache.mina.core.service.IoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Scheduler;
import rx.Scheduler.Inner;
import rx.Scheduler.Recurse;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import com.github.mrstampy.esp.multiconnectionsocket.AbstractMultiConnectionSocket;
import com.github.mrstampy.esp.multiconnectionsocket.ConnectionEvent.State;
import com.github.mrstampy.esp.multiconnectionsocket.EspChannel;
import com.github.mrstampy.esp.multiconnectionsocket.MultiConnectionSocketException;
import com.github.mrstampy.esp.nia.subscription.NiaEvent;
import com.github.mrstampy.esp.nia.subscription.NiaEventListener;

/**
 * OCZ Nia implementation of the {@link AbstractMultiConnectionSocket}.
 * 
 * @author burton
 * 
 */
public class MultiConnectNiaSocket extends AbstractMultiConnectionSocket<byte[]> implements NiaConstants {
	private static final Logger log = LoggerFactory.getLogger(MultiConnectNiaSocket.class);
	private static final int MAX_NUM_OUTSTANDING = 10;

	private UsbInterface usbInterface;
	private UsbPipe niaPipe;

	private SampleBuffer sampleBuffer = new SampleBuffer();

	private Scheduler scheduler = Schedulers.executor(Executors.newScheduledThreadPool(5));
	private Subscription subscription;

	private volatile boolean connected;

	private List<NiaEventListener> listeners = new FastList<NiaEventListener>();

	private NiaSubscriptionHandlerAdapter subscriptionHandlerAdapter;

	private AtomicInteger numOutstanding = new AtomicInteger();

	private EspChannel niaChannel = new EspChannel(1, "OCZ NIA Signal");

	public MultiConnectNiaSocket() throws IOException {
		this(false);
	}

	public MultiConnectNiaSocket(boolean broadcasting) throws IOException {
		super(broadcasting);

		addConnectionEventListener(sampleBuffer);
	}

	public void addListener(NiaEventListener l) {
		if (l != null && !listeners.contains(l)) listeners.add(l);
	}

	public void removeListener(NiaEventListener l) {
		if (l != null) listeners.remove(l);
	}

	public void clearListeners() {
		listeners.clear();
	}

	/**
	 * When invoked the tuning functionality of the {@link SampleBuffer} will be
	 * activated. The tuning process takes ~ 10 seconds, during which the number
	 * of samples will be counted and used to resize the buffer to more closely
	 * represent 1 seconds' worth of data.
	 */
	public void tune() {
		if (!isConnected()) {
			log.warn("Must be connected to the Nia to tune");
			return;
		}

		log.info("Tuning sample buffer");

		sampleBuffer.tune();

		scheduler.schedule(new Action1<Scheduler.Inner>() {

			@Override
			public void call(Inner t1) {
				sampleBuffer.stopTuning();
			}
		}, 10, TimeUnit.SECONDS);
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public int getNumChannels() {
		return 1;
	}

	@Override
	public List<EspChannel> getChannels() {
		return Collections.nCopies(1, niaChannel);
	}

	@Override
	public EspChannel getChannel(int channelNumber) {
		return niaChannel;
	}

	@Override
	protected void startImpl() throws MultiConnectionSocketException {
		try {
			niaStart();
		} catch (Exception e) {
			stop();
			throw new MultiConnectionSocketException(e);
		}
	}

	private void niaStart() throws UsbException, UsbClaimException {
		numOutstanding.set(0);
		initDevice();
		usbInterface.claim();
		niaPipe.open();
		connected = true;
		startReadThread();

		NiaDSPValues values = NiaDSPValues.getInstance();
		long pause = 500l;
		long snooze = values.getSampleRateSleepTime();
		TimeUnit tu = values.getSampleRateUnits();

		subscription = scheduler.schedulePeriodically(new Action1<Scheduler.Inner>() {

			@Override
			public void call(Inner t1) {
				processSnapshot(sampleBuffer.getSnapshot());
			}
		}, pause, snooze, tu);
	}

	@Override
	protected void stopImpl() {
		try {
			niaStop();
		} catch (Exception e) {
			log.error("Problem closing NIA", e);
		}
	}

	private void niaStop() throws UsbException, UsbClaimException {
		connected = false;
		if (subscription != null) subscription.unsubscribe();
		if (niaPipe != null && niaPipe.isOpen()) {
			niaPipe.abortAllSubmissions();
			niaPipe.close();
		}
		if (usbInterface != null && usbInterface.isClaimed()) usbInterface.release();
	}

	@Override
	protected IoHandler getHandlerAdapter() {
		subscriptionHandlerAdapter = new NiaSubscriptionHandlerAdapter(this);
		return subscriptionHandlerAdapter;
	}

	@Override
	protected void parseMessage(byte[] message) {
		sampleBuffer.addSample(message);
	}

	private void processSnapshot(double[] snapshot) {
		Observable.just(snapshot).subscribe(new Action1<double[]>() {

			@Override
			public void call(double[] snap) {
				notifyListeners(snap);
				if (canBroadcast()) subscriptionHandlerAdapter.sendMultiConnectionEvent(new NiaEvent(snap));
			}
		});
	}

	private void notifyListeners(double[] snapshot) {
		if (listeners.isEmpty()) return;

		NiaEvent event = new NiaEvent(snapshot);
		for (NiaEventListener l : listeners) {
			l.niaEventPerformed(event);
		}
	}

	private void startReadThread() {
		scheduler.scheduleRecursive(new Action1<Scheduler.Recurse>() {

			@Override
			public void call(Recurse t1) {
				if (!isConnected()) return;

				addBytesToPipe();

				t1.schedule();
			}
		});
	}

	private void addBytesToPipe() {
		try {
			byte[] b = new byte[55];
			niaPipe.asyncSubmit(b);

			int num = numOutstanding.incrementAndGet();
			while (isConnected() && num > MAX_NUM_OUTSTANDING) {
				Thread.sleep(1);
				num = numOutstanding.get();
			}
		} catch (Exception e) {
			log.error("Problem reading from the Nia", e);
		}
	}

	private void initDevice() throws SecurityException, UsbException {
		UsbHub hub = UsbHostManager.getUsbServices().getRootUsbHub();

		UsbDevice nia = findDevice(hub, NIA_VENDOR, NIA_DEVICE);
		if (nia == null) {
			String msg = "No OCZ NIA found. Ensure the device is connected.";
			log.error(msg);
			throw new RuntimeException(msg);
		}

		UsbConfiguration config = nia.getActiveUsbConfiguration();
		usbInterface = config.getUsbInterface((byte) NIA_DEVICE);
		UsbEndpoint ue = usbInterface.getUsbEndpoint((byte) NIA_ENDPOINT_1);
		niaPipe = ue.getUsbPipe();

		niaPipe.addUsbPipeListener(new UsbPipeListener() {

			@Override
			public void errorEventOccurred(UsbPipeErrorEvent event) {
				log.error("Unexpected exception reading NIA", event.getUsbException());
				notifyConnectionEventListeners(State.ERROR_STOPPED);
			}

			@Override
			public void dataEventOccurred(UsbPipeDataEvent event) {
				if (isConnected()) {
					numOutstanding.decrementAndGet();
					publishMessage(event.getData());
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	private UsbDevice findDevice(UsbHub hub, short vendorId, short productId) {
		for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
			UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
			if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
			if (device.isUsbHub()) {
				device = findDevice((UsbHub) device, vendorId, productId);
				if (device != null) return device;
			}
		}

		return null;
	}

}
