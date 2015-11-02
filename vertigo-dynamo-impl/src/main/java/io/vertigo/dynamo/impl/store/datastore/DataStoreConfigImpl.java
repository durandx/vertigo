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
package io.vertigo.dynamo.impl.store.datastore;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.commons.event.EventManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.impl.store.datastore.cache.CacheDataStoreConfig;
import io.vertigo.dynamo.impl.store.datastore.logical.LogicalDataStoreConfig;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.datastore.DataStoreConfig;
import io.vertigo.dynamo.store.datastore.DataStorePlugin;
import io.vertigo.lang.Assertion;

import java.util.List;

/**
 * Implémentation Standard du StoreProvider.
 *
 * @author pchretien
 */
public final class DataStoreConfigImpl implements DataStoreConfig {
	private final CacheDataStoreConfig cacheStoreConfig;
	private final LogicalDataStoreConfig logicalDataStoreConfig;

	private final StoreManager storeManager;
	private final EventManager eventsManager;

	/**
	 * Constructeur.
	 * @param dataStorePlugins DataStorePlugins list
	 * @param cacheManager Manager de gestion du cache
	 * @param storeManager Manager de persistence
	 * @param eventsManager Manager d'events
	 */
	public DataStoreConfigImpl(final List<DataStorePlugin> dataStorePlugins, final CacheManager cacheManager, final StoreManager storeManager, final EventManager eventsManager) {
		Assertion.checkNotNull(dataStorePlugins);
		Assertion.checkNotNull(cacheManager);
		Assertion.checkNotNull(storeManager);
		Assertion.checkNotNull(eventsManager);
		//-----
		this.storeManager = storeManager;
		this.eventsManager = eventsManager;
		cacheStoreConfig = new CacheDataStoreConfig(cacheManager);
		logicalDataStoreConfig = new LogicalDataStoreConfig(dataStorePlugins);
	}

	/**
	 * @return Manager de persistence
	 */
	public StoreManager getStoreManager() {
		return storeManager;
	}

	/**
	 * @return Manager d'events
	 */
	public EventManager getEventsManager() {
		return eventsManager;
	}

	/**
	 * Enregistre si un DT peut être mis en cache et la façon de charger les données.
	 * @param dtDefinition Définition de DT
	 * @param timeToLiveInSeconds Durée de vie du cache
	 * @param isReloadedByList Si ce type d'objet doit être chargé de façon ensembliste ou non
	 */
	@Override
	public void registerCacheable(final DtDefinition dtDefinition, final long timeToLiveInSeconds, final boolean isReloadedByList) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		cacheStoreConfig.registerCacheable(dtDefinition, timeToLiveInSeconds, isReloadedByList);
	}

	public CacheDataStoreConfig getCacheStoreConfig() {
		return cacheStoreConfig;
	}

	public LogicalDataStoreConfig getLogicalStoreConfig() {
		return logicalDataStoreConfig;
	}
}
