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

package org.springframework.boot.docs.data.sql.jdbcclient;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * MyBean class.
 */
@Component
public class MyBean {

	private final JdbcClient jdbcClient;

	/**
	 * Constructs a new instance of MyBean with the specified JdbcClient.
	 * @param jdbcClient the JdbcClient to be used by this MyBean
	 */
	public MyBean(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	/**
	 * Performs a specific action.
	 *
	 * This method deletes all records from the customer table using the JDBC client.
	 *
	 * @chomp:line this.jdbcClient ...
	 */
	public void doSomething() {
		/* @chomp:line this.jdbcClient ... */ this.jdbcClient.sql("delete from customer").update();
	}

}
