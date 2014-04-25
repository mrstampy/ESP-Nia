package com.github.mrstampy.esp.nia.subscription;

import com.github.mrstampy.esp.mutliconnectionsocket.event.AbstractMultiConnectionEvent;

/**
 * Represents the current second's worth of samples, 512 data points sampled at
 * a rate of 100Hz
 * 
 * @author burton
 * 
 */
public class NiaEvent extends AbstractMultiConnectionEvent<NiaEventType> {
	private static final long serialVersionUID = -3110454737233216652L;

	private final double[] sample;

	public NiaEvent(double[] sample) {
		super(NiaEventType.rawSignal);
		this.sample = sample;
	}

	public double[] getSample() {
		return sample;
	}

}
