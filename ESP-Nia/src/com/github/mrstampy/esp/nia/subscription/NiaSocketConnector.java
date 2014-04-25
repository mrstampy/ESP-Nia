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
package com.github.mrstampy.esp.nia.subscription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.mrstampy.esp.mutliconnectionsocket.AbstractSocketConnector;
import com.github.mrstampy.esp.mutliconnectionsocket.event.AbstractMultiConnectionEvent;

/**
 * {@link AbstractSocketConnector} implementation for the OCZ Nia.
 * 
 * @author burton
 * 
 */
public class NiaSocketConnector extends AbstractSocketConnector<NiaEventType> {

	private List<NiaEventListener> listeners = Collections.synchronizedList(new ArrayList<NiaEventListener>());

	public NiaSocketConnector(String socketBroadcasterHost) {
		super(socketBroadcasterHost);
	}

	public void addListener(NiaEventListener l) {
		if (l != null && !listeners.contains(l)) listeners.add(l);
	}

	public void removeListener(NiaEventListener l) {
		if (l != null) listeners.remove(l);
	}

	public void clearListeners() {
		listeners.clear();
	}

	public boolean subscribe() {
		return subscribe(new NiaSubscriptionRequest());
	}

	@Override
	public boolean subscribeAll() {
		return subscribe();
	}

	@Override
	protected void processEvent(AbstractMultiConnectionEvent<NiaEventType> message) {
		NiaEvent event = (NiaEvent) message;

		for (NiaEventListener l : listeners) {
			l.niaEventPerformed(event);
		}
	}

}
