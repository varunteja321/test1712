/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.actuate.health;

import java.util.Map;
import java.util.function.Function;

/**
 * Default {@link ReactiveHealthContributorRegistry} implementation.
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
public class DefaultReactiveHealthContributorRegistry extends DefaultContributorRegistry<ReactiveHealthContributor>
		implements ReactiveHealthContributorRegistry {

	/**
	 * Constructs a new DefaultReactiveHealthContributorRegistry.
	 */
	public DefaultReactiveHealthContributorRegistry() {
	}

	/**
	 * Constructs a new DefaultReactiveHealthContributorRegistry with the specified
	 * contributors.
	 * @param contributors a map of contributors where the key is the name of the
	 * contributor and the value is the ReactiveHealthContributor instance
	 */
	public DefaultReactiveHealthContributorRegistry(Map<String, ReactiveHealthContributor> contributors) {
		super(contributors);
	}

	/**
	 * Constructs a new DefaultReactiveHealthContributorRegistry with the specified
	 * contributors and name factory.
	 * @param contributors the map of contributors to be registered in the registry
	 * @param nameFactory the function used to generate names for the contributors
	 */
	public DefaultReactiveHealthContributorRegistry(Map<String, ReactiveHealthContributor> contributors,
			Function<String, String> nameFactory) {
		super(contributors, nameFactory);
	}

}
