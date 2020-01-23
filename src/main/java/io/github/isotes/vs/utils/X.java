/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import io.github.isotes.vs.model.SimpleItemType;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Convenience functions to access elements of project files
 *
 * <p>Based on the upstream schema, most elements may occur multiple times even if semantically only one is possible,
 * e.g., for specifying the compiler warning level. MSBuild seems to use the last occurrence while evaluating the
 * project. Moreover, most simple elements that seem to be strings are simply specified as {@code xs:any}. This results
 * in most XMLBeans generated accessors to return {@link List}&lt;{@link XmlObject}&gt;, e.g.,
 * {@link io.github.isotes.vs.model.ClCompileDocument.ClCompile#getWarningLevelList()}.</p>
 *
 * <p>The functions {@link #string(List)} and {@link #set(List, String)} provide easy access by working on the last
 * value of a list and treat it as a string. Additionally, {@link #set(List, String)} adds an element if the list
 * is empty. The functions {@link #string(XmlObject)} and {@link #set(XmlObject, String)} are for the case if the
 * underlying element is modelled as a single {@code xs:any} element.</p>
 * 
 * <p>Based on these functions, {@link #list(List, String)} and {@link #set(List, List, CharSequence)} provide
 * get/set access to string elements that actually represent string-delimited (usually ';') lists. For this, the
 * {@link ListElementWrapper} provides an alternative mutable list implementation that is backed directly by the
 * underlying model.</p>
 *
 */
public class X {
	// e.g., '$(Configuration)|$(Platform)'=='Release|Win32'
	public static final String CONFIGURATION_CONDITION = "'$(Configuration)|$(Platform)'=='";

	public static Optional<String> string(List<XmlObject> xmlObjectList) {
		int n = xmlObjectList.size();
		if (n == 0) {
			return Optional.empty();
		}
		XmlObject xo = xmlObjectList.get(n - 1);
		if (!(xo instanceof SimpleValue)) {
			throw throwIAE("Expected list of string elements, got ", xo.getClass());
		}
		return Optional.of(((SimpleValue) xo).getStringValue());
	}

	public static void set(List<XmlObject> xmlObjectList, String newValue) {
		// we do not use clear + add to preserve position and indent if already present
		int n = xmlObjectList.size();
		if (n == 0) {
			xmlObjectList.add(XmlString.Factory.newValue(newValue));
		} else {
			xmlObjectList.set(n - 1, XmlString.Factory.newValue(newValue));
		}
	}

	public static List<String> list(List<XmlObject> xmlObjectList, String delimiterRegex) {
		return Arrays.asList(string(xmlObjectList).orElse("").split(delimiterRegex));
	}

	public static List<String> list(List<XmlObject> xmlObjectList) {
		return list(xmlObjectList, "\\s*;\\s*");
	}

	public static void set(List<XmlObject> xmlObjectList, List<String> newValue, CharSequence delimiter) {
		set(xmlObjectList, String.join(delimiter, newValue));
	}

	public static void set(List<XmlObject> xmlObjectList, List<String> newValue) {
		set(xmlObjectList, newValue, ";");
	}

	public static Optional<String> string(XmlObject xmlObject) {
		if (xmlObject == null) {
			return Optional.empty();
		}
		if (xmlObject instanceof SimpleValue) {
			return Optional.of(((SimpleValue) xmlObject).getStringValue());
		}
		throw throwIAE("Expected string element, got ", xmlObject.getClass());
	}

	public static String getString(XmlObject xmlObject) {
		return string(xmlObject).orElseThrow(() -> throwIAE("Expected SimpleValue element"));
	}

	public static void set(XmlObject xmlObject, String newValue) {
		((XmlString) xmlObject).setStringValue(newValue);
	}

	public static String elementName(XmlObject xmlObject) {
		return xmlObject.getDomNode().getLocalName();
	}

	public static <T> Optional<T> optionalComponent(List<T> components) {
		int n = components.size();
		return n == 0 ? Optional.empty() : Optional.of(components.get(n - 1));
	}

	public static <T> Optional<T> simpleItem(List<SimpleItemType> simpleItems, Class<T> itemClass) {
		for (SimpleItemType simpleItem : simpleItems) {
			if (itemClass.isInstance(simpleItem)) {
				return Optional.of(itemClass.cast(simpleItem));
			}
		}
		return Optional.empty();
	}

	public static <T> T getSimpleItem(List<SimpleItemType> simpleItems, Class<T> itemClass) {
		return simpleItem(simpleItems, itemClass).orElseThrow(() -> throwIAE("SimpleItem of specified type not found", itemClass));
	}

	public static <T> Optional<T> component(String attributeValue, Iterable<T> components, Function<T, String> attributeGetter) {
		for (T component : components) {
			if (attributeValue.equals(attributeGetter.apply(component))) {
				return Optional.ofNullable(component);
			}
		}
		return Optional.empty();
	}

	public static <T> Optional<T> configComponent(String config, Iterable<T> components, Function<T, String> conditionGetter) {
		final String cond = X.configurationCondition(config);
		for (T component : components) {
			if (cond.equals(conditionGetter.apply(component))) {
				return Optional.ofNullable(component);
			}
		}
		return Optional.empty();
	}

	public static <T> T getConfigComponent(String config, Iterable<T> components, Function<T, String> conditionGetter) {
		return configComponent(config, components, conditionGetter).orElseThrow(() -> throwIAE(
				"Component for specified configuration not found", config));
	}


	public static String configurationCondition(String configuration) {
		return CONFIGURATION_CONDITION + configuration + "'";
	}

	// functions to work on XML for parts where the schema is incomplete

	static boolean isStringElement(Node node) {
		return node.getNodeType() == Node.ELEMENT_NODE && node.getFirstChild() != null && node.getFirstChild().getNodeType() == Node.TEXT_NODE;
	}

	public static void set(Node node, String value) {
		node.getFirstChild().setNodeValue(value);
	}

	public static String string(Node node) {
		return node.getFirstChild().getNodeValue();
	}

	public static Element addElement(Node parent, String tag, Map<String, String> attributes) {
		Element element = parent.getOwnerDocument().createElementNS(parent.getNamespaceURI(), tag);
		for (Map.Entry<String, String> attr : attributes.entrySet()) {
			element.setAttribute(attr.getKey(), attr.getValue());
		}
		parent.appendChild(element);
		return element;
	}

	public static Element addElement(Node parent, String tag) {
		return addElement(parent, tag, Collections.emptyMap());
	}

	public static Element addStringElement(Node parent, String tag, String value, Map<String, String> attributes) {
		Element element = addElement(parent, tag, attributes);
		element.appendChild(element.getOwnerDocument().createTextNode(value));
		return element;
	}

	public static Element addStringElement(Node parent, String tag, String value) {
		return addStringElement(parent, tag, value, Collections.emptyMap());
	}

	public static Element addStringElement(XmlObject parent, String tag, String value, Map<String, String> attributes) {
		return addStringElement(parent.getDomNode(), tag, value, attributes);
	}

	public static Element addStringElement(XmlObject parent, String tag, String value) {
		return addStringElement(parent, tag, value, Collections.emptyMap());
	}

	public static NodeList childElements(XmlObject parent, String tag) {
		return ((Element) parent.getDomNode()).getElementsByTagName(tag);
	}

	public static NodeList childElements(XmlObject parent) {
		return childElements(parent, "*");
	}


	private static IllegalArgumentException throwIAE(String message) {
		return new IllegalArgumentException(message);
	}

	private static IllegalArgumentException throwIAE(String message, Object argument) {
		return new IllegalArgumentException(message + ": " + argument);
	}
}
