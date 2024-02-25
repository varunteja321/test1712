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

package org.springframework.boot.actuate.autoconfigure.tracing.zipkin;

import zipkin2.Span;
import zipkin2.codec.BytesEncoder;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;

import org.springframework.boot.actuate.autoconfigure.tracing.zipkin.ZipkinConfigurations.BraveConfiguration;
import org.springframework.boot.actuate.autoconfigure.tracing.zipkin.ZipkinConfigurations.OpenTelemetryConfiguration;
import org.springframework.boot.actuate.autoconfigure.tracing.zipkin.ZipkinConfigurations.ReporterConfiguration;
import org.springframework.boot.actuate.autoconfigure.tracing.zipkin.ZipkinConfigurations.SenderConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Zipkin.
 * <p>
 * It uses imports on {@link ZipkinConfigurations} to guarantee the correct configuration
 * ordering.
 *
 * @author Moritz Halbritter
 * @since 3.0.0
 */
@AutoConfiguration(after = RestTemplateAutoConfiguration.class)
@ConditionalOnClass(Sender.class)
@Import({ SenderConfiguration.class, ReporterConfiguration.class, BraveConfiguration.class,
		OpenTelemetryConfiguration.class })
@EnableConfigurationProperties(ZipkinProperties.class)
public class ZipkinAutoConfiguration {

	/**
	 * Generates a {@link PropertiesZipkinConnectionDetails} bean if there is no existing
	 * bean of type {@link ZipkinConnectionDetails}. Uses the provided
	 * {@link ZipkinProperties} to create the connection details.
	 * @param properties The {@link ZipkinProperties} used to create the connection
	 * details.
	 * @return The generated {@link PropertiesZipkinConnectionDetails} bean.
	 */
	@Bean
	@ConditionalOnMissingBean(ZipkinConnectionDetails.class)
	PropertiesZipkinConnectionDetails zipkinConnectionDetails(ZipkinProperties properties) {
		return new PropertiesZipkinConnectionDetails(properties);
	}

	/**
	 * Returns a {@link BytesEncoder} for encoding {@link Span} objects into JSON format.
	 * This method is annotated with {@link ConditionalOnMissingBean} to ensure that it is
	 * only executed if no other bean of type {@link BytesEncoder<Span>} is present in the
	 * application context. The returned {@link BytesEncoder} is configured to use JSON_V2
	 * format for encoding spans.
	 * @return a {@link BytesEncoder} for encoding {@link Span} objects into JSON format
	 */
	@Bean
	@ConditionalOnMissingBean
	public BytesEncoder<Span> spanBytesEncoder() {
		return SpanBytesEncoder.JSON_V2;
	}

}
