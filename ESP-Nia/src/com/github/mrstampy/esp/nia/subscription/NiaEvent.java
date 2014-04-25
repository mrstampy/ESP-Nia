package com.github.mrstampy.esp.nia.subscription;

import com.github.mrstampy.esp.mutliconnectionsocket.event.AbstractMultiConnectionEvent;

public class NiaEvent extends AbstractMultiConnectionEvent<NiaEventType> {
	private static final long serialVersionUID = -3110454737233216652L;
	
	private double[] sample;

	public NiaEvent(double[] sample) {
		super(NiaEventType.rawSignal);
		setSample(sample);
	}

	public double[] getSample() {
		return sample;
	}

	public void setSample(double[] sample) {
		this.sample = sample;
	}

}
