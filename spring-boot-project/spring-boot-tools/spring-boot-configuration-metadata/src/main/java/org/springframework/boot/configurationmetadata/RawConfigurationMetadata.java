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

package org.springframework.boot.configurationmetadata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A raw metadata structure. Used to initialize a {@link ConfigurationMetadataRepository}.
 *
 * @author Stephane Nicoll
 */
class RawConfigurationMetadata {

	private final List<ConfigurationMetadataSource> sources;

	private final List<ConfigurationMetadataItem> items;

	private final List<ConfigurationMetadataHint> hints;

	/**
	 * Constructs a new instance of RawConfigurationMetadata with the specified sources,
	 * items, and hints.
	 * @param sources the list of ConfigurationMetadataSource objects
	 * @param items the list of ConfigurationMetadataItem objects
	 * @param hints the list of ConfigurationMetadataHint objects
	 */
	RawConfigurationMetadata(List<ConfigurationMetadataSource> sources, List<ConfigurationMetadataItem> items,
			List<ConfigurationMetadataHint> hints) {
		this.sources = new ArrayList<>(sources);
		this.items = new ArrayList<>(items);
		this.hints = new ArrayList<>(hints);
		for (ConfigurationMetadataItem item : this.items) {
			resolveName(item);
		}
	}

	/**
	 * Returns the list of configuration metadata sources.
	 * @return the list of configuration metadata sources
	 */
	List<ConfigurationMetadataSource> getSources() {
		return this.sources;
	}

	/**
	 * Retrieves the source of the given configuration metadata item.
	 * @param item the configuration metadata item
	 * @return the configuration metadata source, or null if the source type is null or no
	 * matching source is found
	 */
	ConfigurationMetadataSource getSource(ConfigurationMetadataItem item) {
		if (item.getSourceType() == null) {
			return null;
		}
		return this.sources.stream()
			.filter((candidate) -> item.getSourceType().equals(candidate.getType())
					&& item.getId().startsWith(candidate.getGroupId()))
			.max(Comparator.comparingInt((candidate) -> candidate.getGroupId().length()))
			.orElse(null);
	}

	/**
	 * Returns the list of ConfigurationMetadataItems.
	 * @return the list of ConfigurationMetadataItems
	 */
	List<ConfigurationMetadataItem> getItems() {
		return this.items;
	}

	/**
	 * Returns the list of configuration metadata hints.
	 * @return the list of configuration metadata hints
	 */
	List<ConfigurationMetadataHint> getHints() {
		return this.hints;
	}

	/**
	 * Resolve the name of an item against this instance.
	 * @param item the item to resolve
	 * @see ConfigurationMetadataProperty#setName(String)
	 */
	private void resolveName(ConfigurationMetadataItem item) {
		item.setName(item.getId()); // fallback
		ConfigurationMetadataSource source = getSource(item);
		if (source != null) {
			String groupId = source.getGroupId();
			String dottedPrefix = groupId + ".";
			String id = item.getId();
			if (hasLength(groupId) && id.startsWith(dottedPrefix)) {
				String name = id.substring(dottedPrefix.length());
				item.setName(name);
			}
		}
	}

	/**
	 * Checks if the given string has a non-null and non-empty length.
	 * @param string the string to be checked
	 * @return true if the string has a non-null and non-empty length, false otherwise
	 */
	private static boolean hasLength(String string) {
		return (string != null && !string.isEmpty());
	}

}
