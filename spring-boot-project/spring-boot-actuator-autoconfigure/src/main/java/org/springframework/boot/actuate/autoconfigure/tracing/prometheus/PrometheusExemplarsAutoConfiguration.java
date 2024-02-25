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

package org.springframework.boot.actuate.autoconfigure.tracing.prometheus;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.tracing.MicrometerTracingAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.function.SingletonSupplier;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Prometheus Exemplars with
 * Micrometer Tracing.
 *
 * @author Jonatan Ivanov
 * @since 3.0.0
 */
@AutoConfiguration(before = PrometheusMetricsExportAutoConfiguration.class,
		after = MicrometerTracingAutoConfiguration.class)
@ConditionalOnBean(Tracer.class)
@ConditionalOnClass({ Tracer.class, SpanContextSupplier.class })
public class PrometheusExemplarsAutoConfiguration {

	/**
	 * Returns a {@link SpanContextSupplier} bean if no other bean of the same type is
	 * present in the application context. The returned bean is an instance of
	 * {@link LazyTracingSpanContextSupplier} which is responsible for supplying span
	 * context information for tracing purposes. It uses an {@link ObjectProvider} to
	 * lazily retrieve an instance of {@link Tracer} from the application context.
	 * @param tracerProvider the object provider for retrieving an instance of
	 * {@link Tracer}
	 * @return a {@link SpanContextSupplier} bean
	 */
	@Bean
	@ConditionalOnMissingBean
	SpanContextSupplier spanContextSupplier(ObjectProvider<Tracer> tracerProvider) {
		return new LazyTracingSpanContextSupplier(tracerProvider);
	}

	/**
	 * Since the MeterRegistry can depend on the {@link Tracer} (Exemplars) and the
	 * {@link Tracer} can depend on the MeterRegistry (recording metrics), this
	 * {@link SpanContextSupplier} breaks the cycle by lazily loading the {@link Tracer}.
	 */
	static class LazyTracingSpanContextSupplier implements SpanContextSupplier {

		private final SingletonSupplier<Tracer> tracer;

		/**
		 * Constructs a new LazyTracingSpanContextSupplier with the given tracer provider.
		 * @param tracerProvider the provider for obtaining the Tracer object
		 */
		LazyTracingSpanContextSupplier(ObjectProvider<Tracer> tracerProvider) {
			this.tracer = SingletonSupplier.of(tracerProvider::getObject);
		}

		/**
		 * Returns the trace ID associated with the current span.
		 * @return the trace ID as a String, or null if there is no current span
		 */
		@Override
		public String getTraceId() {
			Span currentSpan = currentSpan();
			return (currentSpan != null) ? currentSpan.context().traceId() : null;
		}

		/**
		 * Returns the span ID of the current span.
		 * @return the span ID of the current span, or null if there is no current span
		 */
		@Override
		public String getSpanId() {
			Span currentSpan = currentSpan();
			return (currentSpan != null) ? currentSpan.context().spanId() : null;
		}

		/**
		 * Returns a boolean value indicating whether the current span is sampled.
		 * @return true if the current span is sampled, false otherwise
		 */
		@Override
		public boolean isSampled() {
			Span currentSpan = currentSpan();
			if (currentSpan == null) {
				return false;
			}
			Boolean sampled = currentSpan.context().sampled();
			return sampled != null && sampled;
		}

		/**
		 * Returns the current span from the tracer obtained by the
		 * LazyTracingSpanContextSupplier.
		 * @return the current span
		 */
		private Span currentSpan() {
			return this.tracer.obtain().currentSpan();
		}

	}

}
