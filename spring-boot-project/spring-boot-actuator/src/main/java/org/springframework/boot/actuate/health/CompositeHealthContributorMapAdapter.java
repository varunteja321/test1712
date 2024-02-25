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
 * {@link CompositeHealthContributor} backed by a map with values adapted as necessary.
 *
 * @param <V> the value type
 * @author Phillip Webb
 */
class CompositeHealthContributorMapAdapter<V> extends NamedContributorsMapAdapter<V, HealthContributor>
		implements CompositeHealthContributor {

	/**
	 * Constructs a new CompositeHealthContributorMapAdapter with the specified map and
	 * value adapter.
	 * @param map the map to be adapted
	 * @param valueAdapter the function used to adapt the values in the map to
	 * HealthContributor instances
	 */
	CompositeHealthContributorMapAdapter(Map<String, V> map, Function<V, ? extends HealthContributor> valueAdapter) {
		super(map, valueAdapter);
	}

}
