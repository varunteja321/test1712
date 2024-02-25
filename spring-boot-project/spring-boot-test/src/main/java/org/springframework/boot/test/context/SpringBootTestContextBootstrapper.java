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

package org.springframework.boot.test.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.aot.AotTestAttributes;
import org.springframework.test.context.support.DefaultTestContextBootstrapper;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.context.web.WebMergedContextConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link TestContextBootstrapper} for Spring Boot. Provides support for
 * {@link SpringBootTest @SpringBootTest} and may also be used directly or subclassed.
 * Provides the following features over and above {@link DefaultTestContextBootstrapper}:
 * <ul>
 * <li>Uses {@link SpringBootContextLoader} as the
 * {@link #getDefaultContextLoaderClass(Class) default context loader}.</li>
 * <li>Automatically searches for a
 * {@link SpringBootConfiguration @SpringBootConfiguration} when required.</li>
 * <li>Allows custom {@link Environment} {@link #getProperties(Class)} to be defined.</li>
 * <li>Provides support for different {@link WebEnvironment webEnvironment} modes.</li>
 * </ul>
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Brian Clozel
 * @author Madhura Bhave
 * @author Lorenzo Dee
 * @since 1.4.0
 * @see SpringBootTest
 * @see TestConfiguration
 */
public class SpringBootTestContextBootstrapper extends DefaultTestContextBootstrapper {

	private static final String[] WEB_ENVIRONMENT_CLASSES = { "jakarta.servlet.Servlet",
			"org.springframework.web.context.ConfigurableWebApplicationContext" };

	private static final String REACTIVE_WEB_ENVIRONMENT_CLASS = "org.springframework."
			+ "web.reactive.DispatcherHandler";

	private static final String MVC_WEB_ENVIRONMENT_CLASS = "org.springframework.web.servlet.DispatcherServlet";

	private static final String JERSEY_WEB_ENVIRONMENT_CLASS = "org.glassfish.jersey.server.ResourceConfig";

	private static final String ACTIVATE_SERVLET_LISTENER = "org.springframework.test."
			+ "context.web.ServletTestExecutionListener.activateListener";

	private static final Log logger = LogFactory.getLog(SpringBootTestContextBootstrapper.class);

	private final AotTestAttributes aotTestAttributes;

	/**
	 * Constructs a new SpringBootTestContextBootstrapper with the default
	 * AotTestAttributes instance.
	 */
	public SpringBootTestContextBootstrapper() {
		this(AotTestAttributes.getInstance());
	}

	/**
	 * Constructs a new instance of SpringBootTestContextBootstrapper with the specified
	 * AotTestAttributes.
	 * @param aotTestAttributes the AotTestAttributes to be used for initializing the
	 * SpringBootTestContextBootstrapper
	 */
	SpringBootTestContextBootstrapper(AotTestAttributes aotTestAttributes) {
		this.aotTestAttributes = aotTestAttributes;
	}

	/**
	 * Builds the test context for the SpringBootTestContextBootstrapper class.
	 * @return The test context.
	 */
	@Override
	public TestContext buildTestContext() {
		TestContext context = super.buildTestContext();
		verifyConfiguration(context.getTestClass());
		WebEnvironment webEnvironment = getWebEnvironment(context.getTestClass());
		if (webEnvironment == WebEnvironment.MOCK && deduceWebApplicationType() == WebApplicationType.SERVLET) {
			context.setAttribute(ACTIVATE_SERVLET_LISTENER, true);
		}
		else if (webEnvironment != null && webEnvironment.isEmbedded()) {
			context.setAttribute(ACTIVATE_SERVLET_LISTENER, false);
		}
		return context;
	}

	/**
	 * Resolves the context loader for the given test class and configuration attributes
	 * list.
	 * @param testClass the test class
	 * @param configAttributesList the list of configuration attributes
	 * @return the resolved context loader
	 */
	@Override
	protected ContextLoader resolveContextLoader(Class<?> testClass,
			List<ContextConfigurationAttributes> configAttributesList) {
		Class<?>[] classes = getClasses(testClass);
		if (!ObjectUtils.isEmpty(classes)) {
			for (ContextConfigurationAttributes configAttributes : configAttributesList) {
				addConfigAttributesClasses(configAttributes, classes);
			}
		}
		return super.resolveContextLoader(testClass, configAttributesList);
	}

	/**
	 * Adds the given classes to the configuration attributes of the context
	 * configuration.
	 * @param configAttributes the context configuration attributes to modify
	 * @param classes the classes to add to the configuration attributes
	 */
	private void addConfigAttributesClasses(ContextConfigurationAttributes configAttributes, Class<?>[] classes) {
		Set<Class<?>> combined = new LinkedHashSet<>(Arrays.asList(classes));
		if (configAttributes.getClasses() != null) {
			combined.addAll(Arrays.asList(configAttributes.getClasses()));
		}
		configAttributes.setClasses(ClassUtils.toClassArray(combined));
	}

	/**
	 * Returns the default context loader class for the given test class. This method
	 * overrides the default implementation in the parent class.
	 * @param testClass the test class for which the default context loader class is
	 * needed
	 * @return the default context loader class for the given test class
	 */
	@Override
	protected Class<? extends ContextLoader> getDefaultContextLoaderClass(Class<?> testClass) {
		return SpringBootContextLoader.class;
	}

	/**
	 * Processes the merged context configuration by modifying it based on the web
	 * environment and property source properties.
	 * @param mergedConfig the merged context configuration to be processed
	 * @return the modified merged context configuration
	 */
	@Override
	protected MergedContextConfiguration processMergedContextConfiguration(MergedContextConfiguration mergedConfig) {
		Class<?>[] classes = getOrFindConfigurationClasses(mergedConfig);
		List<String> propertySourceProperties = getAndProcessPropertySourceProperties(mergedConfig);
		mergedConfig = createModifiedConfig(mergedConfig, classes, StringUtils.toStringArray(propertySourceProperties));
		WebEnvironment webEnvironment = getWebEnvironment(mergedConfig.getTestClass());
		if (webEnvironment != null && isWebEnvironmentSupported(mergedConfig)) {
			WebApplicationType webApplicationType = getWebApplicationType(mergedConfig);
			if (webApplicationType == WebApplicationType.SERVLET
					&& (webEnvironment.isEmbedded() || webEnvironment == WebEnvironment.MOCK)) {
				mergedConfig = new WebMergedContextConfiguration(mergedConfig, determineResourceBasePath(mergedConfig));
			}
			else if (webApplicationType == WebApplicationType.REACTIVE
					&& (webEnvironment.isEmbedded() || webEnvironment == WebEnvironment.MOCK)) {
				return new ReactiveWebMergedContextConfiguration(mergedConfig);
			}
		}
		return mergedConfig;
	}

	/**
	 * Retrieves the web application type based on the provided merged context
	 * configuration.
	 * @param configuration the merged context configuration
	 * @return the web application type
	 */
	private WebApplicationType getWebApplicationType(MergedContextConfiguration configuration) {
		ConfigurationPropertySource source = new MapConfigurationPropertySource(
				TestPropertySourceUtils.convertInlinedPropertiesToMap(configuration.getPropertySourceProperties()));
		Binder binder = new Binder(source);
		return binder.bind("spring.main.web-application-type", Bindable.of(WebApplicationType.class))
			.orElseGet(this::deduceWebApplicationType);
	}

	/**
	 * Deduces the type of web application based on the presence of certain classes.
	 * @return The type of web application (REACTIVE, NONE, or SERVLET)
	 */
	private WebApplicationType deduceWebApplicationType() {
		if (ClassUtils.isPresent(REACTIVE_WEB_ENVIRONMENT_CLASS, null)
				&& !ClassUtils.isPresent(MVC_WEB_ENVIRONMENT_CLASS, null)
				&& !ClassUtils.isPresent(JERSEY_WEB_ENVIRONMENT_CLASS, null)) {
			return WebApplicationType.REACTIVE;
		}
		for (String className : WEB_ENVIRONMENT_CLASSES) {
			if (!ClassUtils.isPresent(className, null)) {
				return WebApplicationType.NONE;
			}
		}
		return WebApplicationType.SERVLET;
	}

	/**
	 * Determines the resource base path for web applications using the value of
	 * {@link WebAppConfiguration @WebAppConfiguration}, if any, on the test class of the
	 * given {@code configuration}. Defaults to {@code src/main/webapp} in its absence.
	 * @param configuration the configuration to examine
	 * @return the resource base path
	 * @since 2.1.6
	 */
	protected String determineResourceBasePath(MergedContextConfiguration configuration) {
		return MergedAnnotations.from(configuration.getTestClass(), SearchStrategy.TYPE_HIERARCHY)
			.get(WebAppConfiguration.class)
			.getValue(MergedAnnotation.VALUE, String.class)
			.orElse("src/main/webapp");
	}

	/**
	 * Checks if the web environment is supported for the given merged context
	 * configuration.
	 * @param mergedConfig the merged context configuration
	 * @return true if the web environment is supported, false otherwise
	 */
	private boolean isWebEnvironmentSupported(MergedContextConfiguration mergedConfig) {
		Class<?> testClass = mergedConfig.getTestClass();
		ContextHierarchy hierarchy = AnnotationUtils.getAnnotation(testClass, ContextHierarchy.class);
		if (hierarchy == null || hierarchy.value().length == 0) {
			return true;
		}
		ContextConfiguration[] configurations = hierarchy.value();
		return isFromConfiguration(mergedConfig, configurations[configurations.length - 1]);
	}

	/**
	 * Checks if the given {@link MergedContextConfiguration} is from the specified
	 * {@link ContextConfiguration}.
	 * @param candidateConfig the candidate {@link MergedContextConfiguration} to check
	 * @param configuration the {@link ContextConfiguration} to compare against
	 * @return {@code true} if the candidate configuration is from the specified
	 * configuration, {@code false} otherwise
	 */
	private boolean isFromConfiguration(MergedContextConfiguration candidateConfig,
			ContextConfiguration configuration) {
		ContextConfigurationAttributes attributes = new ContextConfigurationAttributes(candidateConfig.getTestClass(),
				configuration);
		Set<Class<?>> configurationClasses = new HashSet<>(Arrays.asList(attributes.getClasses()));
		for (Class<?> candidate : candidateConfig.getClasses()) {
			if (configurationClasses.contains(candidate)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves or finds the configuration classes for the given merged context
	 * configuration.
	 * @param mergedConfig the merged context configuration
	 * @return an array of configuration classes
	 */
	protected Class<?>[] getOrFindConfigurationClasses(MergedContextConfiguration mergedConfig) {
		Class<?>[] classes = mergedConfig.getClasses();
		if (containsNonTestComponent(classes) || mergedConfig.hasLocations()) {
			return classes;
		}
		Class<?> found = findConfigurationClass(mergedConfig.getTestClass());
		logger.info("Found @SpringBootConfiguration " + found.getName() + " for test " + mergedConfig.getTestClass());
		return merge(found, classes);
	}

	/**
	 * Finds the configuration class for the given test class.
	 * @param testClass the test class for which to find the configuration class
	 * @return the configuration class found
	 * @throws IllegalStateException if a configuration class cannot be found
	 */
	private Class<?> findConfigurationClass(Class<?> testClass) {
		String propertyName = "%s.SpringBootConfiguration.%s"
			.formatted(SpringBootTestContextBootstrapper.class.getName(), testClass.getName());
		String foundClassName = this.aotTestAttributes.getString(propertyName);
		if (foundClassName != null) {
			return ClassUtils.resolveClassName(foundClassName, testClass.getClassLoader());
		}
		Class<?> found = new AnnotatedClassFinder(SpringBootConfiguration.class).findFromClass(testClass);
		Assert.state(found != null, "Unable to find a @SpringBootConfiguration, you need to use "
				+ "@ContextConfiguration or @SpringBootTest(classes=...) with your test");
		this.aotTestAttributes.setAttribute(propertyName, found.getName());
		return found;
	}

	/**
	 * Checks if the given array of classes contains any non-test component.
	 * @param classes the array of classes to check
	 * @return {@code true} if the array contains a non-test component, {@code false}
	 * otherwise
	 */
	private boolean containsNonTestComponent(Class<?>[] classes) {
		for (Class<?> candidate : classes) {
			if (!MergedAnnotations.from(candidate, SearchStrategy.INHERITED_ANNOTATIONS)
				.isPresent(TestConfiguration.class)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Merges the given head class with the existing array of classes.
	 * @param head the class to be added at the beginning of the merged array
	 * @param existing the existing array of classes to be merged
	 * @return the merged array of classes with the head class added at the beginning
	 */
	private Class<?>[] merge(Class<?> head, Class<?>[] existing) {
		Class<?>[] result = new Class<?>[existing.length + 1];
		result[0] = head;
		System.arraycopy(existing, 0, result, 1, existing.length);
		return result;
	}

	/**
	 * Retrieves and processes the property source properties from the given merged
	 * context configuration.
	 * @param mergedConfig the merged context configuration
	 * @return the list of property source properties
	 */
	private List<String> getAndProcessPropertySourceProperties(MergedContextConfiguration mergedConfig) {
		List<String> propertySourceProperties = new ArrayList<>(
				Arrays.asList(mergedConfig.getPropertySourceProperties()));
		String differentiator = getDifferentiatorPropertySourceProperty();
		if (differentiator != null) {
			propertySourceProperties.add(differentiator);
		}
		processPropertySourceProperties(mergedConfig, propertySourceProperties);
		return propertySourceProperties;
	}

	/**
	 * Return a "differentiator" property to ensure that there is something to
	 * differentiate regular tests and bootstrapped tests. Without this property a cached
	 * context could be returned that wasn't created by this bootstrapper. By default uses
	 * the bootstrapper class as a property.
	 * @return the differentiator or {@code null}
	 */
	protected String getDifferentiatorPropertySourceProperty() {
		return getClass().getName() + "=true";
	}

	/**
	 * Post process the property source properties, adding or removing elements as
	 * required.
	 * @param mergedConfig the merged context configuration
	 * @param propertySourceProperties the property source properties to process
	 */
	protected void processPropertySourceProperties(MergedContextConfiguration mergedConfig,
			List<String> propertySourceProperties) {
		Class<?> testClass = mergedConfig.getTestClass();
		String[] properties = getProperties(testClass);
		if (!ObjectUtils.isEmpty(properties)) {
			// Added first so that inlined properties from @TestPropertySource take
			// precedence
			propertySourceProperties.addAll(0, Arrays.asList(properties));
		}
		WebEnvironment webEnvironment = getWebEnvironment(testClass);
		if (webEnvironment == WebEnvironment.RANDOM_PORT) {
			propertySourceProperties.add("server.port=0");
		}
		else if (webEnvironment == WebEnvironment.NONE) {
			propertySourceProperties.add("spring.main.web-application-type=none");
		}
	}

	/**
	 * Return the {@link WebEnvironment} type for this test or null if undefined.
	 * @param testClass the source test class
	 * @return the {@link WebEnvironment} or {@code null}
	 */
	protected WebEnvironment getWebEnvironment(Class<?> testClass) {
		SpringBootTest annotation = getAnnotation(testClass);
		return (annotation != null) ? annotation.webEnvironment() : null;
	}

	/**
	 * Retrieves the classes specified in the {@link SpringBootTest} annotation of the
	 * given test class.
	 * @param testClass the test class to retrieve the classes from
	 * @return an array of classes specified in the {@link SpringBootTest} annotation, or
	 * null if the annotation is not present
	 */
	protected Class<?>[] getClasses(Class<?> testClass) {
		SpringBootTest annotation = getAnnotation(testClass);
		return (annotation != null) ? annotation.classes() : null;
	}

	/**
	 * Retrieves the properties from the given test class.
	 * @param testClass the test class to retrieve properties from
	 * @return an array of properties if the SpringBootTest annotation is present on the
	 * test class, otherwise null
	 */
	protected String[] getProperties(Class<?> testClass) {
		SpringBootTest annotation = getAnnotation(testClass);
		return (annotation != null) ? annotation.properties() : null;
	}

	/**
	 * Retrieves the {@link SpringBootTest} annotation from the given test class.
	 * @param testClass the test class to retrieve the annotation from
	 * @return the {@link SpringBootTest} annotation found in the test class, or null if
	 * not found
	 */
	protected SpringBootTest getAnnotation(Class<?> testClass) {
		return TestContextAnnotationUtils.findMergedAnnotation(testClass, SpringBootTest.class);
	}

	/**
	 * Verifies the configuration of the given test class.
	 * @param testClass the test class to verify the configuration for
	 * @throws IllegalStateException if the configuration is invalid
	 */
	protected void verifyConfiguration(Class<?> testClass) {
		SpringBootTest springBootTest = getAnnotation(testClass);
		if (springBootTest != null && isListeningOnPort(springBootTest.webEnvironment())
				&& MergedAnnotations.from(testClass, SearchStrategy.INHERITED_ANNOTATIONS)
					.isPresent(WebAppConfiguration.class)) {
			throw new IllegalStateException("@WebAppConfiguration should only be used "
					+ "with @SpringBootTest when @SpringBootTest is configured with a "
					+ "mock web environment. Please remove @WebAppConfiguration or reconfigure @SpringBootTest.");
		}
	}

	/**
	 * Checks if the web environment is listening on a specific port.
	 * @param webEnvironment the web environment to check
	 * @return true if the web environment is listening on a specific port, false
	 * otherwise
	 */
	private boolean isListeningOnPort(WebEnvironment webEnvironment) {
		return webEnvironment == WebEnvironment.DEFINED_PORT || webEnvironment == WebEnvironment.RANDOM_PORT;
	}

	/**
	 * Create a new {@link MergedContextConfiguration} with different classes.
	 * @param mergedConfig the source config
	 * @param classes the replacement classes
	 * @return a new {@link MergedContextConfiguration}
	 */
	protected final MergedContextConfiguration createModifiedConfig(MergedContextConfiguration mergedConfig,
			Class<?>[] classes) {
		return createModifiedConfig(mergedConfig, classes, mergedConfig.getPropertySourceProperties());
	}

	/**
	 * Create a new {@link MergedContextConfiguration} with different classes and
	 * properties.
	 * @param mergedConfig the source config
	 * @param classes the replacement classes
	 * @param propertySourceProperties the replacement properties
	 * @return a new {@link MergedContextConfiguration}
	 */
	protected final MergedContextConfiguration createModifiedConfig(MergedContextConfiguration mergedConfig,
			Class<?>[] classes, String[] propertySourceProperties) {
		Set<ContextCustomizer> contextCustomizers = new LinkedHashSet<>(mergedConfig.getContextCustomizers());
		contextCustomizers.add(new SpringBootTestAnnotation(mergedConfig.getTestClass()));
		return new MergedContextConfiguration(mergedConfig.getTestClass(), mergedConfig.getLocations(), classes,
				mergedConfig.getContextInitializerClasses(), mergedConfig.getActiveProfiles(),
				mergedConfig.getPropertySourceDescriptors(), propertySourceProperties, contextCustomizers,
				mergedConfig.getContextLoader(), getCacheAwareContextLoaderDelegate(), mergedConfig.getParent());
	}

}
