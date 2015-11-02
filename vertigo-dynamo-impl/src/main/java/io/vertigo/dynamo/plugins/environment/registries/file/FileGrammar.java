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
package io.vertigo.dynamo.plugins.environment.registries.file;

import io.vertigo.core.dsl.entity.Entity;
import io.vertigo.core.dsl.entity.EntityBuilder;
import io.vertigo.core.dsl.entity.EntityGrammar;
import io.vertigo.core.dsl.entity.EntityPropertyType;
import io.vertigo.dynamo.plugins.environment.KspProperty;

/**
 * @author npiedeloup
 */
final class FileGrammar {

	/**Définition de tache.*/
	public static final Entity FILE_INFO_DEFINITION_ENTITY;

	/** File Grammar instance. */
	public static final EntityGrammar GRAMMAR;

	static {
		FILE_INFO_DEFINITION_ENTITY = new EntityBuilder("FileInfo")
				.addField(KspProperty.STORE_NAME, EntityPropertyType.String, true)
				.build();
		GRAMMAR = new EntityGrammar(FILE_INFO_DEFINITION_ENTITY);
	}

	private FileGrammar() {
		//private
	}
}
