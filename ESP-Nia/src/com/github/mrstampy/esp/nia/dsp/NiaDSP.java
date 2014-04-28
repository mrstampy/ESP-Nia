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
package com.github.mrstampy.esp.nia.dsp;

import java.util.Map;
import java.util.Map.Entry;

import com.github.mrstampy.esp.dsp.EspDSP;
import com.github.mrstampy.esp.dsp.EspSignalUtilities;
import com.github.mrstampy.esp.dsp.RawProcessedListener;
import com.github.mrstampy.esp.dsp.RawSignalAggregator;
import com.github.mrstampy.esp.nia.MultiConnectNiaSocket;
import com.github.mrstampy.esp.nia.NiaConstants;
import com.github.mrstampy.esp.nia.NiaDSPValues;

/**
 * {@link EspDSP} default implementation for the OCZ Nia.
 * 
 * @author burton
 * 
 */
public class NiaDSP extends EspDSP<MultiConnectNiaSocket> implements NiaConstants {

	private NiaSignalAggregator aggregator = new NiaSignalAggregator();
	private NiaSignalUtilities utilities = new NiaSignalUtilities();

	public NiaDSP(MultiConnectNiaSocket socket, double... frequencies) {
		super(socket, NiaDSPValues.getInstance().getSampleRate(), frequencies);

		socket.addListener(aggregator);
	}

	@Override
	protected void destroyImpl() {
		socket.removeListener(aggregator);
	}

	@Override
	protected RawSignalAggregator getAggregator() {
		return aggregator;
	}

	@Override
	protected EspSignalUtilities getUtilities() {
		return utilities;
	}

	/**
	 * Main method to demonstrate {@link NiaDSP} use.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {
		MultiConnectNiaSocket socket = new MultiConnectNiaSocket();

		final NiaDSP dsp = new NiaDSP(socket, 3.6, 5.4, 7.83, 10.4);
		dsp.addProcessedListener(new RawProcessedListener() {

			@Override
			public void signalProcessed() {
				showValues(dsp.getSnapshot());
			}
		});

		socket.start();
	}

	private static void showValues(Map<Double, Double> snapshot) {
		for (Entry<Double, Double> entry : snapshot.entrySet()) {
			System.out.println("Frequency: " + entry.getKey() + ", value: " + entry.getValue());
		}
		System.out.println();
	}

}
