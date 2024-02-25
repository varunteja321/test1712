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

package org.springframework.boot.actuate.autoconfigure.context.properties;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.expose.EndpointExposure;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpointWebExtension;
import org.springframework.boot.actuate.endpoint.SanitizingFunction;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for the
 * {@link ConfigurationPropertiesReportEndpoint}.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Chris Bono
 * @since 2.0.0
 */
@AutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = ConfigurationPropertiesReportEndpoint.class)
@EnableConfigurationProperties(ConfigurationPropertiesReportEndpointProperties.class)
public class ConfigurationPropertiesReportEndpointAutoConfiguration {

	/**
	 * Creates a {@link ConfigurationPropertiesReportEndpoint} bean if there is no
	 * existing bean of the same type.
	 * @param properties The {@link ConfigurationPropertiesReportEndpointProperties}
	 * object containing the properties for the endpoint.
	 * @param sanitizingFunctions The {@link ObjectProvider} of {@link SanitizingFunction}
	 * objects used for sanitizing the endpoint values.
	 * @return The {@link ConfigurationPropertiesReportEndpoint} bean.
	 */
	@Bean
	@ConditionalOnMissingBean
	public ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint(
			ConfigurationPropertiesReportEndpointProperties properties,
			ObjectProvider<SanitizingFunction> sanitizingFunctions) {
		return new ConfigurationPropertiesReportEndpoint(sanitizingFunctions.orderedStream().toList(),
				properties.getShowValues());
	}

	/**
	 * Creates a web extension for the ConfigurationPropertiesReportEndpoint. This
	 * extension is conditional on the absence of a bean of the same type, the presence of
	 * the ConfigurationPropertiesReportEndpoint bean, and the availability of the
	 * endpoint for web and Cloud Foundry exposure.
	 * @param configurationPropertiesReportEndpoint The
	 * ConfigurationPropertiesReportEndpoint bean.
	 * @param properties The properties for the ConfigurationPropertiesReportEndpoint.
	 * @return The ConfigurationPropertiesReportEndpointWebExtension.
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(ConfigurationPropertiesReportEndpoint.class)
	@ConditionalOnAvailableEndpoint(exposure = { EndpointExposure.WEB, EndpointExposure.CLOUD_FOUNDRY })
	public ConfigurationPropertiesReportEndpointWebExtension configurationPropertiesReportEndpointWebExtension(
			ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint,
			ConfigurationPropertiesReportEndpointProperties properties) {
		return new ConfigurationPropertiesReportEndpointWebExtension(configurationPropertiesReportEndpoint,
				properties.getShowValues(), properties.getRoles());
	}

}
