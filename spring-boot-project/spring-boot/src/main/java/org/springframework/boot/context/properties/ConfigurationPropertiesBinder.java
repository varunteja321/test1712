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

package org.springframework.boot.context.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.AbstractBindHandler;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Bindable.BindRestriction;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.BoundPropertiesTrackingBindHandler;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.bind.handler.IgnoreTopLevelConverterNotFoundBindHandler;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;
import org.springframework.boot.context.properties.bind.validation.ValidationBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.UnboundElementsSourceFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.PropertySources;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

/**
 * Internal class used by the {@link ConfigurationPropertiesBindingPostProcessor} to
 * handle the actual {@link ConfigurationProperties @ConfigurationProperties} binding.
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 */
class ConfigurationPropertiesBinder {

	private static final String BEAN_NAME = "org.springframework.boot.context.internalConfigurationPropertiesBinder";

	private static final String VALIDATOR_BEAN_NAME = EnableConfigurationProperties.VALIDATOR_BEAN_NAME;

	private final ApplicationContext applicationContext;

	private final PropertySources propertySources;

	private final Validator configurationPropertiesValidator;

	private final boolean jsr303Present;

	private volatile Validator jsr303Validator;

	private volatile Binder binder;

	/**
	 * Constructs a new ConfigurationPropertiesBinder with the given ApplicationContext.
	 * @param applicationContext the ApplicationContext to bind configuration properties
	 * to
	 */
	ConfigurationPropertiesBinder(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.propertySources = new PropertySourcesDeducer(applicationContext).getPropertySources();
		this.configurationPropertiesValidator = getConfigurationPropertiesValidator(applicationContext);
		this.jsr303Present = ConfigurationPropertiesJsr303Validator.isJsr303Present(applicationContext);
	}

	/**
	 * Binds the given ConfigurationPropertiesBean to its corresponding target object.
	 * @param propertiesBean the ConfigurationPropertiesBean to bind
	 * @return the BindResult containing the bound target object
	 */
	BindResult<?> bind(ConfigurationPropertiesBean propertiesBean) {
		Bindable<?> target = propertiesBean.asBindTarget();
		ConfigurationProperties annotation = propertiesBean.getAnnotation();
		BindHandler bindHandler = getBindHandler(target, annotation);
		return getBinder().bind(annotation.prefix(), target, bindHandler);
	}

	/**
	 * Binds or creates an object based on the provided ConfigurationPropertiesBean.
	 * @param propertiesBean The ConfigurationPropertiesBean containing the properties to
	 * bind or create an object from.
	 * @return The bound or created object.
	 */
	Object bindOrCreate(ConfigurationPropertiesBean propertiesBean) {
		Bindable<?> target = propertiesBean.asBindTarget();
		ConfigurationProperties annotation = propertiesBean.getAnnotation();
		BindHandler bindHandler = getBindHandler(target, annotation);
		return getBinder().bindOrCreate(annotation.prefix(), target, bindHandler);
	}

	/**
	 * Retrieves the validator bean from the application context based on the provided
	 * bean name.
	 * @param applicationContext the application context to retrieve the validator bean
	 * from
	 * @return the validator bean if found, otherwise null
	 */
	private Validator getConfigurationPropertiesValidator(ApplicationContext applicationContext) {
		if (applicationContext.containsBean(VALIDATOR_BEAN_NAME)) {
			return applicationContext.getBean(VALIDATOR_BEAN_NAME, Validator.class);
		}
		return null;
	}

	/**
	 * Returns the bind handler for the given target and annotation.
	 * @param target the bindable target
	 * @param annotation the configuration properties annotation
	 * @return the bind handler
	 * @param <T> the type of the bindable target
	 */
	private <T> BindHandler getBindHandler(Bindable<T> target, ConfigurationProperties annotation) {
		List<Validator> validators = getValidators(target);
		BindHandler handler = getHandler();
		handler = new ConfigurationPropertiesBindHandler(handler);
		if (annotation.ignoreInvalidFields()) {
			handler = new IgnoreErrorsBindHandler(handler);
		}
		if (!annotation.ignoreUnknownFields()) {
			UnboundElementsSourceFilter filter = new UnboundElementsSourceFilter();
			handler = new NoUnboundElementsBindHandler(handler, filter);
		}
		if (!validators.isEmpty()) {
			handler = new ValidationBindHandler(handler, validators.toArray(new Validator[0]));
		}
		for (ConfigurationPropertiesBindHandlerAdvisor advisor : getBindHandlerAdvisors()) {
			handler = advisor.apply(handler);
		}
		return handler;
	}

