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

package org.springframework.boot.buildpack.platform.build;

import java.util.Objects;

import org.springframework.boot.buildpack.platform.docker.type.VolumeName;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Details of a cache for use by the CNB builder.
 *
 * @author Scott Frederick
 * @since 2.6.0
 */
public class Cache {

	/**
	 * The format of the cache.
	 */
	public enum Format {

		/**
		 * A cache stored as a volume in the Docker daemon.
		 */
		VOLUME("volume"),

		/**
		 * A cache stored as a bind mount.
		 */
		BIND("bind mount");

		private final String description;

		/**
		 * Sets the description of the cache.
		 * @param description the description to be set
		 */
		Format(String description) {
			this.description = description;
		}

		/**
		 * Returns the description of the cache.
		 * @return the description of the cache
		 */
		public String getDescription() {
			return this.description;
		}

	}

	protected final Format format;

	/**
	 * Constructs a new Cache object with the specified format.
	 * @param format the format to be used by the Cache object
	 */
	Cache(Format format) {
		this.format = format;
	}

	/**
	 * Return the details of the cache if it is a volume cache.
	 * @return the cache, or {@code null} if it is not a volume cache
	 */
	public Volume getVolume() {
		return (this.format.equals(Format.VOLUME)) ? (Volume) this : null;
	}

	/**
	 * Return the details of the cache if it is a bind cache.
	 * @return the cache, or {@code null} if it is not a bind cache
	 */
	public Bind getBind() {
		return (this.format.equals(Format.BIND)) ? (Bind) this : null;
	}

	/**
	 * Create a new {@code Cache} that uses a volume with the provided name.
	 * @param name the cache volume name
	 * @return a new cache instance
	 */
	public static Cache volume(String name) {
		Assert.notNull(name, "Name must not be null");
		return new Volume(VolumeName.of(name));
	}

	/**
	 * Create a new {@code Cache} that uses a volume with the provided name.
	 * @param name the cache volume name
	 * @return a new cache instance
	 */
	public static Cache volume(VolumeName name) {
		Assert.notNull(name, "Name must not be null");
		return new Volume(name);
	}

	/**
	 * Create a new {@code Cache} that uses a bind mount with the provided source.
	 * @param source the cache bind mount source
	 * @return a new cache instance
	 */
	public static Cache bind(String source) {
		Assert.notNull(source, "Source must not be null");
		return new Bind(source);
	}

	/**
	 * Compares this Cache object to the specified object for equality.
	 * @param obj the object to compare to
	 * @return true if the specified object is equal to this Cache object, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Cache other = (Cache) obj;
		return Objects.equals(this.format, other.format);
	}

	/**
	 * Returns the hash code value for the Cache object.
	 * @return the hash code value for the Cache object
	 */
	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.format);
	}

	/**
	 * Details of a cache stored in a Docker volume.
	 */
	public static class Volume extends Cache {

		private final VolumeName name;

		/**
		 * Constructs a new Volume object with the specified name.
		 * @param name the name of the volume
		 */
		Volume(VolumeName name) {
			super(Format.VOLUME);
			this.name = name;
		}

		/**
		 * Returns the name of the Volume.
		 * @return the name of the Volume
		 */
		public String getName() {
			return this.name.toString();
		}

		/**
		 * Returns the name of the volume.
		 * @return the name of the volume
		 */
		public VolumeName getVolumeName() {
			return this.name;
		}

		/**
		 * Compares this Volume object to the specified object for equality. Returns true
		 * if the specified object is also a Volume object and has the same name as this
		 * Volume object.
		 * @param obj the object to compare with
		 * @return true if the objects are equal, false otherwise
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			if (!super.equals(obj)) {
				return false;
			}
			Volume other = (Volume) obj;
			return Objects.equals(this.name, other.name);
		}

		/**
		 * Returns the hash code value for this Volume object.
		 * @return the hash code value for this object
		 */
		@Override
		public int hashCode() {
			int result = super.hashCode();
			result = 31 * result + ObjectUtils.nullSafeHashCode(this.name);
			return result;
		}

		/**
		 * Returns a string representation of the Volume object. The string representation
		 * includes the format description and the name of the Volume.
		 * @return a string representation of the Volume object
		 */
		@Override
		public String toString() {
			return this.format.getDescription() + " '" + this.name + "'";
		}

	}

	/**
	 * Details of a cache stored in a bind mount.
	 */
	public static class Bind extends Cache {

		private final String source;

		/**
		 * Constructs a new Bind object with the specified source.
		 * @param source the source string to bind
		 */
		Bind(String source) {
			super(Format.BIND);
			this.source = source;
		}

		/**
		 * Returns the source of the Bind object.
		 * @return the source of the Bind object
		 */
		public String getSource() {
			return this.source;
		}

		/**
		 * Compares this Bind object to the specified object for equality.
		 * @param obj the object to compare to
		 * @return true if the objects are equal, false otherwise
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			if (!super.equals(obj)) {
				return false;
			}
			Bind other = (Bind) obj;
			return Objects.equals(this.source, other.source);
		}

		/**
		 * Returns a hash code value for the object. This method overrides the hashCode()
		 * method in the superclass.
		 * @return the hash code value for the object
		 */
		@Override
		public int hashCode() {
			int result = super.hashCode();
			result = 31 * result + ObjectUtils.nullSafeHashCode(this.source);
			return result;
		}

		/**
		 * Returns a string representation of the Bind object.
		 * @return the description of the format followed by the source value enclosed in
		 * single quotes
		 */
		@Override
		public String toString() {
			return this.format.getDescription() + " '" + this.source + "'";
		}

	}

}
