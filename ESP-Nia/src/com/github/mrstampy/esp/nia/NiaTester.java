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

import com.github.mrstampy.esp.multiconnectionsocket.AbstractSocketConnector;
import com.github.mrstampy.esp.nia.dsp.NiaSignalAggregator;
import com.github.mrstampy.esp.nia.subscription.NiaSocketConnector;

/**
 * Main class to demonstrate local and remote notifications from the
 * {@link MultiConnectNiaSocket}
 * 
 * @author burton
 * 
 */
public class NiaTester {

	/**
	 * Demonstrates local raw data acquisition from the Nia.
	 * 
	 * @throws Exception
	 */
	protected static void testLocalAggregation() throws Exception {
		System.out.println("Local Aggregation");
		MultiConnectNiaSocket niaSocket = new MultiConnectNiaSocket();

		NiaSignalAggregator aggregator = new NiaSignalAggregator();
		niaSocket.addListener(aggregator);

		niaSocket.start();

		printSampleLengths(aggregator);
	}

	/**
	 * Connects to the {@link MultiConnectNiaSocket} on the default port
	 * (12345) to receive raw data events.
	 * 
	 * @throws Exception
	 * @see {@link AbstractSocketConnector#SOCKET_BROADCASTER_KEY}
	 */
	protected static void testRemoteAggregation() throws Exception {
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

	private static void printSampleLengths(NiaSignalAggregator aggregator) throws InterruptedException {
		while (true) {
			Thread.sleep(1000);
			double[][] sampled = aggregator.getCurrentSecondOfSampledData();

			int length = sampled.length;
			if (length > 0) {
				System.out.println(length); // should be mostly 100
				System.out.println(sampled[0].length); // should be == 512
			}
		}
	}

	/**
	 * No args == {@link #testLocalAggregation()}, any args == {@link #testRemoteAggregation()}
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			testLocalAggregation();
		} else {
			testRemoteAggregation();
		}
	}

}
