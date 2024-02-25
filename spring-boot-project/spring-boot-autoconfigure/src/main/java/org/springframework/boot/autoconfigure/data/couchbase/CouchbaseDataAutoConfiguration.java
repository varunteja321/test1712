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

package org.springframework.boot.autoconfigure.data.couchbase;

import com.couchbase.client.java.Bucket;
import jakarta.validation.Validator;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.couchbase.core.mapping.event.ValidatingCouchbaseEventListener;
import org.springframework.data.couchbase.repository.CouchbaseRepository;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Data's Couchbase support.
 *
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @since 1.4.0
 */
@AutoConfiguration(after = { CouchbaseAutoConfiguration.class, ValidationAutoConfiguration.class })
@ConditionalOnClass({ Bucket.class, CouchbaseRepository.class })
@EnableConfigurationProperties(CouchbaseDataProperties.class)
@Import({ CouchbaseDataConfiguration.class, CouchbaseClientFactoryConfiguration.class,
		CouchbaseClientFactoryDependentConfiguration.class })
public class CouchbaseDataAutoConfiguration {

	/**
	 * ValidationConfiguration class.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(Validator.class)
	public static class ValidationConfiguration {

		/**
		 * Creates a ValidatingCouchbaseEventListener bean if a single candidate of type
		 * Validator is available.
		 * @param validator the Validator bean to be used for validation
		 * @return the ValidatingCouchbaseEventListener bean
		 */
		@Bean
		@ConditionalOnSingleCandidate(Validator.class)
		public ValidatingCouchbaseEventListener validationEventListener(Validator validator) {
			return new ValidatingCouchbaseEventListener(validator);
		}

	}

}
