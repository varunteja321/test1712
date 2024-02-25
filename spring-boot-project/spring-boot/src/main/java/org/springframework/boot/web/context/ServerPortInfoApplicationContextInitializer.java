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

package org.springframework.boot.web.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

/**
 * {@link ApplicationContextInitializer} that sets {@link Environment} properties for the
 * ports that {@link WebServer} servers are actually listening on. The property
 * {@literal "local.server.port"} can be injected directly into tests using
 * {@link Value @Value} or obtained through the {@link Environment}.
 * <p>
 * If the {@link WebServerInitializedEvent} has a
 * {@link WebServerApplicationContext#getServerNamespace() server namespace} , it will be
 * used to construct the property name. For example, the "management" actuator context
 * will have the property name {@literal "local.management.port"}.
 * <p>
 * Properties are automatically propagated up to any parent context.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @since 2.0.0
 */
public class ServerPortInfoApplicationContextInitializer implements
		ApplicationContextInitializer<ConfigurableApplicationContext>, ApplicationListener<WebServerInitializedEvent> {

	private static final String PROPERTY_SOURCE_NAME = "server.ports";

	/**
	 * Initializes the ServerPortInfoApplicationContextInitializer by adding itself as an
	 * application listener to the provided ConfigurableApplicationContext.
	 * @param applicationContext the ConfigurableApplicationContext to which this
	 * initializer will be added as an application listener
	 */
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		applicationContext.addApplicationListener(this);
	}

	/**
	 * This method is called when the web server is initialized. It sets the port property
	 * for the application context based on the web server's port. The port property is
	 * determined by concatenating "local." with the name of the application context and
	 * ".port".
	 * @param event The WebServerInitializedEvent containing information about the web
	 * server initialization.
	 */
	@Override
	public void onApplicationEvent(WebServerInitializedEvent event) {
		String propertyName = "local." + getName(event.getApplicationContext()) + ".port";
		setPortProperty(event.getApplicationContext(), propertyName, event.getWebServer().getPort());
	}

	/**
	 * Returns the name of the web server application context.
	 * @param context the web server application context
	 * @return the name of the web server application context, or "server" if the name is
	 * empty or null
	 */
	private String getName(WebServerApplicationContext context) {
		String name = context.getServerNamespace();
		return StringUtils.hasText(name) ? name : "server";
	}

	/**
	 * Sets the port property for the given ApplicationContext and its parent
	 * ApplicationContexts.
	 * @param context the ApplicationContext to set the port property for
	 * @param propertyName the name of the port property
	 * @param port the port value to set
	 */
	private void setPortProperty(ApplicationContext context, String propertyName, int port) {
		if (context instanceof ConfigurableApplicationContext configurableContext) {
			setPortProperty(configurableContext.getEnvironment(), propertyName, port);
		}
		if (context.getParent() != null) {
			setPortProperty(context.getParent(), propertyName, port);
		}
	}

	/**
	 * Sets the port property in the given environment.
	 * @param environment the configurable environment
	 * @param propertyName the name of the property to set
	 * @param port the port value to set
	 */
	@SuppressWarnings("unchecked")
	private void setPortProperty(ConfigurableEnvironment environment, String propertyName, int port) {
		MutablePropertySources sources = environment.getPropertySources();
		PropertySource<?> source = sources.get(PROPERTY_SOURCE_NAME);
		if (source == null) {
			source = new MapPropertySource(PROPERTY_SOURCE_NAME, new HashMap<>());
			sources.addFirst(source);
		}
		((Map<String, Object>) source.getSource()).put(propertyName, port);
	}

}
