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

package org.springframework.boot.actuate.autoconfigure.metrics.export.signalfx;

import io.micrometer.core.instrument.Clock;
import io.micrometer.signalfx.SignalFxConfig;
import io.micrometer.signalfx.SignalFxMeterRegistry;

import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.ConditionalOnEnabledMetricsExport;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for exporting metrics to SignalFX.
 *
 * @author Jon Schneider
 * @author Andy Wilkinson
 * @since 2.0.0
 */
@AutoConfiguration(
		before = { CompositeMeterRegistryAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class },
		after = MetricsAutoConfiguration.class)
@ConditionalOnBean(Clock.class)
@ConditionalOnClass(SignalFxMeterRegistry.class)
@ConditionalOnEnabledMetricsExport("signalfx")
@EnableConfigurationProperties(SignalFxProperties.class)
public class SignalFxMetricsExportAutoConfiguration {

	/**
	 * Creates a SignalFxConfig bean if no other bean of the same type is present.
	 * @param props the SignalFxProperties object containing the configuration properties
	 * @return a SignalFxConfig bean configured with the provided properties
	 */
	@Bean
	@ConditionalOnMissingBean
	public SignalFxConfig signalfxConfig(SignalFxProperties props) {
		return new SignalFxPropertiesConfigAdapter(props);
	}

	/**
	 * Creates a new instance of SignalFxMeterRegistry if no other bean of type
	 * SignalFxMeterRegistry is present.
	 * @param config the SignalFxConfig object containing the configuration for the meter
	 * registry
	 * @param clock the Clock object used for measuring time
	 * @return a new instance of SignalFxMeterRegistry
	 */
	@Bean
	@ConditionalOnMissingBean
	public SignalFxMeterRegistry signalFxMeterRegistry(SignalFxConfig config, Clock clock) {
		return new SignalFxMeterRegistry(config, clock);
	}

}
