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

package org.springframework.boot.docker.compose.service.connection.sqlserver;

import io.r2dbc.spi.ConnectionFactoryOptions;

import org.springframework.boot.autoconfigure.r2dbc.R2dbcConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;
import org.springframework.boot.docker.compose.service.connection.r2dbc.ConnectionFactoryOptionsBuilder;

/**
 * {@link DockerComposeConnectionDetailsFactory} to create {@link R2dbcConnectionDetails}
 * for a {@code mssql} service.
 *
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
class SqlServerR2dbcDockerComposeConnectionDetailsFactory
		extends DockerComposeConnectionDetailsFactory<R2dbcConnectionDetails> {

	/**
	 * Constructs a new SqlServerR2dbcDockerComposeConnectionDetailsFactory object.
	 * @param imageName the name of the Docker image for the SQL Server
	 * @param connectionFactoryOptions the options for the R2DBC connection factory
	 */
	SqlServerR2dbcDockerComposeConnectionDetailsFactory() {
		super("mssql/server", "io.r2dbc.spi.ConnectionFactoryOptions");
	}

	/**
	 * Retrieves the connection details for a Docker Compose connection source.
	 * @param source the Docker Compose connection source
	 * @return the R2dbcConnectionDetails for the Docker Compose connection
	 */
	@Override
	protected R2dbcConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
		return new SqlServerR2dbcDockerComposeConnectionDetails(source.getRunningService());
	}

	/**
	 * {@link R2dbcConnectionDetails} backed by a {@code mssql} {@link RunningService}.
	 */
	static class SqlServerR2dbcDockerComposeConnectionDetails extends DockerComposeConnectionDetails
			implements R2dbcConnectionDetails {

		private static final ConnectionFactoryOptionsBuilder connectionFactoryOptionsBuilder = new ConnectionFactoryOptionsBuilder(
				"mssql", 1433);

		private final ConnectionFactoryOptions connectionFactoryOptions;

		/**
		 * Constructs a new SqlServerR2dbcDockerComposeConnectionDetails object with the
		 * provided RunningService.
		 * @param service the RunningService object representing the running service
		 */
		SqlServerR2dbcDockerComposeConnectionDetails(RunningService service) {
			super(service);
			SqlServerEnvironment environment = new SqlServerEnvironment(service.env());
			this.connectionFactoryOptions = connectionFactoryOptionsBuilder.build(service, "",
					environment.getUsername(), environment.getPassword());
		}

		/**
		 * Returns the connection factory options for the
		 * SqlServerR2dbcDockerComposeConnectionDetails.
		 * @return the connection factory options
		 */
		@Override
		public ConnectionFactoryOptions getConnectionFactoryOptions() {
			return this.connectionFactoryOptions;
		}

	}

}
