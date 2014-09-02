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
package io.vertigo.dynamo.impl.kvdatastore;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.kvdatastore.KVDataStoreManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * @author npiedeloup, pchretien
 */
public final class KVDataStoreManagerImpl implements KVDataStoreManager {
	private final Map<String, KVDataStorePlugin> kvDataStorePluginBinding;

	@Inject
	public KVDataStoreManagerImpl(final List<KVDataStorePlugin> kvDataStorePlugins) {
		Assertion.checkNotNull(kvDataStorePlugins);
		//---------------------------------------------------------------------
		final Map<String, KVDataStorePlugin> map = new HashMap<>();
		for (final KVDataStorePlugin kvDataStorePlugin : kvDataStorePlugins) {
			map.put(kvDataStorePlugin.getDataStoreName(), kvDataStorePlugin);
		}
		kvDataStorePluginBinding = Collections.unmodifiableMap(map);
	}

	public void put(final String dataStoreName, final String id, final Object objet) {
		getKVDataStorePlugin(dataStoreName).put(id, objet);
	}

	public void remove(final String dataStoreName, final String id) {
		getKVDataStorePlugin(dataStoreName).remove(id);
	}

	public <C> Option<C> find(final String dataStoreName, final String id, final Class<C> clazz) {
		return getKVDataStorePlugin(dataStoreName).find(id, clazz);
	}

	public <C> List<C> findAll(final String dataStoreName, final int skip, final Integer limit, final Class<C> clazz) {
		return getKVDataStorePlugin(dataStoreName).findAll(skip, limit, clazz);
	}

	private KVDataStorePlugin getKVDataStorePlugin(final String dataStoreName) {
		final KVDataStorePlugin kvDataStorePlugin = kvDataStorePluginBinding.get(dataStoreName);
		//---------------------------------------------------------------------
		Assertion.checkNotNull(kvDataStorePlugin, "No KVDataStorePlugin bind to this name : {0}. Registered dataStoreNames : ({1})", dataStoreName, kvDataStorePluginBinding.keySet());
		//---------------------------------------------------------------------
		return kvDataStorePlugin;
	}

}
