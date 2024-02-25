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

package org.springframework.boot.loader.launch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.Manifest;

/**
 * {@link Archive} implementation backed by an exploded archive directory.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Madhura Bhave
 */
class ExplodedArchive implements Archive {

	private static final Object NO_MANIFEST = new Object();

	private static final Set<String> SKIPPED_NAMES = Set.of(".", "..");

	private static final Comparator<File> entryComparator = Comparator.comparing(File::getAbsolutePath);

	private final File rootDirectory;

	private final String rootUriPath;

	private volatile Object manifest;

	/**
	 * Create a new {@link ExplodedArchive} instance.
	 * @param rootDirectory the root directory
	 */
	ExplodedArchive(File rootDirectory) {
		if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
			throw new IllegalArgumentException("Invalid source directory " + rootDirectory);
		}
		this.rootDirectory = rootDirectory;
		this.rootUriPath = ExplodedArchive.this.rootDirectory.toURI().getPath();
	}

	/**
	 * Retrieves the manifest of the exploded archive.
	 * @return The manifest of the exploded archive, or null if no manifest is found.
	 * @throws IOException If an I/O error occurs while loading the manifest.
	 */
	@Override
	public Manifest getManifest() throws IOException {
		Object manifest = this.manifest;
		if (manifest == null) {
			manifest = loadManifest();
			this.manifest = manifest;
		}
		return (manifest != NO_MANIFEST) ? (Manifest) manifest : null;
	}

	/**
	 * Loads the manifest file from the root directory of the exploded archive.
	 * @return the loaded manifest object
	 * @throws IOException if an I/O error occurs while reading the manifest file
	 */
	private Object loadManifest() throws IOException {
		File file = new File(this.rootDirectory, "META-INF/MANIFEST.MF");
		if (!file.exists()) {
			return NO_MANIFEST;
		}
		try (FileInputStream inputStream = new FileInputStream(file)) {
			return new Manifest(inputStream);
		}
	}

	/**
	 * Retrieves the classpath URLs for the entries in the ExplodedArchive.
	 * @param includeFilter a Predicate to filter the entries to be included in the
	 * classpath URLs
	 * @param directorySearchFilter a Predicate to filter the directories to be searched
	 * for entries
	 * @return a Set of URLs representing the classpath URLs
	 * @throws IOException if an I/O error occurs while retrieving the classpath URLs
	 */
	@Override
	public Set<URL> getClassPathUrls(Predicate<Entry> includeFilter, Predicate<Entry> directorySearchFilter)
			throws IOException {
		Set<URL> urls = new LinkedHashSet<>();
		LinkedList<File> files = new LinkedList<>(listFiles(this.rootDirectory));
		while (!files.isEmpty()) {
			File file = files.poll();
			if (SKIPPED_NAMES.contains(file.getName())) {
				continue;
			}
			String entryName = file.toURI().getPath().substring(this.rootUriPath.length());
			Entry entry = new FileArchiveEntry(entryName, file);
			if (entry.isDirectory() && directorySearchFilter.test(entry)) {
				files.addAll(0, listFiles(file));
			}
			if (includeFilter.test(entry)) {
				urls.add(file.toURI().toURL());
			}
		}
		return urls;
	}

	/**
	 * Returns a list of files in the specified directory.
	 * @param file the directory to list files from
	 * @return a list of files in the directory, or an empty list if the directory is
	 * empty or does not exist
	 */
	private List<File> listFiles(File file) {
		File[] files = file.listFiles();
		if (files == null) {
			return Collections.emptyList();
		}
		Arrays.sort(files, entryComparator);
		return Arrays.asList(files);
	}

	/**
	 * Returns the root directory of the ExplodedArchive.
	 * @return the root directory of the ExplodedArchive
	 */
	@Override
	public File getRootDirectory() {
		return this.rootDirectory;
	}

	/**
	 * Returns a string representation of the root directory of the ExplodedArchive.
	 * @return a string representation of the root directory
	 */
	@Override
	public String toString() {
		return this.rootDirectory.toString();
	}

	/**
	 * {@link Entry} backed by a File.
	 */
	private record FileArchiveEntry(String name, File file) implements Entry {

		/**
		 * Returns a boolean value indicating whether the current file is a directory.
		 * @return true if the current file is a directory, false otherwise
		 */
		@Override
		public boolean isDirectory() {
			return this.file.isDirectory();
		}

	}

}
