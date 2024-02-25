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

package org.springframework.boot.testcontainers.service.connection.otlp;

import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

import org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpTracingConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

/**
 * {@link ContainerConnectionDetailsFactory} to create
 * {@link OtlpTracingConnectionDetails} from a
 * {@link ServiceConnection @ServiceConnection}-annotated {@link GenericContainer} using
 * the {@code "otel/opentelemetry-collector-contrib"} image.
 *
 * @author Eddú Meléndez
 */
class OpenTelemetryTracingContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<Container<?>, OtlpTracingConnectionDetails> {

	/**
	 * Constructs a new instance of the
	 * OpenTelemetryTracingContainerConnectionDetailsFactory class.
	 * @param imageName the name of the Docker image for the OpenTelemetry Collector
	 * Contrib
	 * @param autoConfigurationClass the fully qualified class name of the
	 * OtlpAutoConfiguration class from Spring Boot Actuator
	 */
	OpenTelemetryTracingContainerConnectionDetailsFactory() {
		super("otel/opentelemetry-collector-contrib",
				"org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpAutoConfiguration");
	}

	/**
	 * Returns the OpenTelemetryTracingContainerConnectionDetails for the given
	 * ContainerConnectionSource.
	 * @param source the ContainerConnectionSource from which to retrieve the connection
	 * details
	 * @return the OpenTelemetryTracingContainerConnectionDetails for the given
	 * ContainerConnectionSource
	 */
	@Override
	protected OtlpTracingConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<Container<?>> source) {
		return new OpenTelemetryTracingContainerConnectionDetails(source);
	}

	/**
	 * OpenTelemetryTracingContainerConnectionDetails class.
	 */
	private static final class OpenTelemetryTracingContainerConnectionDetails
			extends ContainerConnectionDetails<Container<?>> implements OtlpTracingConnectionDetails {

		/**
		 * Constructs a new OpenTelemetryTracingContainerConnectionDetails object with the
		 * specified ContainerConnectionSource.
		 * @param source the source of the container connection
		 */
		private OpenTelemetryTracingContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
			super(source);
		}

		/**
		 * Returns the URL for the OpenTelemetry tracing container connection. The URL is
		 * constructed using the host and mapped port of the container.
		 * @return the URL for the OpenTelemetry tracing container connection
		 */
		@Override
		public String getUrl() {
			return "http://%s:%d/v1/traces".formatted(getContainer().getHost(), getContainer().getMappedPort(4318));
		}

	}

}
