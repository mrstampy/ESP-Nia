package com.github.mrstampy.esp.nia.subscription;

import java.util.EventListener;

/**
 * Listeners receive {@link NiaEvent}s at a rate of 100Hz
 * 
 * @author burton
 * 
 */
public interface NiaEventListener extends EventListener {

	void niaEventPerformed(NiaEvent event);
}
