package com.github.mrstampy.esp.nia.dsp;

import java.util.Map;
import java.util.Map.Entry;

import com.github.mrstampy.esp.dsp.EspDSP;
import com.github.mrstampy.esp.dsp.EspSignalUtilities;
import com.github.mrstampy.esp.dsp.RawProcessedListener;
import com.github.mrstampy.esp.dsp.RawSignalAggregator;
import com.github.mrstampy.esp.nia.MultiConnectNiaSocket;
import com.github.mrstampy.esp.nia.NiaConstants;

public class NiaDSP extends EspDSP<MultiConnectNiaSocket> implements NiaConstants {

	private NiaSignalAggregator aggregator = new NiaSignalAggregator();
	private NiaSignalUtilities utilities = new NiaSignalUtilities();

	public NiaDSP(MultiConnectNiaSocket socket, double... frequencies) {
		super(socket, (int) SAMPLE_RATE, frequencies);

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
	 * @param args
	 * @throws Exception
	 */
	public static void main(String...args) throws Exception {
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
		for(Entry<Double, Double> entry : snapshot.entrySet()) {
			System.out.println("Frequency: " + entry.getKey() + ", value: " + entry.getValue());
		}
		System.out.println();
	}

}
