/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.commons.plugins.cache.memory;

import io.vertigo.commons.cache.CacheConfig;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.cache.CachePlugin;
import io.vertigo.core.spaces.component.ComponentInfo;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Describable;
import io.vertigo.lang.Modifiable;
import io.vertigo.lang.Option;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation MapCache du plugins.
 *
 * @author npiedeloup
 */
public final class MemoryCachePlugin implements CachePlugin, Describable {
	private final CodecManager codecManager;
	private final Map<String, List<String>> cacheTypeMap = new LinkedHashMap<>();
	private final Map<String, MemoryCache> cachesPerContext = new HashMap<>();
	private final Set<String> noSerializationContext;

	/**
	 * Constructeur.
	 * @param codecManager Manager des mécanismes de codage/décodage.
	 * @param noSerializationOption Liste optionnelles des noms de context à ne jamais sérialiser
	 */
	@Inject
	public MemoryCachePlugin(final CodecManager codecManager, @Named("noSerialization") final Option<String> noSerializationOption) {
		Assertion.checkNotNull(codecManager);
		//-----
		this.codecManager = codecManager;
		if (noSerializationOption.isDefined()) {
			noSerializationContext = new HashSet<>(Arrays.asList(noSerializationOption.get().split(";")));
		} else {
			noSerializationContext = Collections.emptySet();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void addCache(final String context, final CacheConfig cacheConfig) {
		if (!cachesPerContext.containsKey(context)) {
			final MemoryCache cache = new MemoryCache(context, cacheConfig.isEternal(), cacheConfig.getTimeToLiveSeconds());
			cachesPerContext.put(context, cache);
		}
		registerCacheType(context, cacheConfig.getCacheType());
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String context, final Serializable key, final Object value) {
		Assertion.checkNotNull(value, "CachePlugin can't cache null value. (context: {0}, key:{1})", context, key);
		Assertion.checkState(!(value instanceof byte[]), "Ce CachePlugin ne permet pas de mettre en cache des byte[].");
		//-----
		//Si l'objet est bien marqué non modifiable (ie : interface Modifiable ET !isModifiable)
		//on peut le garder tel quel, sinon on le clone
		//TODO à revoir : les DtObject et DtList ne peuvent plus etre non Modifiable, on ajoute un paramétrage spécifique
		if (isUnmodifiable(value) || noSerializationContext.contains(context)) {
			putElement(context, key, value);
		} else {
			Assertion.checkArgument(value instanceof Serializable, "Object to cache isn't Serializable. Make it unmodifiable or add it in noSerialization's plugin parameter. (context: {0}, key:{1}, class:{2})", context, key, value.getClass().getSimpleName());
			// Sérialisation avec compression
			final byte[] serializedObject = codecManager.getCompressedSerializationCodec().encode((Serializable) value);
			//La sérialisation est équivalente à un deep Clone.
			putElement(context, key, serializedObject);
		}
	}

	private static boolean isUnmodifiable(final Object value) {
		//s'il n'implemente pas Modifiable, il doit être cloné
		//s'il implemente Modifiable et que isModifiable == true, il doit être cloné
		return value instanceof Modifiable && !((Modifiable) value).isModifiable();
	}

	/** {@inheritDoc} */
	@Override
	public Object get(final String context, final Serializable key) {
		final Object cachedObject = getElement(context, key);
		//on ne connait pas l'état Modifiable ou non de l'objet, on se base sur son type.
		if (cachedObject instanceof byte[]) {
			final byte[] serializedObject = (byte[]) cachedObject;
			return codecManager.getCompressedSerializationCodec().decode(serializedObject);
		}
		return cachedObject;
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final String context, final Serializable key) {
		return getMapCache(context).remove(key);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void clearAll() {
		for (final MemoryCache mapCache : cachesPerContext.values()) {
			mapCache.removeAll();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clear(final String context) {
		//Dans le cas de clear
		final MemoryCache mapCache = cachesPerContext.get(context);
		if (mapCache != null) {
			mapCache.removeAll();
		}
	}

	private void putElement(final String context, final Serializable key, final Object value) {
		getMapCache(context).put(key, value);
	}

	private Object getElement(final String context, final Serializable key) {
		return getMapCache(context).get(key);
	}

	private synchronized MemoryCache getMapCache(final String context) {
		final MemoryCache mapCache = cachesPerContext.get(context);
		Assertion.checkNotNull(mapCache, "Le cache {0} n''a pas été crée.", context);
		return mapCache;
	}

	/**
	 * Conserve la liste des caches par type.
	 * Déjà synchronisé depuis le addCache.
	 * @param cacheName Nom du cache
	 * @param cacheType Type du cache
	 */
	private void registerCacheType(final String cacheName, final String cacheType) {
		List<String> cacheNames = cacheTypeMap.get(cacheType);
		if (cacheNames == null) {
			cacheNames = new ArrayList<>();
			cacheTypeMap.put(cacheType, cacheNames);
		}
		cacheNames.add(cacheName);
	}

	/** {@inheritDoc} */
	@Override
	public List<ComponentInfo> getInfos() {
		long hits = 0L;
		long calls = 0L;
		//---
		final List<ComponentInfo> componentInfos = new ArrayList<>();
		for (final String cacheName : cachesPerContext.keySet()) {
			final MemoryCache mapCache = getMapCache(cacheName);
			componentInfos.add(new ComponentInfo("cache." + cacheName + ".elements", mapCache.getElementCount()));
			componentInfos.add(new ComponentInfo("cache." + cacheName + ".hits", mapCache.getHits()));
			componentInfos.add(new ComponentInfo("cache." + cacheName + ".calls", mapCache.getCalls()));
			componentInfos.add(new ComponentInfo("cache." + cacheName + ".ttl", mapCache.getTimeToLiveSeconds()));
			componentInfos.add(new ComponentInfo("cache." + cacheName + ".eternal", mapCache.isEternal()));
			hits += mapCache.getHits();
			calls += mapCache.getCalls();
		}
		final double ratio = 100d * (calls > 0 ? hits / calls : 1);//Par convention 100%
		componentInfos.add(new ComponentInfo("cache.hits", hits));
		componentInfos.add(new ComponentInfo("cache.calls", calls));
		componentInfos.add(new ComponentInfo("cache.ratio", ratio));

		return componentInfos;
	}
}
