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

package org.springframework.boot.web.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.util.Assert;

/**
 * Abstract base class for {@link ConfigurableWebServerFactory} implementations.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @author Ivan Sopov
 * @author Eddú Meléndez
 * @author Brian Clozel
 * @author Scott Frederick
 * @since 2.0.0
 */
public abstract class AbstractConfigurableWebServerFactory implements ConfigurableWebServerFactory {

	private int port = 8080;

	private InetAddress address;

	private Set<ErrorPage> errorPages = new LinkedHashSet<>();

	private Ssl ssl;

	private SslBundles sslBundles;

	private Http2 http2;

	private Compression compression;

	private String serverHeader;

	private Shutdown shutdown = Shutdown.IMMEDIATE;

	/**
	 * Create a new {@link AbstractConfigurableWebServerFactory} instance.
	 */
	public AbstractConfigurableWebServerFactory() {
	}

	/**
	 * Create a new {@link AbstractConfigurableWebServerFactory} instance with the
	 * specified port.
	 * @param port the port number for the web server
	 */
	public AbstractConfigurableWebServerFactory(int port) {
		this.port = port;
	}

	/**
	 * The port that the web server listens on.
	 * @return the port
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Sets the port number for the web server.
	 * @param port the port number to set
	 */
	@Override
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Return the address that the web server binds to.
	 * @return the address
	 */
	public InetAddress getAddress() {
		return this.address;
	}

	/**
	 * Sets the address of the web server.
	 * @param address the InetAddress representing the address of the web server
	 */
	@Override
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	/**
	 * Returns a mutable set of {@link ErrorPage ErrorPages} that will be used when
	 * handling exceptions.
	 * @return the error pages
	 */
	public Set<ErrorPage> getErrorPages() {
		return this.errorPages;
	}

	/**
	 * Sets the error pages for this web server factory.
	 * @param errorPages the set of error pages to be set
	 * @throws IllegalArgumentException if the errorPages parameter is null
	 */
	@Override
	public void setErrorPages(Set<? extends ErrorPage> errorPages) {
		Assert.notNull(errorPages, "ErrorPages must not be null");
		this.errorPages = new LinkedHashSet<>(errorPages);
	}

	/**
	 * Adds error pages to the web server factory.
	 * @param errorPages the error pages to be added
	 * @throws IllegalArgumentException if errorPages is null
	 */
	@Override
	public void addErrorPages(ErrorPage... errorPages) {
		Assert.notNull(errorPages, "ErrorPages must not be null");
		this.errorPages.addAll(Arrays.asList(errorPages));
	}

	/**
	 * Returns the SSL configuration of this AbstractConfigurableWebServerFactory.
	 * @return the SSL configuration of this AbstractConfigurableWebServerFactory
	 */
	public Ssl getSsl() {
		return this.ssl;
	}

	/**
	 * Sets the SSL configuration for the web server.
	 * @param ssl the SSL configuration to set
	 */
	@Override
	public void setSsl(Ssl ssl) {
		this.ssl = ssl;
	}

	/**
	 * Return the configured {@link SslBundles}.
	 * @return the {@link SslBundles} or {@code null}
	 * @since 3.2.0
	 */
	public SslBundles getSslBundles() {
		return this.sslBundles;
	}

	/**
	 * Sets the SSL bundles for the web server factory.
	 * @param sslBundles the SSL bundles to be set
	 */
	@Override
	public void setSslBundles(SslBundles sslBundles) {
		this.sslBundles = sslBundles;
	}

	/**
	 * Returns the Http2 object associated with this AbstractConfigurableWebServerFactory.
	 * @return the Http2 object associated with this AbstractConfigurableWebServerFactory
	 */
	public Http2 getHttp2() {
		return this.http2;
	}

	/**
	 * Sets the HTTP/2 configuration for the web server.
	 * @param http2 the HTTP/2 configuration to set
	 */
	@Override
	public void setHttp2(Http2 http2) {
		this.http2 = http2;
	}

	/**
	 * Returns the compression configuration of the web server factory.
	 * @return the compression configuration of the web server factory
	 */
	public Compression getCompression() {
		return this.compression;
	}

	/**
	 * Sets the compression algorithm to be used by the web server.
	 * @param compression the compression algorithm to be used
	 */
	@Override
	public void setCompression(Compression compression) {
		this.compression = compression;
	}

	/**
	 * Returns the server header value.
	 * @return the server header value
	 */
	public String getServerHeader() {
		return this.serverHeader;
	}

	/**
	 * Sets the server header for the web server.
	 * @param serverHeader the server header to be set
	 */
	@Override
	public void setServerHeader(String serverHeader) {
		this.serverHeader = serverHeader;
	}

	/**
	 * Sets the shutdown hook for the web server.
	 * @param shutdown the shutdown hook to be set
	 */
	@Override
	public void setShutdown(Shutdown shutdown) {
		this.shutdown = shutdown;
	}

	/**
	 * Returns the shutdown configuration that will be applied to the server.
	 * @return the shutdown configuration
	 * @since 2.3.0
	 */
	public Shutdown getShutdown() {
		return this.shutdown;
	}

	/**
	 * Return the {@link SslBundle} that should be used with this server.
	 * @return the SSL bundle
	 */
	protected final SslBundle getSslBundle() {
		return WebServerSslBundle.get(this.ssl, this.sslBundles);
	}

	/**
	 * Return the absolute temp dir for given web server.
	 * @param prefix server name
	 * @return the temp dir for given server.
	 */
	protected final File createTempDir(String prefix) {
		try {
			File tempDir = Files.createTempDirectory(prefix + "." + getPort() + ".").toFile();
			tempDir.deleteOnExit();
			return tempDir;
		}
		catch (IOException ex) {
			throw new WebServerException(
					"Unable to create tempDir. java.io.tmpdir is set to " + System.getProperty("java.io.tmpdir"), ex);
		}
	}

}
