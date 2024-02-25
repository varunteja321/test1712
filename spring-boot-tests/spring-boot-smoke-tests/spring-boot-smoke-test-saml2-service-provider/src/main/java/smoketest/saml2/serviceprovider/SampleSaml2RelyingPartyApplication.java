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

package smoketest.saml2.serviceprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SampleSaml2RelyingPartyApplication class.
 */
@SpringBootApplication
public class SampleSaml2RelyingPartyApplication {

	/**
	 * The main method of the SampleSaml2RelyingPartyApplication class.
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(SampleSaml2RelyingPartyApplication.class);
	}

}
