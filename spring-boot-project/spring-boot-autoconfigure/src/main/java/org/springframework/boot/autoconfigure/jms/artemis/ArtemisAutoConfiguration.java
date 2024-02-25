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

package org.springframework.boot.autoconfigure.jms.artemis;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} to integrate with an Artemis broker.
 * If the necessary classes are present, embed the broker in the application by default.
 * Otherwise, connect to a broker available on the local machine with the default
 * settings.
 *
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @since 1.3.0
 * @see ArtemisProperties
 */
@AutoConfiguration(before = JmsAutoConfiguration.class, after = JndiConnectionFactoryAutoConfiguration.class)
@ConditionalOnClass({ ConnectionFactory.class, ActiveMQConnectionFactory.class })
@ConditionalOnMissingBean(ConnectionFactory.class)
@EnableConfigurationProperties({ ArtemisProperties.class, JmsProperties.class })
@Import({ ArtemisEmbeddedServerConfiguration.class, ArtemisXAConnectionFactoryConfiguration.class,
		ArtemisConnectionFactoryConfiguration.class })
public class ArtemisAutoConfiguration {

	/**
	 * Generates an instance of ArtemisConnectionDetails if no other bean of type
	 * ArtemisConnectionDetails is present. Uses the provided ArtemisProperties to create
	 * a new instance of PropertiesArtemisConnectionDetails.
	 * @param properties The ArtemisProperties used to create the new instance of
	 * PropertiesArtemisConnectionDetails.
	 * @return An instance of ArtemisConnectionDetails.
	 */
	@Bean
	@ConditionalOnMissingBean(ArtemisConnectionDetails.class)
	ArtemisConnectionDetails artemisConnectionDetails(ArtemisProperties properties) {
		return new PropertiesArtemisConnectionDetails(properties);
	}

	/**
	 * Adapts {@link ArtemisProperties} to {@link ArtemisConnectionDetails}.
	 */
	static class PropertiesArtemisConnectionDetails implements ArtemisConnectionDetails {

		private final ArtemisProperties properties;

		/**
		 * Constructs a new instance of PropertiesArtemisConnectionDetails with the
		 * specified ArtemisProperties.
		 * @param properties the ArtemisProperties to be used for configuring the
		 * connection details
		 */
		PropertiesArtemisConnectionDetails(ArtemisProperties properties) {
			this.properties = properties;
		}

		/**
		 * Returns the Artemis mode of the connection details.
		 * @return the Artemis mode of the connection details
		 */
		@Override
		public ArtemisMode getMode() {
			return this.properties.getMode();
		}

		/**
		 * Returns the broker URL.
		 * @return the broker URL
		 */
		@Override
		public String getBrokerUrl() {
			return this.properties.getBrokerUrl();
		}

		/**
		 * Returns the user associated with this connection details.
		 * @return the user associated with this connection details
		 */
		@Override
		public String getUser() {
			return this.properties.getUser();
		}

		/**
		 * Returns the password for the Artemis connection details.
		 * @return the password for the Artemis connection details
		 */
		@Override
		public String getPassword() {
			return this.properties.getPassword();
		}

	}

}
