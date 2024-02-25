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

package org.springframework.boot.autoconfigure.web.reactive.function.client;

import org.springframework.boot.ssl.SslBundle;
import org.springframework.http.client.reactive.ClientHttpConnector;

/**
 * Internal factory used to create {@link ClientHttpConnector} instances.
 *
 * @param <T> the {@link ClientHttpConnector} type
 * @author Phillip Webb
 */
@FunctionalInterface
interface ClientHttpConnectorFactory<T extends ClientHttpConnector> {

	/**
	 * Creates a default instance of the {@link ClientHttpConnector} interface.
	 * @return a default instance of the {@link ClientHttpConnector} interface
	 */
	default T createClientHttpConnector() {
		return createClientHttpConnector(null);
	}

	T createClientHttpConnector(SslBundle sslBundle);

}
