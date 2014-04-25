package com.github.mrstampy.esp.nia;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mrstampy.esp.mutliconnectionsocket.AbstractSubscriptionHandlerAdapter;
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
