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

package org.springframework.boot.actuate.ldap;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.ldap.core.ContextExecutor;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.util.Assert;

/**
 * {@link HealthIndicator} for configured LDAP server(s).
 *
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @since 2.0.0
 */
public class LdapHealthIndicator extends AbstractHealthIndicator {

	private static final ContextExecutor<String> versionContextExecutor = new VersionContextExecutor();

	private final LdapOperations ldapOperations;

	/**
	 * Constructs a new LdapHealthIndicator with the specified LdapOperations.
	 * @param ldapOperations the LdapOperations to be used for health check
	 * @throws IllegalArgumentException if ldapOperations is null
	 */
	public LdapHealthIndicator(LdapOperations ldapOperations) {
		super("LDAP health check failed");
		Assert.notNull(ldapOperations, "LdapOperations must not be null");
		this.ldapOperations = ldapOperations;
	}

	/**
	 * Performs a health check on the LDAP server.
	 * @param builder the Health.Builder object used to build the health status
	 * @throws Exception if an error occurs during the health check
	 */
	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		String version = this.ldapOperations.executeReadOnly(versionContextExecutor);
		builder.up().withDetail("version", version);
	}

	/**
	 * VersionContextExecutor class.
	 */
	private static final class VersionContextExecutor implements ContextExecutor<String> {

		/**
		 * Executes the method with the given context.
		 * @param ctx the directory context
		 * @return the LDAP version as a string
		 * @throws NamingException if there is an error with the naming operation
		 */
		@Override
		public String executeWithContext(DirContext ctx) throws NamingException {
			Object version = ctx.getEnvironment().get("java.naming.ldap.version");
			if (version != null) {
				return (String) version;
			}
			return null;
		}

	}

}
