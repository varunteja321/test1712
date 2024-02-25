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

package org.springframework.boot.autoconfigure.pulsar;

/**
 * Adapts {@link PulsarProperties} to {@link PulsarConnectionDetails}.
 *
 * @author Chris Bono
 */
class PropertiesPulsarConnectionDetails implements PulsarConnectionDetails {

	private final PulsarProperties pulsarProperties;

	/**
	 * Constructs a new instance of PropertiesPulsarConnectionDetails with the specified
	 * PulsarProperties.
	 * @param pulsarProperties the PulsarProperties object containing the properties for
	 * the Pulsar connection
	 */
	PropertiesPulsarConnectionDetails(PulsarProperties pulsarProperties) {
		this.pulsarProperties = pulsarProperties;
	}

	/**
	 * Returns the broker URL for the Pulsar connection.
	 * @return the broker URL
	 */
	@Override
	public String getBrokerUrl() {
		return this.pulsarProperties.getClient().getServiceUrl();
	}

	/**
	 * Returns the admin URL for the Pulsar connection.
	 * @return the admin URL
	 */
	@Override
	public String getAdminUrl() {
		return this.pulsarProperties.getAdmin().getServiceUrl();
	}

}
