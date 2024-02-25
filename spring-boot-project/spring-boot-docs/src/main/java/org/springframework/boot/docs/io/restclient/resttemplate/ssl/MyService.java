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

package org.springframework.boot.docs.io.restclient.resttemplate.ssl;

import org.springframework.boot.docs.io.restclient.resttemplate.Details;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * MyService class.
 */
@Service
public class MyService {

	private final RestTemplate restTemplate;

	/**
	 * Constructs a new instance of MyService with the provided RestTemplateBuilder and
	 * SslBundles.
	 * @param restTemplateBuilder the RestTemplateBuilder used to build the RestTemplate
	 * @param sslBundles the SslBundles used to retrieve the SSL bundle
	 */
	public MyService(RestTemplateBuilder restTemplateBuilder, SslBundles sslBundles) {
		this.restTemplate = restTemplateBuilder.setSslBundle(sslBundles.getBundle("mybundle")).build();
	}

	/**
	 * Makes a REST call to retrieve the details of a given name.
	 * @param name the name for which details are to be retrieved
	 * @return the details of the given name
	 */
	public Details someRestCall(String name) {
		return this.restTemplate.getForObject("/{name}/details", Details.class, name);
	}

}
