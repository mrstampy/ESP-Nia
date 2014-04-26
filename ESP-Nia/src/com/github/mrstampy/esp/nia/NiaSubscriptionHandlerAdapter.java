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

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mrstampy.esp.multiconnectionsocket.AbstractSubscriptionHandlerAdapter;
import com.github.mrstampy.esp.nia.subscription.NiaEventType;
import com.github.mrstampy.esp.nia.subscription.NiaSubscriptionRequest;

public class NiaSubscriptionHandlerAdapter extends
		AbstractSubscriptionHandlerAdapter<NiaEventType, MultiConnectNiaSocket, NiaSubscriptionRequest> {

	private static final Logger log = LoggerFactory.getLogger(NiaSubscriptionHandlerAdapter.class);

	public NiaSubscriptionHandlerAdapter(MultiConnectNiaSocket socket) {
		super(socket);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		if (message instanceof NiaSubscriptionRequest) {
			subscribe(session, (NiaSubscriptionRequest) message);
		} else {
			log.error("Cannot process message {}", message);
		}
	}

}
