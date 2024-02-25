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

package org.springframework.boot.autoconfigure.web.reactive;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ContextPathCompositeHandler;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link HttpHandler}.
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 * @author Lasse Wulff
 * @since 2.0.0
 */
@AutoConfiguration(after = { WebFluxAutoConfiguration.class })
@ConditionalOnClass({ DispatcherHandler.class, HttpHandler.class })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnMissingBean(HttpHandler.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
public class HttpHandlerAutoConfiguration {

	/**
	 * AnnotationConfig class.
	 */
	@Configuration(proxyBeanMethods = false)
	public static class AnnotationConfig {

		private final ApplicationContext applicationContext;

		/**
		 * Constructs a new AnnotationConfig object with the specified ApplicationContext.
		 * @param applicationContext the ApplicationContext to be associated with this
		 * AnnotationConfig object
		 */
		public AnnotationConfig(ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		/**
		 * Creates and configures an HttpHandler for the WebFlux application.
		 * @param propsProvider The provider for WebFluxProperties.
		 * @param handlerBuilderCustomizers The provider for
		 * WebHttpHandlerBuilderCustomizer.
		 * @return The configured HttpHandler for the WebFlux application.
		 */
		@Bean
		public HttpHandler httpHandler(ObjectProvider<WebFluxProperties> propsProvider,
				ObjectProvider<WebHttpHandlerBuilderCustomizer> handlerBuilderCustomizers) {
			WebHttpHandlerBuilder handlerBuilder = WebHttpHandlerBuilder.applicationContext(this.applicationContext);
			handlerBuilderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(handlerBuilder));
			HttpHandler httpHandler = handlerBuilder.build();
			WebFluxProperties properties = propsProvider.getIfAvailable();
			if (properties != null && StringUtils.hasText(properties.getBasePath())) {
				Map<String, HttpHandler> handlersMap = Collections.singletonMap(properties.getBasePath(), httpHandler);
				return new ContextPathCompositeHandler(handlersMap);
			}
			return httpHandler;
		}

	}

}
