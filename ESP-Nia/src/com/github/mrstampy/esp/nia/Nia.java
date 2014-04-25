package com.github.mrstampy.esp.nia;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

public class Nia {

	public static final short NIA_VENDOR = (short) 0x1234;
	public static final short NIA_DEVICE = (short) 0x0;
	public static final int NIA_ENDPOINT_1 = 0x81;
	public static final int NIA_ENDPOINT_2 = 0x1;
	
	private AtomicInteger numJobs = new AtomicInteger();
	private AtomicInteger cntr = new AtomicInteger();
	
	public void doIt() throws SecurityException, UsbException, InterruptedException {
		UsbHub hub = UsbHostManager.getUsbServices().getRootUsbHub();
		
		UsbDevice nia = findDevice(hub, NIA_VENDOR, NIA_DEVICE);
		
		UsbConfiguration config = nia.getActiveUsbConfiguration();
		UsbInterface ui = config.getUsbInterface((byte)NIA_DEVICE);
		ui.claim();
		UsbEndpoint ue = ui.getUsbEndpoint((byte)NIA_ENDPOINT_1);
		UsbPipe pipe = ue.getUsbPipe();
		pipe.open();
		
		pipe.addUsbPipeListener(new UsbPipeListener() {
			
			@Override
			public void errorEventOccurred(UsbPipeErrorEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void dataEventOccurred(UsbPipeDataEvent event) {
				cntr.addAndGet(event.getData()[54]);
				numJobs.decrementAndGet();
			}
		});
		
		byte[] buf;
		
		long start = System.nanoTime();
		for(int i = 0; i < 5000; i++) {
			buf = new byte[55];
			pipe.syncSubmit(buf);
			int num = numJobs.incrementAndGet();
			while(num > 10) {
				Thread.sleep(2);
				num = numJobs.get();
			}
		}
		long end = System.nanoTime();
		System.out.println("Bong!");
		int num = cntr.get();
		System.out.println(num);
		
		BigDecimal bd = new BigDecimal(end - start);
		System.out.println(bd.doubleValue());
		bd = bd.divide(new BigDecimal(num).multiply(new BigDecimal(1000000)), 10, RoundingMode.HALF_UP);
		System.out.println(bd.doubleValue());
		bd = bd.multiply(new BigDecimal(3910)).divide(new BigDecimal(1000));
		System.out.println(bd.doubleValue());
//		
//		for(int i = 0; i < buf.length; i++) {
//			System.out.print(buf[i]);
//			System.out.print(" ");
//		}
//		
//		System.out.println();
//		
//		System.out.println("Number of samples: " + getNumberOfSamples(buf));
//		
//		double[] sample = processData(buf);
//		
//		for(int i = 0; i < sample.length; i++) {
//			System.out.print(sample[i]);
//			System.out.print(" ");
//		}
	}

	// This method processes the data queued by the read thread
	private double[] processData(byte[] buffer) {
		int numSamples = getNumberOfSamples(buffer);

		double[] samples = new double[numSamples];
		for (int b = 0; b < numSamples; b++) {
			samples[b] = getSample(buffer, b);
		}

		return samples;
	}

	private double getSample(byte[] buffer, int i) {
		// get sample value
		int value = (buffer[i * 3] & 0xFF) | ((buffer[i * 3 + 1] & 0xFF) << 8) | ((buffer[i * 3 + 2] & 0xFF) << 16);

		// according to the HID information of the device, sample are between
		// -8388608 and 8388607
		// meaning the sample has a sign bit and is in two's complement.
		if ((value & 0x800000) != 0) {
			value = ~(value ^ 0x7fffff) + 0x800000;
		}

		return value;
	}

	private int getNumberOfSamples(byte[] data) {
		return data[54];
	}

	@SuppressWarnings("unchecked")
	public UsbDevice findDevice(UsbHub hub, short vendorId, short productId) {
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
	
	public static void main(String...args) throws Exception {
		new Nia().doIt();
	}
}
