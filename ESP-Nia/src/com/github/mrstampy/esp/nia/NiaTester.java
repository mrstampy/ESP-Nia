package com.github.mrstampy.esp.nia;

import com.github.mrstampy.esp.nia.dsp.NiaSignalAggregator;

public class NiaTester {

	public static void main(String[] args) throws Exception {
		MultiConnectNiaSocket niaSocket = new MultiConnectNiaSocket();
		
		NiaSignalAggregator aggregator = new NiaSignalAggregator();
		niaSocket.addListener(aggregator);
		
		niaSocket.start();
		
		while(true) {
			Thread.sleep(1000);
			double[][] sampled = aggregator.getCurrentSecondOfSampledData();			

			int length = sampled.length;
			if (length > 0) {
				System.out.println(length); // should be mostly 100
				System.out.println(sampled[0].length); // should be == 512
			}
		}
	}

}
