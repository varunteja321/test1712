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

package org.springframework.boot.actuate.autoconfigure.metrics.export.otlp;

import java.util.Map;

import io.micrometer.registry.otlp.AggregationTemporality;
import io.micrometer.registry.otlp.OtlpConfig;

import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryPropertiesConfigAdapter;
import org.springframework.boot.autoconfigure.otlp.OtlpConnectionDetails;

/**
 * Adapter to convert {@link OtlpProperties} to an {@link OtlpConfig}.
 *
 * @author Eddú Meléndez
 * @author Jonatan Ivanov
 */
class OtlpPropertiesConfigAdapter extends StepRegistryPropertiesConfigAdapter<OtlpProperties> implements OtlpConfig {

	private final OtlpConnectionDetails connectionDetails;

	OtlpPropertiesConfigAdapter(OtlpProperties properties, OtlpConnectionDetails connectionDetails) {
		super(properties);
		this.connectionDetails = connectionDetails;
	}

	@Override
	public String prefix() {
		return "management.otlp.metrics.export";
	}

	@Override
	public String url() {
		return get((properties) -> this.connectionDetails.getUrl(), OtlpConfig.super::url);
	}

	@Override
	public AggregationTemporality aggregationTemporality() {
		return get(OtlpProperties::getAggregationTemporality, OtlpConfig.super::aggregationTemporality);
	}

	@Override
	public Map<String, String> resourceAttributes() {
		return get(OtlpProperties::getResourceAttributes, OtlpConfig.super::resourceAttributes);
	}

	@Override
	public Map<String, String> headers() {
		return get(OtlpProperties::getHeaders, OtlpConfig.super::headers);
	}

}
