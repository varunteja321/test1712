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

package org.springframework.boot.cli.command.init;

/**
 * Exception with a message that can be reported to the user.
 *
 * @author Stephane Nicoll
 * @since 1.2.0
 */
public class ReportableException extends RuntimeException {

	/**
	 * Constructs a new ReportableException with the specified detail message.
	 * @param message the detail message (which is saved for later retrieval by the
	 * getMessage() method)
	 */
	public ReportableException(String message) {
		super(message);
	}

	/**
	 * Constructs a new ReportableException with the specified detail message and cause.
	 * @param message the detail message (which is saved for later retrieval by the
	 * getMessage() method).
	 * @param cause the cause (which is saved for later retrieval by the getCause()
	 * method).
	 */
	public ReportableException(String message, Throwable cause) {
		super(message, cause);
	}

}
