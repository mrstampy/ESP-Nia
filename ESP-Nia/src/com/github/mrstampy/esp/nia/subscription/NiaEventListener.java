package com.github.mrstampy.esp.nia.subscription;

import java.util.EventListener;

public interface NiaEventListener extends EventListener {

	void niaEventPerformed(NiaEvent event);
}
