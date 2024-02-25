/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.autoconfigure.web.format;

import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

import org.springframework.util.StringUtils;

/**
 * {@link DateTimeFormatter Formatters} for dates, times, and date-times.
 *
 * @author Andy Wilkinson
 * @author Gaurav Pareek
 * @since 2.3.0
 */
public class DateTimeFormatters {

	private DateTimeFormatter dateFormatter;

	private String datePattern;

	private DateTimeFormatter timeFormatter;

	private DateTimeFormatter dateTimeFormatter;

	/**
	 * Configures the date format using the given {@code pattern}.
	 * @param pattern the pattern for formatting dates
	 * @return {@code this} for chained method invocation
	 */
	public DateTimeFormatters dateFormat(String pattern) {
		if (isIso(pattern)) {
			this.dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
			this.datePattern = "yyyy-MM-dd";
		}
		else {
			this.dateFormatter = formatter(pattern);
			this.datePattern = pattern;
		}
		return this;
	}

	/**
	 * Configures the time format using the given {@code pattern}.
	 * @param pattern the pattern for formatting times
	 * @return {@code this} for chained method invocation
	 */
	public DateTimeFormatters timeFormat(String pattern) {
		this.timeFormatter = isIso(pattern) ? DateTimeFormatter.ISO_LOCAL_TIME
				: (isIsoOffset(pattern) ? DateTimeFormatter.ISO_OFFSET_TIME : formatter(pattern));
		return this;
	}

	/**
	 * Configures the date-time format using the given {@code pattern}.
	 * @param pattern the pattern for formatting date-times
	 * @return {@code this} for chained method invocation
	 */
	public DateTimeFormatters dateTimeFormat(String pattern) {
		this.dateTimeFormatter = isIso(pattern) ? DateTimeFormatter.ISO_LOCAL_DATE_TIME
				: (isIsoOffset(pattern) ? DateTimeFormatter.ISO_OFFSET_DATE_TIME : formatter(pattern));
		return this;
	}

	/**
	 * Returns the date formatter used by this DateTimeFormatters instance.
	 * @return the date formatter used by this DateTimeFormatters instance
	 */
	DateTimeFormatter getDateFormatter() {
		return this.dateFormatter;
	}

	/**
	 * Returns the date pattern used by this DateTimeFormatters instance.
	 * @return the date pattern
	 */
	String getDatePattern() {
		return this.datePattern;
	}

	/**
	 * Returns the time formatter used by this DateTimeFormatters instance.
	 * @return the time formatter
	 */
	DateTimeFormatter getTimeFormatter() {
		return this.timeFormatter;
	}

	/**
	 * Returns the DateTimeFormatter object used by this DateTimeFormatters instance.
	 * @return the DateTimeFormatter object used by this DateTimeFormatters instance
	 */
	DateTimeFormatter getDateTimeFormatter() {
		return this.dateTimeFormatter;
	}

	/**
	 * Returns true if any of the formatters (dateFormatter, timeFormatter,
	 * dateTimeFormatter) are customized.
	 * @return true if any of the formatters are customized, false otherwise
	 */
	boolean isCustomized() {
		return this.dateFormatter != null || this.timeFormatter != null || this.dateTimeFormatter != null;
	}

	/**
	 * Returns a DateTimeFormatter object based on the provided pattern.
	 * @param pattern the pattern to be used for formatting the date and time
	 * @return a DateTimeFormatter object with the specified pattern, or null if the
	 * pattern is empty or null
	 */
	private static DateTimeFormatter formatter(String pattern) {
		return StringUtils.hasText(pattern)
				? DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.SMART) : null;
	}

	/**
	 * Checks if the given pattern is equal to "iso" (case insensitive).
	 * @param pattern the pattern to be checked
	 * @return true if the pattern is equal to "iso", false otherwise
	 */
	private static boolean isIso(String pattern) {
		return "iso".equalsIgnoreCase(pattern);
	}

	/**
	 * Checks if the given pattern is an ISO offset pattern.
	 * @param pattern the pattern to check
	 * @return true if the pattern is an ISO offset pattern, false otherwise
	 */
	private static boolean isIsoOffset(String pattern) {
		return "isooffset".equalsIgnoreCase(pattern) || "iso-offset".equalsIgnoreCase(pattern);
	}

}
