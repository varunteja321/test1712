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

package org.springframework.boot.docker.compose.service.connection.mysql;

import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * MySQL environment details.
 *
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @author Scott Frederick
 */
class MySqlEnvironment {

	private final String username;

	private final String password;

	private final String database;

	/**
	 * Constructs a new MySqlEnvironment object with the provided environment variables.
	 * @param env a map containing the environment variables
	 * @throws NullPointerException if the env parameter is null
	 */
	MySqlEnvironment(Map<String, String> env) {
		this.username = env.getOrDefault("MYSQL_USER", "root");
		this.password = extractPassword(env);
		this.database = extractDatabase(env);
	}

	/**
	 * Extracts the password from the given environment variables.
	 * @param env the environment variables
	 * @return the extracted password
	 * @throws IllegalStateException if MYSQL_RANDOM_ROOT_PASSWORD is present
	 * @throws IllegalStateException if no MySQL password is found and empty passwords are
	 * not allowed
	 */
	private String extractPassword(Map<String, String> env) {
		Assert.state(!env.containsKey("MYSQL_RANDOM_ROOT_PASSWORD"), "MYSQL_RANDOM_ROOT_PASSWORD is not supported");
		boolean allowEmpty = env.containsKey("MYSQL_ALLOW_EMPTY_PASSWORD") || env.containsKey("ALLOW_EMPTY_PASSWORD");
		String password = env.get("MYSQL_PASSWORD");
		password = (password != null) ? password : env.get("MYSQL_ROOT_PASSWORD");
		Assert.state(StringUtils.hasLength(password) || allowEmpty, "No MySQL password found");
		return (password != null) ? password : "";
	}

	/**
	 * Extracts the database name from the environment variables.
	 * @param env the map of environment variables
	 * @return the database name
	 * @throws IllegalStateException if no MYSQL_DATABASE is defined
	 */
	private String extractDatabase(Map<String, String> env) {
		String database = env.get("MYSQL_DATABASE");
		Assert.state(database != null, "No MYSQL_DATABASE defined");
		return database;
	}

	/**
	 * Returns the username associated with the current MySqlEnvironment instance.
	 * @return the username
	 */
	String getUsername() {
		return this.username;
	}

	/**
	 * Retrieves the password associated with the current MySqlEnvironment instance.
	 * @return the password as a String
	 */
	String getPassword() {
		return this.password;
	}

	/**
	 * Returns the name of the database.
	 * @return the name of the database
	 */
	String getDatabase() {
		return this.database;
	}

}
