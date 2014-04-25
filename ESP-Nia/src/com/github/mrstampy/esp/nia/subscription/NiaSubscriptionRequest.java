package com.github.mrstampy.esp.nia.subscription;

import com.github.mrstampy.esp.mutliconnectionsocket.subscription.MultiConnectionSubscriptionRequest;

public class NiaSubscriptionRequest implements MultiConnectionSubscriptionRequest<NiaEventType> {

	private static final long serialVersionUID = 6054403295795982525L;

	@Override
	public NiaEventType[] getEventTypes() {
		return new NiaEventType[]{NiaEventType.rawSignal};
	}

	@Override
	public boolean containsEventType(NiaEventType eventType) {
		return NiaEventType.rawSignal == eventType;
	}

}
