/*
 * Copyright 2021-2023 the original author or authors.
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

package org.springframework.boot.build;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/**
 * Tasks for syncing the source code of a Spring Boot application, filtering its
 * {@code build.gradle} to set the version of its {@code org.springframework.boot} plugin.
 *
 * @author Andy Wilkinson
 */
public class SyncAppSource extends DefaultTask {

	private final DirectoryProperty sourceDirectory;

	private final DirectoryProperty destinationDirectory;

	private final Property<String> pluginVersion;

	/**
	 * Initializes a new instance of the SyncAppSource class.
	 *
	 * This method sets up the source and destination directories for the synchronization
	 * process. It also sets the plugin version property to the current project version.
	 */
	public SyncAppSource() {
		ObjectFactory objects = getProject().getObjects();
		this.sourceDirectory = objects.directoryProperty();
		this.destinationDirectory = objects.directoryProperty();
		this.pluginVersion = objects.property(String.class)
			.convention(getProject().provider(() -> getProject().getVersion().toString()));
	}

	/**
	 * Synchronizes the application sources by copying files from the source directory to
	 * the destination directory. Filters the copied files to replace the version of the
	 * "org.springframework.boot" dependency with the current project version.
	 */
	@TaskAction
	void syncAppSources() {
		getProject().sync((copySpec) -> {
			copySpec.from(this.sourceDirectory);
			copySpec.into(this.destinationDirectory);
			copySpec.filter((line) -> line.replace("id \"org.springframework.boot\"",
					"id \"org.springframework.boot\" version \"" + getProject().getVersion() + "\""));
		});
	}

	/**
	 * Returns the source directory for the SyncApp.
	 * @return the source directory
	 */
	@InputDirectory
	public DirectoryProperty getSourceDirectory() {
		return this.sourceDirectory;
	}

	/**
	 * Returns the destination directory where the output should be stored.
	 * @return The destination directory.
	 */
	@OutputDirectory
	public DirectoryProperty getDestinationDirectory() {
		return this.destinationDirectory;
	}

	/**
	 * Returns the plugin version property.
	 * @return the plugin version property
	 */
	@Input
	public Property<String> getPluginVersion() {
		return this.pluginVersion;
	}

}
