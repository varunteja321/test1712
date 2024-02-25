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

package org.springframework.boot.actuate.autoconfigure.tracing.zipkin;

/**
 * Adapts {@link ZipkinProperties} to {@link ZipkinConnectionDetails}.
 *
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
class PropertiesZipkinConnectionDetails implements ZipkinConnectionDetails {

	private final ZipkinProperties properties;

	/**
	 * Constructs a new instance of PropertiesZipkinConnectionDetails with the specified
	 * ZipkinProperties.
	 * @param properties the ZipkinProperties to be used for the connection details
	 */
	PropertiesZipkinConnectionDetails(ZipkinProperties properties) {
		this.properties = properties;
	}

	/**
	 * Returns the endpoint of the Span in the Zipkin connection details.
	 * @return the endpoint of the Span
	 */
	@Override
	public String getSpanEndpoint() {
		return this.properties.getEndpoint();
	}

}
