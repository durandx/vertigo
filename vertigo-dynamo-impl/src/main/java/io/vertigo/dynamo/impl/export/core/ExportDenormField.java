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
package io.vertigo.dynamo.impl.export.core;

import io.vertigo.core.lang.MessageText;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.export.ExportField;

/**
 * Définition d'une colonne de type dénormalisation à exporter.
 * On précise la liste et le champs a utiliser comme libellé à afficher à la place de l'id de la liste de l'export.
 *
 * @author pchretien, npiedeloup
 */
final class ExportDenormField extends ExportField {
	private final DtList<?> list;
	private final DtField keyField;
	private final DtField displayField;

	/**
	 * Constructeur.
	 * @param dtField Champ à exporter
	 * @param list Liste de éléments dénormés
	 * @param displayField Champs dénormé
	 */
	ExportDenormField(final DtField dtField, final MessageText label, final DtList<?> list, final DtField displayField) {
		super(dtField, label);
		this.list = list;
		this.keyField = list.getDefinition().getIdField().get();
		this.displayField = displayField;
	}

	/**
	 * @return DtList<?> liste contenant les éléments dénormés.
	 */
	DtList<?> getDenormList() {
		return list;
	}

	/**
	 * @return DtField représentant le display de la liste de dénorm.
	 */
	DtField getDisplayField() {
		return displayField;
	}

	/**
	 * @return DtField représentant la clé de la liste de dénorm.
	 * (par défaut la key du DT)
	 */
	DtField getKeyField() {
		return keyField;
	}

	//--------------------------------------------------------------------------

	//	/**
	//	 * @param keyField représentant la clé de la liste de dénorm. (util seulement si différent de la key du DT)
	//	 */
	//	void setKeyField(final DtField keyField) {
	//		this.keyField = keyField;
	//	}
}