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

package org.springframework.boot.actuate.autoconfigure.tracing;

import java.util.List;

import brave.baggage.BaggageField;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagation.FactoryBuilder;
import brave.baggage.BaggagePropagationConfig;
import brave.baggage.BaggagePropagationCustomizer;
import brave.baggage.CorrelationScopeConfig.SingleCorrelationField;
import brave.baggage.CorrelationScopeCustomizer;
import brave.baggage.CorrelationScopeDecorator;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext.ScopeDecorator;
import brave.propagation.Propagation;
import brave.propagation.Propagation.Factory;
import brave.propagation.Propagation.KeyFactory;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.tracing.TracingProperties.Baggage.Correlation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Brave propagation configurations. They are imported by {@link BraveAutoConfiguration}.
 *
 * @author Moritz Halbritter
 */
class BravePropagationConfigurations {

	/**
	 * Propagates traces but no baggage.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(value = "management.tracing.baggage.enabled", havingValue = "false")
	static class PropagationWithoutBaggage {

		/**
		 * Creates a composite propagation factory based on the provided tracing
		 * properties. This method is annotated with @Bean to indicate that it is a bean
		 * definition method. It is also annotated
		 * with @ConditionalOnMissingBean(Factory.class) to ensure that this bean is only
		 * created if there is no existing bean of type Factory. Additionally, it is
		 * annotated with @ConditionalOnEnabledTracing to ensure that tracing is enabled
		 * before creating this bean.
		 * @param properties the tracing properties used to configure the composite
		 * propagation factory
		 * @return the created composite propagation factory
		 */
		@Bean
		@ConditionalOnMissingBean(Factory.class)
		@ConditionalOnEnabledTracing
		CompositePropagationFactory propagationFactory(TracingProperties properties) {
			return CompositePropagationFactory.create(properties.getPropagation());
		}

	}

	/**
	 * Propagates traces and baggage.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(value = "management.tracing.baggage.enabled", matchIfMissing = true)
	@EnableConfigurationProperties(TracingProperties.class)
	static class PropagationWithBaggage {

		private final TracingProperties tracingProperties;

		/**
		 * Initializes a new instance of the PropagationWithBaggage class with the
		 * specified tracing properties.
		 * @param tracingProperties The tracing properties to be used for propagation.
		 */
		PropagationWithBaggage(TracingProperties tracingProperties) {
			this.tracingProperties = tracingProperties;
		}

		/**
		 * Creates a BaggagePropagation.FactoryBuilder bean if no other bean of the same
		 * type is present. This method is conditional on the absence of a bean of the
		 * same type.
		 *
		 * The BaggagePropagation.FactoryBuilder is responsible for building a factory
		 * that creates instances of BaggagePropagation.
		 *
		 * This method takes an ObjectProvider of BaggagePropagationCustomizer as a
		 * parameter, which allows for customizing the BaggagePropagation.FactoryBuilder.
		 *
		 * The method first creates a throw-away builder with a throw-away factory, as
		 * there is a chicken-and-egg problem where the builder needs a factory, but the
		 * CompositePropagationFactory needs data from the builder.
		 *
		 * It then iterates over the baggagePropagationCustomizers and applies each
		 * customizer to the throw-away builder.
		 *
		 * Next, it creates a CompositePropagationFactory using the tracingProperties for
		 * propagation and a BraveBaggageManager for baggage.
		 *
		 * After that, it creates the real builder using the CompositePropagationFactory
		 * and copies the configuration from the throw-away builder to the real builder.
		 *
		 * Finally, it returns the real builder.
		 * @param baggagePropagationCustomizers the ObjectProvider of
		 * BaggagePropagationCustomizer used for customizing the
		 * BaggagePropagation.FactoryBuilder
		 * @return the BaggagePropagation.FactoryBuilder bean
		 */
		@Bean
		@ConditionalOnMissingBean
		BaggagePropagation.FactoryBuilder propagationFactoryBuilder(
				ObjectProvider<BaggagePropagationCustomizer> baggagePropagationCustomizers) {
			// There's a chicken-and-egg problem here: to create a builder, we need a
			// factory. But the CompositePropagationFactory needs data from the builder.
			// We create a throw-away builder with a throw-away factory, and then copy the
			// config to the real builder.
			FactoryBuilder throwAwayBuilder = BaggagePropagation.newFactoryBuilder(createThrowAwayFactory());
			baggagePropagationCustomizers.orderedStream()
				.forEach((customizer) -> customizer.customize(throwAwayBuilder));
			CompositePropagationFactory propagationFactory = CompositePropagationFactory.create(
					this.tracingProperties.getPropagation(),
					new BraveBaggageManager(this.tracingProperties.getBaggage().getTagFields()),
					LocalBaggageFields.extractFrom(throwAwayBuilder));
			FactoryBuilder builder = BaggagePropagation.newFactoryBuilder(propagationFactory);
			throwAwayBuilder.configs().forEach(builder::add);
			return builder;
		}

		/**
		 * Creates a throwaway factory.
		 * @return a new instance of Factory
		 * @deprecated This method is deprecated and should not be used.
		 */
		@SuppressWarnings("deprecation")
		private Factory createThrowAwayFactory() {
			return new Factory() {

				@Override
				public <K> Propagation<K> create(KeyFactory<K> keyFactory) {
					return null;
				}

			};
		}

		/**
		 * Customizer for baggage propagation configuration. Adds remote and local baggage
		 * fields to the builder based on the configuration properties.
		 * @return the baggage propagation customizer
		 */
		@Bean
		BaggagePropagationCustomizer remoteFieldsBaggagePropagationCustomizer() {
			return (builder) -> {
				List<String> remoteFields = this.tracingProperties.getBaggage().getRemoteFields();
				for (String fieldName : remoteFields) {
					builder.add(BaggagePropagationConfig.SingleBaggageField.remote(BaggageField.create(fieldName)));
				}
				List<String> localFields = this.tracingProperties.getBaggage().getLocalFields();
				for (String localFieldName : localFields) {
					builder.add(BaggagePropagationConfig.SingleBaggageField.local(BaggageField.create(localFieldName)));
				}
			};
		}

		/**
		 * Generates a factory for baggage propagation based on the provided factory
		 * builder. This method is annotated with @Bean, @ConditionalOnMissingBean,
		 * and @ConditionalOnEnabledTracing.
		 * @param factoryBuilder the factory builder used to build the baggage propagation
		 * factory
		 * @return the baggage propagation factory generated by the factory builder
		 */
		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnEnabledTracing
		Factory propagationFactory(BaggagePropagation.FactoryBuilder factoryBuilder) {
			return factoryBuilder.build();
		}

		/**
		 * Returns a builder for creating a {@link CorrelationScopeDecorator} with MDC
		 * (Mapped Diagnostic Context) support. This builder is conditional on the absence
		 * of an existing bean of type {@link CorrelationScopeDecorator}.
		 * @param correlationScopeCustomizers an object provider for
		 * {@link CorrelationScopeCustomizer} instances
		 * @return a builder for creating a {@link CorrelationScopeDecorator} with MDC
		 * support
		 */
		@Bean
		@ConditionalOnMissingBean
		CorrelationScopeDecorator.Builder mdcCorrelationScopeDecoratorBuilder(
				ObjectProvider<CorrelationScopeCustomizer> correlationScopeCustomizers) {
			CorrelationScopeDecorator.Builder builder = MDCScopeDecorator.newBuilder();
			correlationScopeCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
			return builder;
		}

		/**
		 * Creates a customizer for correlation fields in the correlation scope. This
		 * customizer is conditionally enabled based on the property
		 * "management.tracing.baggage.correlation.enabled". If the property is not
		 * present, the customizer is enabled by default. The customizer adds correlation
		 * fields to the correlation scope based on the configuration properties. Each
		 * field is created as a baggage field and added to the correlation scope builder.
		 * The baggage field is flushed on update.
		 * @return the correlation fields correlation scope customizer
		 */
		@Bean
		@Order(0)
		@ConditionalOnProperty(prefix = "management.tracing.baggage.correlation", name = "enabled",
				matchIfMissing = true)
		CorrelationScopeCustomizer correlationFieldsCorrelationScopeCustomizer() {
			return (builder) -> {
				Correlation correlationProperties = this.tracingProperties.getBaggage().getCorrelation();
				for (String field : correlationProperties.getFields()) {
					BaggageField baggageField = BaggageField.create(field);
					SingleCorrelationField correlationField = SingleCorrelationField.newBuilder(baggageField)
						.flushOnUpdate()
						.build();
					builder.add(correlationField);
				}
			};
		}

		/**
		 * Generates a new instance of ScopeDecorator if there is no existing bean of type
		 * CorrelationScopeDecorator.
		 * @param builder the builder used to construct the CorrelationScopeDecorator
		 * instance
		 * @return a new instance of ScopeDecorator if there is no existing bean of type
		 * CorrelationScopeDecorator
		 */
		@Bean
		@ConditionalOnMissingBean(CorrelationScopeDecorator.class)
		ScopeDecorator correlationScopeDecorator(CorrelationScopeDecorator.Builder builder) {
			return builder.build();
		}

	}

	/**
	 * Propagates neither traces nor baggage.
	 */
	@Configuration(proxyBeanMethods = false)
	static class NoPropagation {

		/**
		 * Creates a CompositePropagationFactory with a noop implementation if no other
		 * bean of type Factory is present.
		 * @return the CompositePropagationFactory with noop implementation
		 */
		@Bean
		@ConditionalOnMissingBean(Factory.class)
		CompositePropagationFactory noopPropagationFactory() {
			return CompositePropagationFactory.noop();
		}

	}

}
