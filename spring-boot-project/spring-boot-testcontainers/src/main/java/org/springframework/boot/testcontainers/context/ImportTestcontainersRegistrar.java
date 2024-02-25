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

package org.springframework.boot.testcontainers.context;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * {@link ImportBeanDefinitionRegistrar} for
 * {@link ImportTestcontainers @ImportTestcontainers}.
 *
 * @author Phillip Webb
 * @see ContainerFieldsImporter
 * @see DynamicPropertySourceMethodsImporter
 */
class ImportTestcontainersRegistrar implements ImportBeanDefinitionRegistrar {

	private static final String DYNAMIC_PROPERTY_SOURCE_CLASS = "org.springframework.test.context.DynamicPropertySource";

	private final ContainerFieldsImporter containerFieldsImporter;

	private final DynamicPropertySourceMethodsImporter dynamicPropertySourceMethodsImporter;

	/**
	 * Initializes the ImportTestcontainersRegistrar with the given environment.
	 * @param environment the environment to be used
	 */
	ImportTestcontainersRegistrar(Environment environment) {
		this.containerFieldsImporter = new ContainerFieldsImporter();
		this.dynamicPropertySourceMethodsImporter = (!ClassUtils.isPresent(DYNAMIC_PROPERTY_SOURCE_CLASS, null)) ? null
				: new DynamicPropertySourceMethodsImporter(environment);
	}

	/**
	 * Registers bean definitions based on the provided annotation metadata and bean
	 * definition registry.
	 * @param importingClassMetadata the annotation metadata of the importing class
	 * @param registry the bean definition registry
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		MergedAnnotation<ImportTestcontainers> annotation = importingClassMetadata.getAnnotations()
			.get(ImportTestcontainers.class);
		Class<?>[] definitionClasses = annotation.getClassArray(MergedAnnotation.VALUE);
		if (ObjectUtils.isEmpty(definitionClasses)) {
			Class<?> importingClass = ClassUtils.resolveClassName(importingClassMetadata.getClassName(), null);
			definitionClasses = new Class<?>[] { importingClass };
		}
		registerBeanDefinitions(registry, definitionClasses);
	}

	/**
	 * Registers bean definitions for the given definition classes in the specified
	 * registry.
	 * @param registry the BeanDefinitionRegistry to register the bean definitions with
	 * @param definitionClasses the array of definition classes to register bean
	 * definitions for
	 */
	private void registerBeanDefinitions(BeanDefinitionRegistry registry, Class<?>[] definitionClasses) {
		for (Class<?> definitionClass : definitionClasses) {
			this.containerFieldsImporter.registerBeanDefinitions(registry, definitionClass);
			if (this.dynamicPropertySourceMethodsImporter != null) {
				this.dynamicPropertySourceMethodsImporter.registerDynamicPropertySources(registry, definitionClass);
			}
		}
	}

}
