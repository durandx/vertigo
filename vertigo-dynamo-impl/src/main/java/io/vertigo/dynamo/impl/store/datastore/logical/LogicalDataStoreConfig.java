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
package io.vertigo.dynamo.impl.store.datastore.logical;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.store.datastore.DataStorePlugin;
import io.vertigo.lang.Assertion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration logique des stores physiques.
 * @author pchretien, npiedeloup
 */
public final class LogicalDataStoreConfig {
	private static final String MAIN_DATA_STORE_NAME = "main";

	/** Map des stores utilisés spécifiquement */
	private final Map<String, DataStorePlugin> dataStoresMap = new HashMap<>();

	/**
	 * @param dataStorePlugins DataStore plugins
	 */
	public LogicalDataStoreConfig(final List<DataStorePlugin> dataStorePlugins) {
		Assertion.checkNotNull(dataStorePlugins);
		//-----
		for (final DataStorePlugin dataStorePlugin : dataStorePlugins) {
			final String name = dataStorePlugin.getName();
			final DataStorePlugin previous = dataStoresMap.put(name, dataStorePlugin);
			Assertion.checkState(previous == null, "DataStorePlugin {0}, was already registered", name);
		}
		Assertion.checkNotNull(dataStoresMap.get(MAIN_DATA_STORE_NAME), "No " + MAIN_DATA_STORE_NAME + " DataStorePlugin was set. Configure one and only one DataStorePlugin with name '" + MAIN_DATA_STORE_NAME + "'.");
	}

	/**
	 * Fournit un store adpaté au type de l'objet.
	 * @param definition Définition
	 * @return Store utilisé pour cette definition
	 */
	public DataStorePlugin getPhysicalDataStore(final DtDefinition definition) {
		Assertion.checkNotNull(definition);
		//-----
		//On regarde si il existe un store enregistré spécifiquement pour cette Definition
		final DataStorePlugin dataStore = dataStoresMap.get(definition.getStoreName().getOrElse(MAIN_DATA_STORE_NAME));
		Assertion.checkNotNull(dataStore, "Aucun store trouvé pour la définition '{0}'", definition.getName());
		return dataStore;
	}
}
