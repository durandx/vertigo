package io.vertigo.commons.cache;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.kernel.component.ComponentInitializer;

/**
 * Initialisation du manager des caches.
 * @author dchallas
 * @version $Id: CacheManagerInitializer.java,v 1.2 2013/10/22 10:46:21 pchretien Exp $
 */
public final class CacheManagerInitializer implements ComponentInitializer<CacheManager> {
	public static final String CONTEXT = "testCacheManager";

	/** {@inheritDoc} */
	public void init(final CacheManager manager) {
		//Param�trage d'un cache sp�cifique au test	
		/** Parametre du cache, pour une config ou il est multi-session*/
		final int maxElementsInMemory = 5000;
		final long timeToLiveSeconds = 10; //long�vit� d�un �l�ment
		final long timeToIdleSeconds = 10; //long�vit� d�un �l�ment non utilis� 

		manager.addCache("test", CONTEXT, maxElementsInMemory, timeToLiveSeconds, timeToIdleSeconds);
	}
}
