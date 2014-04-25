package com.github.mrstampy.esp.nia;

import com.github.mrstampy.esp.nia.dsp.NiaSignalAggregator;
import com.github.mrstampy.esp.nia.subscription.NiaSocketConnector;

public class NiaTester {
	
	private static void testLocalAggregation() throws Exception {
		System.out.println("Local Aggregation");
		MultiConnectNiaSocket niaSocket = new MultiConnectNiaSocket();
		
		NiaSignalAggregator aggregator = new NiaSignalAggregator();
		niaSocket.addListener(aggregator);
		
		niaSocket.start();
		
		printSampleLengths(aggregator);
	}

	private static void printSampleLengths(NiaSignalAggregator aggregator) throws InterruptedException {
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
	
	private static void testRemoteAggregation() throws Exception {
		System.out.println("Remote Aggregation");
		MultiConnectNiaSocket niaSocket = new MultiConnectNiaSocket(true);
		
		NiaSocketConnector connector = new NiaSocketConnector("localhost");
		NiaSignalAggregator aggregator = new NiaSignalAggregator();
		connector.addListener(aggregator);
		
		connector.connect();
		connector.subscribe();
		
		niaSocket.start();
		
		printSampleLengths(aggregator);
	}

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			testLocalAggregation();
		} else {
			testRemoteAggregation();
		}
	}

}
