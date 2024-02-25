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

package org.springframework.boot.actuate.logging;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.boot.logging.LogFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Web {@link Endpoint @Endpoint} that provides access to an application's log file.
 *
 * @author Johannes Edmeier
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 2.0.0
 */
@WebEndpoint(id = "logfile")
public class LogFileWebEndpoint {

	private static final Log logger = LogFactory.getLog(LogFileWebEndpoint.class);

	private final LogFile logFile;

	private final File externalFile;

	/**
	 * Constructs a new LogFileWebEndpoint with the specified LogFile and externalFile.
	 * @param logFile the LogFile object to be used by the LogFileWebEndpoint
	 * @param externalFile the external File object to be used by the LogFileWebEndpoint
	 */
	public LogFileWebEndpoint(LogFile logFile, File externalFile) {
		this.logFile = logFile;
		this.externalFile = externalFile;
	}

	/**
	 * Retrieves the log file resource and returns it as a Resource object.
	 * @return The log file resource as a Resource object, or null if the resource is not
	 * available or not readable.
	 *
	 * @since 1.0
	 */
	@ReadOperation(produces = "text/plain; charset=UTF-8")
	public Resource logFile() {
		Resource logFileResource = getLogFileResource();
		if (logFileResource == null || !logFileResource.isReadable()) {
			return null;
		}
		return logFileResource;
	}

	/**
	 * Returns the resource for the log file.
	 *
	 * If the external file is set, it returns a FileSystemResource using the external
	 * file path. If the external file is not set, it checks if the log file is null. If
	 * so, it logs a debug message and returns null. If the log file is not null, it
	 * returns a FileSystemResource using the log file path.
	 * @return the resource for the log file, or null if the log file is missing or not
	 * set
	 */
	private Resource getLogFileResource() {
		if (this.externalFile != null) {
			return new FileSystemResource(this.externalFile);
		}
		if (this.logFile == null) {
			logger.debug("Missing 'logging.file.name' or 'logging.file.path' properties");
			return null;
		}
		return new FileSystemResource(this.logFile.toString());
	}

}