	/**
	 * Returns the handler for ignoring top-level converter not found bind errors.
	 * @return the handler for ignoring top-level converter not found bind errors
	 */
	private IgnoreTopLevelConverterNotFoundBindHandler getHandler() {
		BoundConfigurationProperties bound = BoundConfigurationProperties.get(this.applicationContext);
		return (bound != null)
				? new IgnoreTopLevelConverterNotFoundBindHandler(new BoundPropertiesTrackingBindHandler(bound::add))
				: new IgnoreTopLevelConverterNotFoundBindHandler();
	}

	/**
	 * Retrieves a list of validators for the given target object.
	 * @param target the target object to retrieve validators for
	 * @return a list of validators
	 */
	private List<Validator> getValidators(Bindable<?> target) {
		List<Validator> validators = new ArrayList<>(3);
		if (this.configurationPropertiesValidator != null) {
			validators.add(this.configurationPropertiesValidator);
		}
		if (this.jsr303Present && target.getAnnotation(Validated.class) != null) {
			validators.add(getJsr303Validator());
		}
		Validator selfValidator = getSelfValidator(target);
		if (selfValidator != null) {
			validators.add(selfValidator);
		}
		return validators;
	}

	/**
	 * Retrieves the self validator for the given target.
	 * @param target the target bindable object
	 * @return the self validator if found, otherwise null
	 */
	private Validator getSelfValidator(Bindable<?> target) {
		if (target.getValue() != null) {
			Object value = target.getValue().get();
			return (value instanceof Validator validator) ? validator : null;
		}
		Class<?> type = target.getType().resolve();
		if (Validator.class.isAssignableFrom(type)) {
			return new SelfValidatingConstructorBoundBindableValidator(type);
		}
		return null;
	}

	/**
	 * Returns the JSR 303 validator for configuration properties. If the validator is not
	 * already initialized, it creates a new instance of
	 * ConfigurationPropertiesJsr303Validator using the application context.
	 * @return the JSR 303 validator for configuration properties
	 */
	private Validator getJsr303Validator() {
		if (this.jsr303Validator == null) {
			this.jsr303Validator = new ConfigurationPropertiesJsr303Validator(this.applicationContext);
		}
		return this.jsr303Validator;
	}

	/**
	 * Retrieves the list of ConfigurationPropertiesBindHandlerAdvisor instances.
	 * @return The list of ConfigurationPropertiesBindHandlerAdvisor instances.
	 */
	private List<ConfigurationPropertiesBindHandlerAdvisor> getBindHandlerAdvisors() {
		return this.applicationContext.getBeanProvider(ConfigurationPropertiesBindHandlerAdvisor.class)
			.orderedStream()
			.toList();
	}

	/**
	 * Returns the binder instance for configuration properties. If the binder instance is
	 * null, it creates a new binder instance using the provided configuration property
	 * sources, property sources placeholders resolver, conversion services, property
	 * editor initializer, and null for the validator and message interpolator.
	 * @return the binder instance for configuration properties
	 */
	private Binder getBinder() {
		if (this.binder == null) {
			this.binder = new Binder(getConfigurationPropertySources(), getPropertySourcesPlaceholdersResolver(),
					getConversionServices(), getPropertyEditorInitializer(), null, null);
		}
		return this.binder;
	}

	/**
	 * Returns an iterable of ConfigurationPropertySource objects.
	 * @return an iterable of ConfigurationPropertySource objects
	 */
	private Iterable<ConfigurationPropertySource> getConfigurationPropertySources() {
		return ConfigurationPropertySources.from(this.propertySources);
	}

	/**
	 * Returns a new instance of PropertySourcesPlaceholdersResolver initialized with the
	 * property sources.
	 * @return a new instance of PropertySourcesPlaceholdersResolver
	 */
	private PropertySourcesPlaceholdersResolver getPropertySourcesPlaceholdersResolver() {
		return new PropertySourcesPlaceholdersResolver(this.propertySources);
	}

	/**
	 * Retrieves the list of conversion services.
	 * @return the list of conversion services
	 */
	private List<ConversionService> getConversionServices() {
		return new ConversionServiceDeducer(this.applicationContext).getConversionServices();
	}

	/**
	 * Returns a {@code Consumer} that initializes the property editor registry.
	 * @return the property editor initializer, or {@code null} if the application context
	 * is not a {@code ConfigurableApplicationContext}
	 */
	private Consumer<PropertyEditorRegistry> getPropertyEditorInitializer() {
		if (this.applicationContext instanceof ConfigurableApplicationContext configurableContext) {
			return configurableContext.getBeanFactory()::copyRegisteredEditorsTo;
		}
		return null;
	}

