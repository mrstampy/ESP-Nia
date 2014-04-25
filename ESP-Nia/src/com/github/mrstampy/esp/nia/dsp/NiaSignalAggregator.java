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
