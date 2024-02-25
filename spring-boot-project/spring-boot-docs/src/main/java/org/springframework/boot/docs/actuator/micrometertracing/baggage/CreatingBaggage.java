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

package org.springframework.boot.docs.actuator.micrometertracing.baggage;

import io.micrometer.tracing.BaggageInScope;
import io.micrometer.tracing.Tracer;

import org.springframework.stereotype.Component;

/**
 * CreatingBaggage class.
 */
@Component
class CreatingBaggage {

	private final Tracer tracer;

	/**
	 * Creates a new instance of the CreatingBaggage class.
	 * @param tracer the Tracer object used for tracing baggage
	 */
	CreatingBaggage(Tracer tracer) {
		this.tracer = tracer;
	}

	/**
	 * Performs the specified business logic with a created baggage in scope.
	 * @throws Exception if an error occurs during the execution of the business logic.
	 */
	void doSomething() {
		try (BaggageInScope scope = this.tracer.createBaggageInScope("baggage1", "value1")) {
			// Business logic
		}
	}

}
