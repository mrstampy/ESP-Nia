package com.github.mrstampy.esp.nia.subscription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.mrstampy.esp.mutliconnectionsocket.AbstractSocketConnector;
import com.github.mrstampy.esp.mutliconnectionsocket.event.AbstractMultiConnectionEvent;

public class NiaSocketConnector extends AbstractSocketConnector<NiaEventType> {
	
	private List<NiaEventListener> listeners = Collections.synchronizedList(new ArrayList<NiaEventListener>());

	public NiaSocketConnector(String socketBroadcasterHost) {
		super(socketBroadcasterHost);
	}
	
	public void addListener(NiaEventListener l) {
		if(l != null && !listeners.contains(l)) listeners.add(l);
	}
	
	public void removeListener(NiaEventListener l) {
		if(l != null) listeners.remove(l);
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
		NiaEvent event = (NiaEvent)message;

		for(NiaEventListener l : listeners) {
			l.niaEventPerformed(event);
		}
	}

}
