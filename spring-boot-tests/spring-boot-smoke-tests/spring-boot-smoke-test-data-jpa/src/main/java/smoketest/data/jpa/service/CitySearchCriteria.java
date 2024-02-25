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

package smoketest.data.jpa.service;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * CitySearchCriteria class.
 */
public class CitySearchCriteria implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	/**
	 * Constructs a new CitySearchCriteria object.
	 */
	public CitySearchCriteria() {
	}

	/**
	 * Constructs a new CitySearchCriteria object with the specified name.
	 * @param name the name of the city to search for
	 * @throws IllegalArgumentException if the name is null
	 */
	public CitySearchCriteria(String name) {
		Assert.notNull(name, "Name must not be null");
		this.name = name;
	}

	/**
	 * Returns the name of the CitySearchCriteria object.
	 * @return the name of the CitySearchCriteria object
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name of the city search criteria.
	 * @param name the name of the city
	 */
	public void setName(String name) {
		this.name = name;
	}

}
