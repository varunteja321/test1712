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

package org.springframework.boot.context.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link ApplicationListener} that delegates to other listeners that are specified under
 * a {@literal context.listener.classes} environment property.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @since 1.0.0
 * @deprecated since 3.2.0 for removal in 3.4.0 as property based initialization is no
 * longer recommended
 */
@Deprecated(since = "3.2.0", forRemoval = true)
public class DelegatingApplicationListener implements ApplicationListener<ApplicationEvent>, Ordered {

	// NOTE: Similar to org.springframework.web.context.ContextLoader

	private static final String PROPERTY_NAME = "context.listener.classes";

	private int order = 0;

	private SimpleApplicationEventMulticaster multicaster;

	/**
	 * This method is called when an application event is triggered. It checks if the
	 * event is an instance of ApplicationEnvironmentPreparedEvent and retrieves the
	 * listeners from the environment. If there are no listeners, the method returns.
	 * Otherwise, it creates a SimpleApplicationEventMulticaster and adds the listeners to
	 * it. Finally, it multicasts the event using the multicaster.
	 * @param event The application event triggered
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationEnvironmentPreparedEvent preparedEvent) {
			List<ApplicationListener<ApplicationEvent>> delegates = getListeners(preparedEvent.getEnvironment());
			if (delegates.isEmpty()) {
				return;
			}
			this.multicaster = new SimpleApplicationEventMulticaster();
			for (ApplicationListener<ApplicationEvent> listener : delegates) {
				this.multicaster.addApplicationListener(listener);
			}
		}
		if (this.multicaster != null) {
			this.multicaster.multicastEvent(event);
		}
	}

	/**
	 * Retrieves the list of application listeners based on the provided environment.
	 * @param environment the configurable environment
	 * @return the list of application listeners
	 */
	@SuppressWarnings("unchecked")
	private List<ApplicationListener<ApplicationEvent>> getListeners(ConfigurableEnvironment environment) {
		if (environment == null) {
			return Collections.emptyList();
		}
		String classNames = environment.getProperty(PROPERTY_NAME);
		List<ApplicationListener<ApplicationEvent>> listeners = new ArrayList<>();
		if (StringUtils.hasLength(classNames)) {
			for (String className : StringUtils.commaDelimitedListToSet(classNames)) {
				try {
					Class<?> clazz = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
					Assert.isAssignable(ApplicationListener.class, clazz,
							() -> "class [" + className + "] must implement ApplicationListener");
					listeners.add((ApplicationListener<ApplicationEvent>) BeanUtils.instantiateClass(clazz));
				}
				catch (Exception ex) {
					throw new ApplicationContextException("Failed to load context listener class [" + className + "]",
							ex);
				}
			}
		}
		AnnotationAwareOrderComparator.sort(listeners);
		return listeners;
	}

	/**
	 * Sets the order of the DelegatingApplicationListener.
	 * @param order the order value to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Returns the order value of this DelegatingApplicationListener.
	 * @return the order value of this DelegatingApplicationListener
	 */
	@Override
	public int getOrder() {
		return this.order;
	}

}
