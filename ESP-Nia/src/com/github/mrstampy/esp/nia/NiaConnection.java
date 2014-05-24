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

import java.io.IOException;

import com.github.mrstampy.esp.dsp.AbstractDSPValues;
import com.github.mrstampy.esp.dsp.EspSignalUtilities;
import com.github.mrstampy.esp.dsp.lab.AbstractRawEspConnection;
import com.github.mrstampy.esp.dsp.lab.RawEspConnection;
import com.github.mrstampy.esp.multiconnectionsocket.MultiConnectionSocketException;
import com.github.mrstampy.esp.nia.dsp.NiaSignalAggregator;
import com.github.mrstampy.esp.nia.dsp.NiaSignalUtilities;

/**
 * {@link RawEspConnection} implementation for the Nia
 * 
 * @author burton
 *
 */
public class NiaConnection extends AbstractRawEspConnection<MultiConnectNiaSocket> {

	private MultiConnectNiaSocket nia;

	private NiaSignalAggregator aggregator = new NiaSignalAggregator();
	private NiaSignalUtilities utilities = new NiaSignalUtilities();

	public NiaConnection() throws IOException {
		this(false);
	}

	public NiaConnection(boolean broadcast) throws IOException {
		super();
		nia = new MultiConnectNiaSocket(broadcast);
	}

	public void start() throws MultiConnectionSocketException {
		getSocket().addListener(aggregator);

		super.start();
	}

	public void stop() {
		try {
			super.stop();
		} finally {
			getSocket().removeListener(aggregator);
		}
	}

	@Override
	public MultiConnectNiaSocket getSocket() {
		return nia;
	}

	@Override
	public EspSignalUtilities getUtilities() {
		return utilities;
	}

	@Override
	public AbstractDSPValues getDSPValues() {
		return NiaDSPValues.getInstance();
	}

	@Override
	public double[][] getCurrent() {
		return aggregator.getCurrentSecondOfSampledData();
	}

	@Override
	public double[][] getCurrent(int numSamples) {
		return aggregator.getCurrentSecondOfSampledData(numSamples);
	}

	@Override
	public String getName() {
		return "ESP Nia";
	}

	@Override
	public double[][] getCurrentFor(int channelNumber) {
		return getCurrent();
	}

	@Override
	public double[][] getCurrentFor(int numSamples, int channelNumber) {
		return getCurrent(numSamples);
	}

}
