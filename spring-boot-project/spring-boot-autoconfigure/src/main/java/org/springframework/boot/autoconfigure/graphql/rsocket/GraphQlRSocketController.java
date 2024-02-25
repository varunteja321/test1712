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

package org.springframework.boot.autoconfigure.graphql.rsocket;

import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.graphql.server.GraphQlRSocketHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * GraphQlRSocketController class.
 */
@Controller
class GraphQlRSocketController {

	private final GraphQlRSocketHandler handler;

	/**
	 * Constructs a new GraphQlRSocketController with the specified GraphQlRSocketHandler.
	 * @param handler the GraphQlRSocketHandler to be used by the controller
	 */
	GraphQlRSocketController(GraphQlRSocketHandler handler) {
		this.handler = handler;
	}

	/**
	 * Handles the incoming payload and returns a Mono of Map containing the response.
	 * @param payload the incoming payload to be handled
	 * @return a Mono of Map containing the response
	 */
	@MessageMapping("${spring.graphql.rsocket.mapping}")
	Mono<Map<String, Object>> handle(Map<String, Object> payload) {
		return this.handler.handle(payload);
	}

	/**
	 * Handles the subscription request for the given payload.
	 * @param payload the payload containing the subscription request
	 * @return a Flux of Map objects representing the subscription response
	 */
	@MessageMapping("${spring.graphql.rsocket.mapping}")
	Flux<Map<String, Object>> handleSubscription(Map<String, Object> payload) {
		return this.handler.handleSubscription(payload);
	}

}
