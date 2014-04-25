package com.github.mrstampy.esp.nia;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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

import com.github.mrstampy.esp.mutliconnectionsocket.AbstractMultiConnectionSocket;
import com.github.mrstampy.esp.mutliconnectionsocket.MultiConnectionSocketException;
import com.github.mrstampy.esp.nia.subscription.NiaEvent;
import com.github.mrstampy.esp.nia.subscription.NiaEventListener;

public class MultiConnectNiaSocket extends AbstractMultiConnectionSocket<byte[]> implements NiaConstants {
	private static final Logger log = LoggerFactory.getLogger(MultiConnectNiaSocket.class);

	private UsbInterface usbInterface;
	private UsbPipe niaPipe;
	private NiaReader reader;

	private SampleBuffer sampleBuffer = new SampleBuffer();

	private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
	private ScheduledFuture<?> future;

	private volatile boolean connected;

	private List<NiaEventListener> listeners = new FastList<NiaEventListener>();

	private NiaSubscriptionHandlerAdapter subscriptionHandlerAdapter;

	private AtomicInteger numOutstanding = new AtomicInteger();

	public MultiConnectNiaSocket() throws IOException {
		this(false);
	}

	public MultiConnectNiaSocket(boolean broadcasting) throws IOException {
		super(broadcasting);
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

	@Override
	public boolean isConnected() {
		return connected;
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
		future = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				processSnapshot(sampleBuffer.getSnapshot());
			}
		}, (long) SAMPLE_RATE * 2, (long) SAMPLE_SLEEP, TimeUnit.MILLISECONDS);
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
		if (future != null) future.cancel(true);
		if (niaPipe != null && niaPipe.isOpen()) niaPipe.close();
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
		notifyListeners(snapshot);
		if (canBroadcast()) subscriptionHandlerAdapter.sendMultiConnectionEvent(new NiaEvent(snapshot));
	}

	private void notifyListeners(double[] snapshot) {
		if (listeners.isEmpty()) return;

		NiaEvent event = new NiaEvent(snapshot);
		for (NiaEventListener l : listeners) {
			l.niaEventPerformed(event);
		}
	}

	private void startReadThread() {
		reader = new NiaReader();
		reader.start();
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

	private class NiaReader extends Thread {

		private static final int MAX_NUM_OUTSTANDING = 10;

		public void run() {
			byte[] buf;
			while (isConnected()) {
				try {
					buf = new byte[55];
					niaPipe.asyncSubmit(buf);
					checkOutstanding();
				} catch (Exception e) {
					log.error("Problem reading Nia data", e);
					break;
				}
			}
		}

		private void checkOutstanding() throws InterruptedException {
			int num = numOutstanding.incrementAndGet();
			while (isConnected() && num > MAX_NUM_OUTSTANDING) {
				Thread.sleep(2);
				num = numOutstanding.get();
			}
		}
	}

}
