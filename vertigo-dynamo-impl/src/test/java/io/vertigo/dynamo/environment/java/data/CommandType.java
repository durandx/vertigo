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
package io.vertigo.dynamo.environment.java.data;

import io.vertigo.dynamo.domain.model.DtMasterData;
import io.vertigo.dynamo.domain.stereotype.DtDefinition;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données CommandType
 */
@javax.persistence.Entity
@javax.persistence.Table(name = "COMMAND_TYPE")
@DtDefinition
public final class CommandType implements DtMasterData {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long ctyId;
	private String label;

	/**
	 * Champ : PRIMARY_KEY.
	 * Récupère la valeur de la propriété 'id'.
	 * @return Long ctyId <b>Obligatoire</b>
	 */
	@javax.persistence.Id
	@javax.persistence.SequenceGenerator(name = "sequence", sequenceName = "SEQ_COMMAND_TYPE")
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.AUTO, generator = "sequence")
	@javax.persistence.Column(name = "CTY_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "PRIMARY_KEY", required = true, label = "id")
	public Long getCtyId() {
		return ctyId;
	}

	/**
	 * Champ : PRIMARY_KEY.
	 * Définit la valeur de la propriété 'id'.
	 * @param ctyId Long <b>Obligatoire</b>
	 */
	public void setCtyId(final Long ctyId) {
		this.ctyId = ctyId;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Label'.
	 * @return String label <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "LABEL")
	@Field(domain = "DO_FULL_TEXT", required = true, label = "Label")
	public String getLabel() {
		return label;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Label'.
	 * @param label String <b>Obligatoire</b>
	 */
	public void setLabel(final String label) {
		this.label = label;
	}

	// Association : Command non navigable

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
