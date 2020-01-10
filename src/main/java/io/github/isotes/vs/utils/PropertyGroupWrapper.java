/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import io.github.isotes.vs.model.PropertyGroupType;
import org.apache.xmlbeans.XmlObject;

import java.util.*;

/**
 * Wrapper to access {@link PropertyGroupType} as a key/value {@link Map}&lt;String, String&gt;
 *
 * <p>This class uses the {@link PropertyGroupType#getPropertyList()} accessor and, therefore, only contains properties
 * that are known to the schema. For property groups with arbitrary elements, e.g., 'UserMacros', use the
 * {@link GenericPropertyGroup} class which operates on directly on the underlying XML. </p>
 */
public class PropertyGroupWrapper extends AbstractMap<String, String> {

	public final PropertyGroupType propertyGroup;
	private final InnerSet innerSet = new InnerSet();

	public PropertyGroupWrapper(PropertyGroupType propertyGroup) {
		this.propertyGroup = propertyGroup;
	}

	private static class IteratorWrapper implements Iterator<Entry<String, String>> {
		private final Iterator<XmlObject> wrapped;

		private IteratorWrapper(Iterator<XmlObject> wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public boolean hasNext() {
			return wrapped.hasNext();
		}

		@Override
		public Entry<String, String> next() {
			XmlObject xo = wrapped.next();
			return new AbstractMap.SimpleEntry<>(X.elementName(xo), X.getString(xo));
		}

		@Override
		public void remove() {
			wrapped.remove();
		}
	}

	private class InnerSet extends AbstractSet<Entry<String, String>> {
		@SuppressWarnings("NullableProblems")
		@Override
		public Iterator<Entry<String, String>> iterator() {
			return new IteratorWrapper(propertyGroup.getPropertyList().iterator());
		}

		@Override
		public int size() {
			return propertyGroup.sizeOfPropertyArray();
		}
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public Set<Entry<String, String>> entrySet() {
		return innerSet;
	}

	@Override
	public String put(String key, String value) {
		for (XmlObject xo : propertyGroup.getPropertyList()) {
			if (Objects.equals(key, X.elementName(xo))) {
				String old = X.getString(xo);
				X.set(xo, value);
				return old;
			}
		}
		X.addStringElement(propertyGroup, key, value);
		return null;
	}
}
