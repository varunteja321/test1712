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

package org.springframework.boot.docs.web.reactive.webflux;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MyRestController class.
 */
@RestController
@RequestMapping("/users")
public class MyRestController {

	private final UserRepository userRepository;

	private final CustomerRepository customerRepository;

	/**
	 * Constructs a new instance of MyRestController with the specified UserRepository and
	 * CustomerRepository.
	 * @param userRepository the UserRepository to be used by the MyRestController
	 * @param customerRepository the CustomerRepository to be used by the MyRestController
	 */
	public MyRestController(UserRepository userRepository, CustomerRepository customerRepository) {
		this.userRepository = userRepository;
		this.customerRepository = customerRepository;
	}

	/**
	 * Retrieves a user with the given userId.
	 * @param userId the ID of the user to retrieve
	 * @return a Mono containing the user with the given userId
	 */
	@GetMapping("/{userId}")
	public Mono<User> getUser(@PathVariable Long userId) {
		return this.userRepository.findById(userId);
	}

	/**
	 * Retrieves the customers associated with a specific user.
	 * @param userId the ID of the user
	 * @return a Flux of Customer objects representing the customers associated with the
	 * user
	 */
	@GetMapping("/{userId}/customers")
	public Flux<Customer> getUserCustomers(@PathVariable Long userId) {
		return this.userRepository.findById(userId).flatMapMany(this.customerRepository::findByUser);
	}

	/**
	 * Deletes a user with the given userId.
	 * @param userId the ID of the user to be deleted
	 * @return a Mono representing the completion of the deletion operation
	 */
	@DeleteMapping("/{userId}")
	public Mono<Void> deleteUser(@PathVariable Long userId) {
		return this.userRepository.deleteById(userId);
	}

}
