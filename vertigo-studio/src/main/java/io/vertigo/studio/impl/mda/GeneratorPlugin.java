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
package io.vertigo.studio.impl.mda;

import io.vertigo.lang.Plugin;
import io.vertigo.studio.mda.ResultBuilder;
import io.vertigo.studio.plugins.mda.FileConfig;

/**
 * Plugin de génération de fichiers.
 *
 * @author dchallas
 */
public interface GeneratorPlugin extends Plugin {
	/**
	 * Génération d'un fichier à partir d'une source et de paramètres.
	 * @param resultBuilder Builder
	 */
	//fix FileConfig dependency ?
	void generate(final FileConfig fileConfig, final ResultBuilder resultBuilder);
}
