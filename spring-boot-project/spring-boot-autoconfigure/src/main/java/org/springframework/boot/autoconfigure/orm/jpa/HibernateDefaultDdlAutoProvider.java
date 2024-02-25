/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.orm.jpa;

import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.SchemaManagement;
import org.springframework.boot.jdbc.SchemaManagementProvider;

/**
 * A {@link SchemaManagementProvider} that invokes a configurable number of
 * {@link SchemaManagementProvider} instances for embedded data sources only.
 *
 * @author Stephane Nicoll
 */
class HibernateDefaultDdlAutoProvider implements SchemaManagementProvider {

	private final Iterable<SchemaManagementProvider> providers;

	/**
	 * Constructs a new HibernateDefaultDdlAutoProvider with the specified list of
	 * SchemaManagementProvider instances.
	 * @param providers the list of SchemaManagementProvider instances to be used by this
	 * HibernateDefaultDdlAutoProvider
	 */
	HibernateDefaultDdlAutoProvider(Iterable<SchemaManagementProvider> providers) {
		this.providers = providers;
	}

	/**
	 * Returns the default DDL auto value for the given data source.
	 * @param dataSource the data source to check
	 * @return the default DDL auto value
	 */
	String getDefaultDdlAuto(DataSource dataSource) {
		if (!EmbeddedDatabaseConnection.isEmbedded(dataSource)) {
			return "none";
		}
		SchemaManagement schemaManagement = getSchemaManagement(dataSource);
		if (SchemaManagement.MANAGED.equals(schemaManagement)) {
			return "none";
		}
		return "create-drop";
	}

	/**
	 * Returns the schema management strategy for the given data source.
	 * @param dataSource the data source to get the schema management for
	 * @return the schema management strategy for the given data source
	 */
	@Override
	public SchemaManagement getSchemaManagement(DataSource dataSource) {
		return StreamSupport.stream(this.providers.spliterator(), false)
			.map((provider) -> provider.getSchemaManagement(dataSource))
			.filter(SchemaManagement.MANAGED::equals)
			.findFirst()
			.orElse(SchemaManagement.UNMANAGED);
	}

}
