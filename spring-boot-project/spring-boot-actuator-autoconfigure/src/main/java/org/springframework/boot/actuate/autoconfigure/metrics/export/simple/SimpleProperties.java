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

package org.springframework.boot.actuate.autoconfigure.metrics.export.simple;

import java.time.Duration;

import io.micrometer.core.instrument.simple.CountingMode;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@link ConfigurationProperties @ConfigurationProperties} for configuring metrics export
 * to a {@link SimpleMeterRegistry}.
 *
 * @author Jon Schneider
 * @author Stephane Nicoll
 * @since 2.0.0
 */
@ConfigurationProperties(prefix = "management.simple.metrics.export")
public class SimpleProperties {

	/**
	 * Whether exporting of metrics to this backend is enabled.
	 */
	private boolean enabled = true;

	/**
	 * Step size (i.e. reporting frequency) to use.
	 */
	private Duration step = Duration.ofMinutes(1);

	/**
	 * Counting mode.
	 */
	private CountingMode mode = CountingMode.CUMULATIVE;

	/**
	 * Returns the current status of the enabled flag.
	 * @return true if the enabled flag is set to true, false otherwise.
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * Sets the enabled status of the SimpleProperties object.
	 * @param enabled the new enabled status to be set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns the step duration.
	 * @return the step duration
	 */
	public Duration getStep() {
		return this.step;
	}

	/**
	 * Sets the step duration for the SimpleProperties class.
	 * @param step the duration to set as the step
	 */
	public void setStep(Duration step) {
		this.step = step;
	}

	/**
	 * Returns the counting mode of the SimpleProperties object.
	 * @return the counting mode of the SimpleProperties object
	 */
	public CountingMode getMode() {
		return this.mode;
	}

	/**
	 * Sets the counting mode for the SimpleProperties object.
	 * @param mode the counting mode to be set
	 */
	public void setMode(CountingMode mode) {
		this.mode = mode;
	}

}
