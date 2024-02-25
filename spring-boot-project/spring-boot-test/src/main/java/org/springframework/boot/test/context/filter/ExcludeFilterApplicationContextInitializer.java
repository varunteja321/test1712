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

package org.springframework.boot.test.context.filter;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * {@link ApplicationContextInitializer} to register the {@link TestTypeExcludeFilter} for
 * when {@link SpringApplication#from} is being used with the test classpath.
 *
 * @author Phillip Webb
 */
class ExcludeFilterApplicationContextInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	/**
	 * Initializes the application context by registering the TestTypeExcludeFilter with
	 * the bean factory.
	 * @param applicationContext the configurable application context to be initialized
	 */
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		TestTypeExcludeFilter.registerWith(applicationContext.getBeanFactory());
	}

}
