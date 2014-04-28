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

import com.github.mrstampy.esp.multiconnectionsocket.RawDataSampleBuffer;

/**
 * Buffer for raw Nia data with a capacity of 3906 data points. While the specs
 * for the device state that the sample rate is 4kHz during testing it was found
 * that buffering at that size took 1.05 seconds. 3906 data points brought the
 * time to unity.
 * 
 * @author burton
 * 
 */
public class SampleBuffer extends RawDataSampleBuffer<byte[]> implements NiaConstants {
	
	public SampleBuffer() {
		super(BUFFER_SIZE, NiaDSPValues.getInstance().getSampleSize());
	}

	public void addSample(byte[] buffer) {
		int numSamples = getNumberOfSamples(buffer);

		double[] samples = new double[numSamples];
		for (int b = 0; b < numSamples; b++) {
			samples[b] = getSample(buffer, b);
		}

		addSampleImpl(samples);
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
}
