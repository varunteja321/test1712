/*
 * Copyright 2012-2024 the original author or authors.
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

package org.springframework.boot.docker.compose.service.connection.neo4j;

import java.net.URI;

import org.neo4j.driver.AuthToken;

import org.springframework.boot.autoconfigure.neo4j.Neo4jConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

/**
 * {@link DockerComposeConnectionDetailsFactory} to create {@link Neo4jConnectionDetails}
 * for a {@code Neo4j} service.
 *
 * @author Andy Wilkinson
 * @author Scott Frederick
 */
class Neo4jDockerComposeConnectionDetailsFactory extends DockerComposeConnectionDetailsFactory<Neo4jConnectionDetails> {

	private static final String[] NEO4J_CONTAINER_NAMES = { "neo4j", "bitnami/neo4j" };

	/**
	 * Constructs a new Neo4jDockerComposeConnectionDetailsFactory.
	 * @param neo4jContainerNames the names of the Neo4j containers
	 */
	Neo4jDockerComposeConnectionDetailsFactory() {
		super(NEO4J_CONTAINER_NAMES);
	}

	/**
	 * Returns the connection details for a Neo4j Docker Compose connection.
	 * @param source the Docker Compose connection source
	 * @return the Neo4j connection details
	 */
	@Override
	protected Neo4jConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
		return new Neo4jDockerComposeConnectionDetails(source.getRunningService());
	}

	/**
	 * {@link Neo4jConnectionDetails} backed by a {@code Neo4j} {@link RunningService}.
	 */
	static class Neo4jDockerComposeConnectionDetails extends DockerComposeConnectionDetails
			implements Neo4jConnectionDetails {

		private static final int BOLT_PORT = 7687;

		private final AuthToken authToken;

		private final URI uri;

		/**
		 * Constructs a new Neo4jDockerComposeConnectionDetails object with the provided
		 * RunningService.
		 * @param service the RunningService object representing the Neo4j Docker Compose
		 * service
		 */
		Neo4jDockerComposeConnectionDetails(RunningService service) {
			super(service);
			Neo4jEnvironment neo4jEnvironment = new Neo4jEnvironment(service.env());
			this.authToken = neo4jEnvironment.getAuthToken();
			this.uri = URI.create("neo4j://%s:%d".formatted(service.host(), service.ports().get(BOLT_PORT)));
		}

		/**
		 * Returns the URI of the Neo4j Docker Compose connection.
		 * @return the URI of the connection
		 */
		@Override
		public URI getUri() {
			return this.uri;
		}

		/**
		 * Returns the authentication token associated with this
		 * Neo4jDockerComposeConnectionDetails instance.
		 * @return the authentication token
		 */
		@Override
		public AuthToken getAuthToken() {
			return this.authToken;
		}

	}

}
