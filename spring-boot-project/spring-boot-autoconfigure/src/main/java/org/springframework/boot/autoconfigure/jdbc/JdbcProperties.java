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

package org.springframework.boot.autoconfigure.jdbc;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

/**
 * Configuration properties for JDBC.
 *
 * @author Kazuki Shimizu
 * @author Stephane Nicoll
 * @since 2.0.0
 */
@ConfigurationProperties(prefix = "spring.jdbc")
public class JdbcProperties {

	private final Template template = new Template();

	/**
	 * Returns the template associated with this JdbcProperties object.
	 * @return the template associated with this JdbcProperties object
	 */
	public Template getTemplate() {
		return this.template;
	}

	/**
	 * {@code JdbcTemplate} settings.
	 */
	public static class Template {

		/**
		 * Number of rows that should be fetched from the database when more rows are
		 * needed. Use -1 to use the JDBC driver's default configuration.
		 */
		private int fetchSize = -1;

		/**
		 * Maximum number of rows. Use -1 to use the JDBC driver's default configuration.
		 */
		private int maxRows = -1;

		/**
		 * Query timeout. Default is to use the JDBC driver's default configuration. If a
		 * duration suffix is not specified, seconds will be used.
		 */
		@DurationUnit(ChronoUnit.SECONDS)
		private Duration queryTimeout;

		/**
		 * Returns the fetch size.
		 * @return the fetch size
		 */
		public int getFetchSize() {
			return this.fetchSize;
		}

		/**
		 * Sets the fetch size for retrieving data from a database.
		 * @param fetchSize the fetch size to be set
		 */
		public void setFetchSize(int fetchSize) {
			this.fetchSize = fetchSize;
		}

		/**
		 * Returns the maximum number of rows.
		 * @return the maximum number of rows
		 */
		public int getMaxRows() {
			return this.maxRows;
		}

		/**
		 * Sets the maximum number of rows for the template.
		 * @param maxRows the maximum number of rows to be set
		 */
		public void setMaxRows(int maxRows) {
			this.maxRows = maxRows;
		}

		/**
		 * Returns the query timeout duration.
		 * @return the query timeout duration
		 */
		public Duration getQueryTimeout() {
			return this.queryTimeout;
		}

		/**
		 * Sets the query timeout for the Template.
		 * @param queryTimeout the duration of the query timeout
		 */
		public void setQueryTimeout(Duration queryTimeout) {
			this.queryTimeout = queryTimeout;
		}

	}

}
