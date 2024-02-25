/*
 * Copyright 2012-2021 the original author or authors.
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

package org.springframework.boot.docs.appendix.configurationmetadata.annotationprocessor.automaticmetadatageneration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MyServerProperties class.
 */
@ConfigurationProperties(prefix = "my.server")
public class MyServerProperties {

	/**
	 * Name of the server.
	 */
	private String name;

	/**
	 * IP address to listen to.
	 */
	private String ip = "127.0.0.1";

	/**
	 * Port to listener to.
	 */
	private int port = 9797;

	// @fold:on // getters/setters ...
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name of the server.
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the IP address of the server.
	 * @return the IP address of the server
	 */
	public String getIp() {
		return this.ip;
	}

	/**
	 * Sets the IP address for the server.
	 * @param ip the IP address to be set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * Returns the port number of the server.
	 * @return the port number
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Sets the port number for the server.
	 * @param port the port number to be set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	// fold:off

}