	/**
	 * Registers the ConfigurationPropertiesBinderFactory bean definition in the given
	 * registry. If the registry does not already contain a bean definition with the
	 * specified bean name, a new bean definition is created and registered with the
	 * specified bean name. The bean definition is set to have the infrastructure role.
	 * @param registry the BeanDefinitionRegistry to register the bean definition in
	 */
	static void register(BeanDefinitionRegistry registry) {
		if (!registry.containsBeanDefinition(BEAN_NAME)) {
			BeanDefinition definition = BeanDefinitionBuilder
				.rootBeanDefinition(ConfigurationPropertiesBinderFactory.class)
				.getBeanDefinition();
			definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			registry.registerBeanDefinition(ConfigurationPropertiesBinder.BEAN_NAME, definition);
		}
	}

	/**
	 * Retrieves the ConfigurationPropertiesBinder instance from the specified
	 * BeanFactory.
	 * @param beanFactory the BeanFactory from which to retrieve the
	 * ConfigurationPropertiesBinder instance
	 * @return the ConfigurationPropertiesBinder instance retrieved from the BeanFactory
	 */
	static ConfigurationPropertiesBinder get(BeanFactory beanFactory) {
		return beanFactory.getBean(BEAN_NAME, ConfigurationPropertiesBinder.class);
	}

	/**
	 * {@link BindHandler} to deal with
	 * {@link ConfigurationProperties @ConfigurationProperties} concerns.
	 */
	private static class ConfigurationPropertiesBindHandler extends AbstractBindHandler {

		/**
		 * Constructs a new ConfigurationPropertiesBindHandler with the specified
		 * BindHandler.
		 * @param handler the BindHandler to be used
		 */
		ConfigurationPropertiesBindHandler(BindHandler handler) {
			super(handler);
		}

		/**
		 * This method is called when the binding process starts for a specific
		 * configuration property. It checks if the target type is a configuration
		 * properties class and applies bind restrictions accordingly.
		 * @param name the name of the configuration property being bound
		 * @param target the target bindable object
		 * @param context the bind context
		 * @return the modified bindable object with bind restrictions applied if
		 * necessary
		 */
		@Override
		public <T> Bindable<T> onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
			return isConfigurationProperties(target.getType().resolve())
					? target.withBindRestrictions(BindRestriction.NO_DIRECT_PROPERTY) : target;
		}

		/**
		 * Checks if the given target class is annotated with
		 * {@link ConfigurationProperties}.
		 * @param target the target class to check
		 * @return {@code true} if the target class is annotated with
		 * {@link ConfigurationProperties}, {@code false} otherwise
		 */
		private boolean isConfigurationProperties(Class<?> target) {
			return target != null && MergedAnnotations.from(target).isPresent(ConfigurationProperties.class);
		}

	}

	/**
	 * {@link FactoryBean} to create the {@link ConfigurationPropertiesBinder}.
	 */
	static class ConfigurationPropertiesBinderFactory
			implements FactoryBean<ConfigurationPropertiesBinder>, ApplicationContextAware {

		private ConfigurationPropertiesBinder binder;

		/**
		 * Sets the application context for this ConfigurationPropertiesBinderFactory.
		 * @param applicationContext the application context to set
		 * @throws BeansException if an error occurs while setting the application context
		 */
		@Override
		public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
			this.binder = (this.binder != null) ? this.binder : new ConfigurationPropertiesBinder(applicationContext);
		}

		/**
		 * Returns the type of object that is created by this factory.
		 * @return the type of object created by this factory, which is
		 * ConfigurationPropertiesBinder class.
		 */
		@Override
		public Class<?> getObjectType() {
			return ConfigurationPropertiesBinder.class;
		}

		/**
		 * Retrieves the ConfigurationPropertiesBinder object.
		 * @return The ConfigurationPropertiesBinder object.
		 * @throws Exception If the binder is not created due to missing
		 * setApplicationContext call.
		 */
		@Override
		public ConfigurationPropertiesBinder getObject() throws Exception {
			Assert.state(this.binder != null, "Binder was not created due to missing setApplicationContext call");
			return this.binder;
		}

	}

	/**
	 * A {@code Validator} for a constructor-bound {@code Bindable} where the type being
	 * bound is itself a {@code Validator} implementation.
	 */
	static class SelfValidatingConstructorBoundBindableValidator implements Validator {

		private final Class<?> type;

		/**
		 * Constructs a new SelfValidatingConstructorBoundBindableValidator with the
		 * specified type.
		 * @param type the class type to be validated
		 */
		SelfValidatingConstructorBoundBindableValidator(Class<?> type) {
			this.type = type;
		}

		/**
		 * Determines if the specified class is supported by this validator.
		 * @param candidate the class to be checked
		 * @return true if the specified class is supported, false otherwise
		 */
		@Override
		public boolean supports(Class<?> candidate) {
			return candidate.isAssignableFrom(this.type);
		}

		/**
		 * Validates the given target object using the specified Errors object.
		 * @param target the object to be validated
		 * @param errors the Errors object to store any validation errors
		 */
		@Override
		public void validate(Object target, Errors errors) {
			((Validator) target).validate(target, errors);
		}

	}

}
