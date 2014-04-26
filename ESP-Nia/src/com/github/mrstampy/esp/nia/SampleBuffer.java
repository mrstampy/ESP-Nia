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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Buffer for raw Nia data with a capacity of 3910 data points. While the specs
 * for the device state that the sample rate is 4kHz during testing it was found
 * that buffering at that size took 1.05 seconds. 3910 data points brought the
 * time to unity.
 * 
 * @author burton
 * 
 */
public class SampleBuffer implements NiaConstants {
	private static final Logger log = LoggerFactory.getLogger(SampleBuffer.class);

	private int bufferSize = BUFFER_SIZE;
	private ArrayBlockingQueue<Double> queue = new ArrayBlockingQueue<Double>(bufferSize);

	private AtomicInteger totalForTuning = new AtomicInteger();

	private long startTimeTuning;

	private volatile boolean tuning;

	public void addSample(byte[] buffer) {
		int numSamples = getNumberOfSamples(buffer);

		if (tuning) totalForTuning.addAndGet(numSamples);

		double[] samples = new double[numSamples];
		for (int b = 0; b < numSamples; b++) {
			samples[b] = getSample(buffer, b);
		}

		addSample(samples);
	}

	public double[] getSnapshot() {
		Double[] snap = queue.toArray(new Double[] {});

		double[] shot = new double[FFT_SIZE];

		double factor = ((double) getBufferSize()) / FFT_SIZE;

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

	/**
	 * When invoked the number of samples will be counted. When
	 * {@link #stopTuning()} is invoked the time taken to process the total number
	 * of samples taken during that time will be used to resize the sample buffer.
	 */
	public void tune() {
		if (tuning) return;

		totalForTuning.set(0);
		tuning = true;
		startTimeTuning = System.nanoTime();
	}

	/**
	 * Invoked after a period of time after invoking tune(). Will resize the queue
	 * to represent ~ 1 second's worth of data based upon the total number of
	 * samples received during tuning.
	 */
	public void stopTuning() {
		if (!tuning) return;

		tuning = false;

		long diff = System.nanoTime() - startTimeTuning;
		int total = totalForTuning.get();

		BigDecimal seconds = new BigDecimal(diff).divide(new BigDecimal(1000000000), 10, RoundingMode.HALF_UP);

		int newBufSize = new BigDecimal(total).divide(seconds, 3, RoundingMode.HALF_UP).intValue();

		log.info("Resizing buffer to {}", newBufSize);

		setBufferSize(newBufSize);

		synchronized (queue) {
			queue = new ArrayBlockingQueue<Double>(newBufSize);
		}
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

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}
