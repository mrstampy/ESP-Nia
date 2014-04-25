package com.github.mrstampy.esp.nia;

import java.util.concurrent.ArrayBlockingQueue;

public class SampleBuffer implements NiaConstants {

	private ArrayBlockingQueue<Double> queue = new ArrayBlockingQueue<Double>(BUFFER_SIZE);

	public void addSample(byte[] buffer) {
		int numSamples = getNumberOfSamples(buffer);

		double[] samples = new double[numSamples];
		for (int b = 0; b < numSamples; b++) {
			samples[b] = getSample(buffer, b);
		}

		addSample(samples);
	}

	public double[] getSnapshot() {
		Double[] snap = queue.toArray(new Double[] {});

		double[] shot = new double[FFT_SIZE];

		double factor = ((double) BUFFER_SIZE) / FFT_SIZE;

		int j = 0;
		for (double i = 0; i < snap.length; i += factor) {
			shot[j] = snap[(int) i];
			j++;
		}

		return shot;
	}

	public void clear() {
		queue.clear();
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

	private void addSample(double[] sample) {
		for (int i = 0; i < sample.length; i++) {
			if (queue.remainingCapacity() == 0) queue.remove();

			queue.add(sample[i]);
		}
	}
}
