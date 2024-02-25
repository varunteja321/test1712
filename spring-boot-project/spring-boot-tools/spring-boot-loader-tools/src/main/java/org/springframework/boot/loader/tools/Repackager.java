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

package org.springframework.boot.loader.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.jar.JarFile;

import org.springframework.util.Assert;

/**
 * Utility class that can be used to repackage an archive so that it can be executed using
 * '{@literal java -jar}'.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @author Madhura Bhave
 * @author Scott Frederick
 * @since 1.0.0
 */
public class Repackager extends Packager {

	private boolean backupSource = true;

	/**
	 * Create a new {@link Repackager} instance.
	 * @param source the source archive file to package
	 */
	public Repackager(File source) {
		super(source);
	}

	/**
	 * Writes the signature file if necessary.
	 * @param writtenLibraries the map of written libraries
	 * @param writer the abstract jar writer
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void writeSignatureFileIfNecessary(Map<String, Library> writtenLibraries, AbstractJarWriter writer)
			throws IOException {
		if (getSource().getName().toLowerCase().endsWith(".jar") && hasSignedLibrary(writtenLibraries)) {
			writer.writeEntry("META-INF/BOOT.SF", (entryWriter) -> {
			});
		}
	}

	/**
	 * Checks if any of the libraries in the given map of written libraries are signed.
	 * @param writtenLibraries a map of written libraries, where the key is the library
	 * name and the value is the Library object
	 * @return true if any of the libraries are signed, false otherwise
	 * @throws IOException if an I/O error occurs while checking the libraries
	 */
	private boolean hasSignedLibrary(Map<String, Library> writtenLibraries) throws IOException {
		for (Library library : writtenLibraries.values()) {
			if (!(library instanceof JarModeLibrary) && FileUtils.isSignedJarFile(library.getFile())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets if source files should be backed up when they would be overwritten.
	 * @param backupSource if source files should be backed up
	 */
	public void setBackupSource(boolean backupSource) {
		this.backupSource = backupSource;
	}

	/**
	 * Repackage the source file so that it can be run using '{@literal java -jar}'.
	 * @param libraries the libraries required to run the archive
	 * @throws IOException if the file cannot be repackaged
	 */
	public void repackage(Libraries libraries) throws IOException {
		repackage(getSource(), libraries);
	}

	/**
	 * Repackage to the given destination so that it can be launched using '
	 * {@literal java -jar}'.
	 * @param destination the destination file (may be the same as the source)
	 * @param libraries the libraries required to run the archive
	 * @throws IOException if the file cannot be repackaged
	 */
	public void repackage(File destination, Libraries libraries) throws IOException {
		repackage(destination, libraries, null);
	}

	/**
	 * Repackage to the given destination so that it can be launched using '
	 * {@literal java -jar}'.
	 * @param destination the destination file (may be the same as the source)
	 * @param libraries the libraries required to run the archive
	 * @param launchScript an optional launch script prepended to the front of the jar
	 * @throws IOException if the file cannot be repackaged
	 * @since 1.3.0
	 */
	public void repackage(File destination, Libraries libraries, LaunchScript launchScript) throws IOException {
		repackage(destination, libraries, launchScript, null);
	}

	/**
	 * Repackage to the given destination so that it can be launched using '
	 * {@literal java -jar}'.
	 * @param destination the destination file (may be the same as the source)
	 * @param libraries the libraries required to run the archive
	 * @param launchScript an optional launch script prepended to the front of the jar
	 * @param lastModifiedTime an optional last modified time to apply to the archive and
	 * its contents
	 * @throws IOException if the file cannot be repackaged
	 * @since 2.3.0
	 */
	public void repackage(File destination, Libraries libraries, LaunchScript launchScript, FileTime lastModifiedTime)
			throws IOException {
		Assert.isTrue(destination != null && !destination.isDirectory(), "Invalid destination");
		getLayout(); // get layout early
		destination = destination.getAbsoluteFile();
		File source = getSource();
		if (isAlreadyPackaged() && source.equals(destination)) {
			return;
		}
		File workingSource = source;
		if (source.equals(destination)) {
			workingSource = getBackupFile();
			workingSource.delete();
			renameFile(source, workingSource);
		}
		destination.delete();
		try {
			try (JarFile sourceJar = new JarFile(workingSource)) {
				repackage(sourceJar, destination, libraries, launchScript, lastModifiedTime);
			}
		}
		finally {
			if (!this.backupSource && !source.equals(workingSource)) {
				deleteFile(workingSource);
			}
		}
	}

	/**
	 * Repackages a JAR file with the specified source JAR, destination file, libraries,
	 * launch script, and last modified time.
	 * @param sourceJar the source JAR file to be repackaged
	 * @param destination the destination file where the repackaged JAR will be saved
	 * @param libraries the libraries to be included in the repackaged JAR
	 * @param launchScript the launch script to be included in the repackaged JAR
	 * @param lastModifiedTime the last modified time of the repackaged JAR file
	 * @throws IOException if an I/O error occurs during the repackaging process
	 */
	private void repackage(JarFile sourceJar, File destination, Libraries libraries, LaunchScript launchScript,
			FileTime lastModifiedTime) throws IOException {
		try (JarWriter writer = new JarWriter(destination, launchScript, lastModifiedTime)) {
			write(sourceJar, libraries, writer, lastModifiedTime != null);
		}
		if (lastModifiedTime != null) {
			destination.setLastModified(lastModifiedTime.toMillis());
		}
	}

	/**
	 * Renames a file to a specified destination.
	 * @param file the file to be renamed
	 * @param dest the destination file name
	 * @throws IllegalStateException if unable to rename the file
	 */
	private void renameFile(File file, File dest) {
		if (!file.renameTo(dest)) {
			throw new IllegalStateException("Unable to rename '" + file + "' to '" + dest + "'");
		}
	}

	/**
	 * Deletes the specified file.
	 * @param file the file to be deleted
	 * @throws IllegalStateException if unable to delete the file
	 */
	private void deleteFile(File file) {
		if (!file.delete()) {
			throw new IllegalStateException("Unable to delete '" + file + "'");
		}
	}

}
