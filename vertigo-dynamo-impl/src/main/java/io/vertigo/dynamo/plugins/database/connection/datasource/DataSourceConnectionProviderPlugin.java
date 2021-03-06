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
package io.vertigo.dynamo.plugins.database.connection.datasource;

import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.vendor.SqlDataBase;
import io.vertigo.dynamo.impl.database.SqlDataBaseManagerImpl;
import io.vertigo.dynamo.plugins.database.connection.AbstractSqlConnectionProviderPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.util.ClassUtil;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * ConnectionProvider permettant la connexion à une datasource Java.
 *
 * @author alauthier
 */
public final class DataSourceConnectionProviderPlugin extends AbstractSqlConnectionProviderPlugin {
	/**
	 * DataSource
	 */
	private final DataSource dataSource;

	/**
	 * Constructeur.
	 * @param name ConnectionProvider's name
	 * @param dataBaseName Nom du type de base de données
	 * @param dataSource URL de la dataSource JNDI
	 */
	@Inject
	public DataSourceConnectionProviderPlugin(@Named("name") final Option<String> name, @Named("classname") final String dataBaseName, @Named("source") final String dataSource) {
		super(name.getOrElse(SqlDataBaseManagerImpl.MAIN_CONNECTION_PROVIDER_NAME), createDataBase(dataBaseName));
		Assertion.checkNotNull(dataSource);
		//-----
		// Initialisation de la source de données
		try {
			final javax.naming.Context context = new javax.naming.InitialContext();
			this.dataSource = (DataSource) context.lookup(dataSource);
		} catch (final NamingException e) {
			throw new RuntimeException("Impossible de récupérer la DataSource", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public SqlConnection obtainConnection() throws SQLException {
		final java.sql.Connection connection = dataSource.getConnection();
		return new SqlConnection(connection, getDataBase(), true);
	}

	private static SqlDataBase createDataBase(final String dataBaseName) {
		return ClassUtil.newInstance(dataBaseName, SqlDataBase.class);
	}
}
