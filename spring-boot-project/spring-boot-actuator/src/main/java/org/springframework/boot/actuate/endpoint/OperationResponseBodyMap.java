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

package org.springframework.boot.actuate.endpoint;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link LinkedHashMap} to support {@link OperationResponseBody#of(java.util.Map)}.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Phillip Webb
 */
class OperationResponseBodyMap<K, V> extends LinkedHashMap<K, V> implements OperationResponseBody {

	/**
	 * Constructs a new OperationResponseBodyMap object with the specified map.
	 * @param map the map to be used for constructing the OperationResponseBodyMap object
	 */
	OperationResponseBodyMap(Map<? extends K, ? extends V> map) {
		super(map);
	}

}
