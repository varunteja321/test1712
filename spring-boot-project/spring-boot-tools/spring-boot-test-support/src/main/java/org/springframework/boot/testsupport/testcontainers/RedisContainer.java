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

package org.springframework.boot.testsupport.testcontainers;

import org.testcontainers.containers.GenericContainer;

/**
 * A {@link GenericContainer} for Redis.
 *
 * @author Andy Wilkinson
 * @author Madhura Bhave
 */
public class RedisContainer extends GenericContainer<RedisContainer> {

	/**
	 * Constructs a new RedisContainer object.
	 *
	 * This constructor initializes the RedisContainer object with the default Docker
	 * image name for Redis. It also adds the port 6379 to the list of exposed ports for
	 * the container.
	 */
	public RedisContainer() {
		super(DockerImageNames.redis());
		addExposedPorts(6379);
	}

}
