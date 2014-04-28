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

import com.github.mrstampy.esp.dsp.AbstractDSPValues;

public class NiaDSPValues extends AbstractDSPValues implements NiaConstants {

	private static final NiaDSPValues instance = new NiaDSPValues();

	public static NiaDSPValues getInstance() {
		return instance;
	}

	private NiaDSPValues() {
		super();
	}

	@Override
	protected void initialize() {
		setSampleRate(SAMPLE_RATE);
		setSampleSize(FFT_SIZE);
	}

	public void setSampleSize(int sampleSize) {
		if (sampleSize > 2048) {
			throw new IllegalArgumentException("Sample size must be <= 2048: " + sampleSize);
		}

		super.setSampleSize(sampleSize);
	}

}
