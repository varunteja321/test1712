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

package org.springframework.boot.actuate.endpoint.web.annotation;

import java.util.stream.Collectors;

import org.reactivestreams.Publisher;

import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.annotation.AbstractDiscoveredOperation;
import org.springframework.boot.actuate.endpoint.annotation.DiscoveredOperationMethod;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvoker;
import org.springframework.boot.actuate.endpoint.invoke.OperationParameter;
import org.springframework.boot.actuate.endpoint.invoke.reflect.OperationMethod;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.WebOperationRequestPredicate;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.ClassUtils;

/**
 * A discovered {@link WebOperation web operation}.
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @author Moritz Halbritter
 */
class DiscoveredWebOperation extends AbstractDiscoveredOperation implements WebOperation {

	private static final boolean REACTIVE_STREAMS_PRESENT = ClassUtils.isPresent("org.reactivestreams.Publisher",
			DiscoveredWebOperation.class.getClassLoader());

	private final String id;

	private final boolean blocking;

	private final WebOperationRequestPredicate requestPredicate;

	/**
	 * Creates a new instance of DiscoveredWebOperation with the specified parameters.
	 * @param endpointId the ID of the endpoint
	 * @param operationMethod the discovered operation method
	 * @param invoker the operation invoker
	 * @param requestPredicate the web operation request predicate
	 */
	DiscoveredWebOperation(EndpointId endpointId, DiscoveredOperationMethod operationMethod, OperationInvoker invoker,
			WebOperationRequestPredicate requestPredicate) {
		super(operationMethod, invoker);
		this.id = getId(endpointId, operationMethod);
		this.blocking = getBlocking(operationMethod);
		this.requestPredicate = requestPredicate;
	}

	/**
	 * Returns the ID of the web operation based on the provided endpoint ID and operation
	 * method. The ID is generated by concatenating the endpoint ID with the
	 * dash-separated names of the method parameters that have a selector.
	 * @param endpointId The ID of the endpoint.
	 * @param method The operation method.
	 * @return The ID of the web operation.
	 */
	private String getId(EndpointId endpointId, OperationMethod method) {
		return endpointId + method.getParameters()
			.stream()
			.filter(this::hasSelector)
			.map(this::dashName)
			.collect(Collectors.joining());
	}

	/**
	 * Checks if the given OperationParameter has a Selector annotation.
	 * @param parameter the OperationParameter to check
	 * @return true if the OperationParameter has a Selector annotation, false otherwise
	 */
	private boolean hasSelector(OperationParameter parameter) {
		return parameter.getAnnotation(Selector.class) != null;
	}

	/**
	 * Adds a dash prefix to the name of the given operation parameter.
	 * @param parameter the operation parameter to add the dash prefix to
	 * @return the modified name with a dash prefix
	 */
	private String dashName(OperationParameter parameter) {
		return "-" + parameter.getName();
	}

	/**
	 * Determines if the given operation method is blocking.
	 * @param method the operation method to check
	 * @return {@code true} if the method is blocking, {@code false} otherwise
	 */
	private boolean getBlocking(OperationMethod method) {
		return !REACTIVE_STREAMS_PRESENT || !Publisher.class.isAssignableFrom(method.getMethod().getReturnType());
	}

	/**
	 * Returns the ID of the DiscoveredWebOperation.
	 * @return the ID of the DiscoveredWebOperation
	 */
	@Override
	public String getId() {
		return this.id;
	}

	/**
	 * Returns a boolean value indicating whether the web operation is blocking or not.
	 * @return true if the web operation is blocking, false otherwise.
	 */
	@Override
	public boolean isBlocking() {
		return this.blocking;
	}

	/**
	 * Returns the request predicate for this discovered web operation.
	 * @return the request predicate for this web operation
	 */
	@Override
	public WebOperationRequestPredicate getRequestPredicate() {
		return this.requestPredicate;
	}

	/**
	 * Appends the fields of the DiscoveredWebOperation object to the ToStringCreator.
	 * @param creator the ToStringCreator object to append the fields to
	 */
	@Override
	protected void appendFields(ToStringCreator creator) {
		creator.append("id", this.id)
			.append("blocking", this.blocking)
			.append("requestPredicate", this.requestPredicate);
	}

}
