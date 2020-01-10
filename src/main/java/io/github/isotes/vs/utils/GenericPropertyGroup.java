/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import io.github.isotes.vs.model.PropertyGroupType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Wrapper to access {@link PropertyGroupType} as a key/value {@link Map}&lt;String, String&gt; independently of the schema.
 *
 * <p>This class uses the underlying XML to provide access to all elements that are text nodes. This allows, for
 * example, access to custom properties in 'UserMacros' property groups. To access property groups with elements
 * specified in the MSBuild schema (using the {@link PropertyGroupType#getPropertyList()} accessor) see
 * {@link PropertyGroupWrapper}. </p>
 */
public class GenericPropertyGroup extends AbstractMap<String, String> {

	public final PropertyGroupType propertyGroup;
	private final InnerSet innerSet = new InnerSet();

	public GenericPropertyGroup(PropertyGroupType propertyGroup) {
		this.propertyGroup = propertyGroup;
	}

	private class IteratorWrapper implements Iterator<Entry<String, String>> {
		int index;

		public IteratorWrapper() {
			index = -1;
			index = nextTextElementIndex();
		}

		// return -2 if none left
		private int nextTextElementIndex() {
			NodeList childNodes = propertyGroup.getDomNode().getChildNodes();
			int length = childNodes.getLength();
			for (int i = index + 1; i < length; i++) {
				if (X.isStringElement(childNodes.item(i))) {
					return i;
				}
			}
			return -2;
		}

		@Override
		public boolean hasNext() {
			return nextTextElementIndex() >= 0;
		}

		@Override
		public Entry<String, String> next() {
			Node item = propertyGroup.getDomNode().getChildNodes().item(index);
			index = nextTextElementIndex();
			return new SimpleEntry<>(item.getNodeName(), X.string(item));
		}

		@Override
		public void remove() {
			propertyGroup.getDomNode().removeChild(propertyGroup.getDomNode().getChildNodes().item(index));
			index -= 1;
			index = nextTextElementIndex();
		}
	}

	private class InnerSet extends AbstractSet<Entry<String, String>> {
		@SuppressWarnings("NullableProblems")
		@Override
		public Iterator<Entry<String, String>> iterator() {
			return new IteratorWrapper();
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
		NodeList childNodes = propertyGroup.getDomNode().getChildNodes();
		int length = childNodes.getLength();
		for (int i = 0; i < length; i++) {
			if (childNodes.item(i).getNodeName().equals(key)) {
				String old = X.string(childNodes.item(i));
				X.set(childNodes.item(i), value);
				return old;
			}
		}
		X.addStringElement(propertyGroup, key, value);
		return null;
	}
}
