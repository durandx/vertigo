package io.vertigo.dynamo.impl.database.statementhandler;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtDefinitionBuilder;
import io.vertigo.lang.Assertion;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DtDefinition serializable.
 * Permet de serialiser une DT qui par nature n'est pas sérialisable.
 * @author pchretien
 */
final class SerializableDtDefinition implements Serializable {
	private static final String DT_DYNAMIC = "DT_DYNAMIC_DTO";
	//Map des domaines correspondants aux types primitifs
	private static final Map<DataType, Domain> DOMAIN_MAP = createDomainMap();

	private static final long serialVersionUID = -423652372994923330L;
	private final SerializableDtField[] fields;
	private transient DtDefinition dtDefinition;

	/**
	 * @param fields Fields
	 */
	SerializableDtDefinition(final SerializableDtField[] fields) {
		Assertion.checkNotNull(fields);
		//-----
		this.fields = fields;
	}

	/**
	 * @return DtDefinition
	 */
	public synchronized DtDefinition getDtDefinition() {
		//synchronizer, car lasy loading
		if (dtDefinition == null) {
			final DtDefinitionBuilder dtDefinitionBuilder = new DtDefinitionBuilder(DT_DYNAMIC)
					.withPersistent(false)
					.withDynamic(true);

			for (final SerializableDtField field : fields) {
				//On considére le champ nullable et non persistent
				dtDefinitionBuilder.addDataField(field.getName(), field.getLabel(), getDomain(field.getDataType()), false, false, false, false);
			}
			dtDefinition = dtDefinitionBuilder.build();
		}
		return dtDefinition;
	}

	private static Map<DataType, Domain> createDomainMap() {
		final DataType[] dataTypes = DataType.values();
		final Map<DataType, Domain> map = new HashMap<>(dataTypes.length);
		//Initialisation de la map.
		for (final DataType dataType : dataTypes) {
			final Domain domain = new Domain("DO_DYN", dataType);
			map.put(dataType, domain);
		}
		return map;
	}

	private static Domain getDomain(final DataType dataType) {
		final Domain domain = DOMAIN_MAP.get(dataType);
		Assertion.checkNotNull(domain);
		return domain;
	}
}
