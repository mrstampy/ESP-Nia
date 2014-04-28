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
