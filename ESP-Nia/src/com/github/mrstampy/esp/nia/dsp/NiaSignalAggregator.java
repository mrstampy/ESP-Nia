package com.github.mrstampy.esp.nia.dsp;

import com.github.mrstampy.esp.dsp.RawSignalAggregator;
import com.github.mrstampy.esp.nia.NiaConstants;
import com.github.mrstampy.esp.nia.subscription.NiaEvent;
import com.github.mrstampy.esp.nia.subscription.NiaEventListener;

public final class NiaSignalAggregator extends RawSignalAggregator implements NiaEventListener, NiaConstants {

	public NiaSignalAggregator() {
		super((int) SAMPLE_RATE);
	}

	public void niaEventPerformed(NiaEvent event) {
		addSample(event.getSample());
	}

}
