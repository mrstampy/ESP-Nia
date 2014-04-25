package com.github.mrstampy.esp.nia.dsp;

import java.math.BigDecimal;

import com.github.mrstampy.esp.dsp.EspSignalUtilities;
import com.github.mrstampy.esp.nia.NiaConstants;

import ddf.minim.analysis.HammingWindow;

public class NiaSignalUtilities extends EspSignalUtilities implements NiaConstants {

	private static final BigDecimal SIGNAL_BREADTH = new BigDecimal(HIGHEST_SIGNAL_VAL - LOWEST_SIGNAL_VAL);

	public NiaSignalUtilities() {
		super(new HammingWindow());
	}

	@Override
	protected int getFFTSize() {
		return FFT_SIZE;
	}

	@Override
	protected double getSampleRate() {
		return SAMPLE_RATE;
	}

	@Override
	protected BigDecimal getRawSignalBreadth() {
		return SIGNAL_BREADTH;
	}
}
