/*
 * Copyright 2012-2022 the original author or authors.
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

package org.springframework.boot.autoconfigure.jms;

import java.util.Arrays;

import javax.naming.NamingException;

import jakarta.jms.ConnectionFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJndi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration.JndiOrPropertyCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.util.StringUtils;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for JMS provided from JNDI.
 *
 * @author Phillip Webb
 * @since 1.2.0
 */
@AutoConfiguration(before = JmsAutoConfiguration.class)
@ConditionalOnClass(JmsTemplate.class)
@ConditionalOnMissingBean(ConnectionFactory.class)
@Conditional(JndiOrPropertyCondition.class)
@EnableConfigurationProperties(JmsProperties.class)
public class JndiConnectionFactoryAutoConfiguration {

	// Keep these in sync with the condition below
	private static final String[] JNDI_LOCATIONS = { "java:/JmsXA", "java:/XAConnectionFactory" };

	/**
	 * Creates a JMS connection factory based on the provided JmsProperties. If a JNDI
	 * name is specified in the properties, it will be used to lookup the connection
	 * factory. Otherwise, a default JNDI connection factory will be used.
	 * @param properties the JmsProperties containing the JNDI name and other
	 * configuration properties
	 * @return the JMS connection factory
	 * @throws NamingException if there is an error in looking up the JNDI connection
	 * factory
	 */
	@Bean
	public ConnectionFactory jmsConnectionFactory(JmsProperties properties) throws NamingException {
		JndiLocatorDelegate jndiLocatorDelegate = JndiLocatorDelegate.createDefaultResourceRefLocator();
		if (StringUtils.hasLength(properties.getJndiName())) {
			return jndiLocatorDelegate.lookup(properties.getJndiName(), ConnectionFactory.class);
		}
		return findJndiConnectionFactory(jndiLocatorDelegate);
	}

	/**
	 * Finds the JNDI ConnectionFactory using the provided JndiLocatorDelegate.
	 * @param jndiLocatorDelegate the JndiLocatorDelegate used to lookup the
	 * ConnectionFactory
	 * @return the ConnectionFactory found in JNDI
	 * @throws IllegalStateException if the ConnectionFactory is not found in any of the
	 * JNDI locations
	 */
	private ConnectionFactory findJndiConnectionFactory(JndiLocatorDelegate jndiLocatorDelegate) {
		for (String name : JNDI_LOCATIONS) {
			try {
				return jndiLocatorDelegate.lookup(name, ConnectionFactory.class);
			}
			catch (NamingException ex) {
				// Swallow and continue
			}
		}
		throw new IllegalStateException(
				"Unable to find ConnectionFactory in JNDI locations " + Arrays.asList(JNDI_LOCATIONS));
	}

	/**
	 * Condition for JNDI name or a specific property.
	 */
	static class JndiOrPropertyCondition extends AnyNestedCondition {

		/**
		 * Constructs a new JndiOrPropertyCondition with the specified configuration
		 * phase.
		 * @param configurationPhase the configuration phase for this condition
		 */
		JndiOrPropertyCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		/**
		 * Jndi class.
		 */
		@ConditionalOnJndi({ "java:/JmsXA", "java:/XAConnectionFactory" })
		static class Jndi {

		}

		/**
		 * Property class.
		 */
		@ConditionalOnProperty(prefix = "spring.jms", name = "jndi-name")
		static class Property {

		}

	}

}
