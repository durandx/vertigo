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
package io.vertigo.studio.plugins.mda.search;

import io.vertigo.core.Home;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.studio.mda.Result;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.studio.plugins.mda.FileGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Génération des objets relatifs au module Search. 
 *  
 * @author dchallas
 */
public final class SearchGeneratorPlugin extends AbstractGeneratorPlugin<SearchConfiguration> {
	/** {@inheritDoc}  */
	public SearchConfiguration createConfiguration(final Properties properties) {
		return new SearchConfiguration(properties);
	}

	/** {@inheritDoc}  */
	public void generate(final SearchConfiguration searchConfiguration, final Result result) {
		Assertion.checkNotNull(searchConfiguration);
		Assertion.checkNotNull(result);
		//---------------------------------------------------------------------
		generateDtDefinitions(searchConfiguration, result);
	}

	private void generateDtDefinitions(final SearchConfiguration searchConfiguration, final Result result) {
		for (final IndexDefinition indexDefinition : Home.getDefinitionSpace().getAll(IndexDefinition.class)) {
			generateSchema(searchConfiguration, result, indexDefinition);
		}
	}

	private void generateSchema(final SearchConfiguration searchConfiguration, final Result result, final IndexDefinition indexDefinition) {
		/** Registry */
		final Map<String, Object> mapRoot = new HashMap<>();
		mapRoot.put("indexDefinition", indexDefinition);
		mapRoot.put("indexType", new TemplateMethodIndexType());

		final FileGenerator super2java = getFileGenerator(searchConfiguration, mapRoot, "schema", "solr/" + indexDefinition.getName() + "/conf", ".xml", "schema.ftl");
		super2java.generateFile(result, true);
	}
}
