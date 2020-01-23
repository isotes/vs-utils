/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import org.apache.xmlbeans.XmlObject;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper to access a string element that represents a (usually) semicolon-separated list as a {@link List}&lt;String&gt;
 */
public class ListElementWrapper extends AbstractList<String> {
	private final List<XmlObject> element;
	private final String delimiterRegex;
	private final String joinDelimiter;

	public ListElementWrapper(List<XmlObject> element, String delimiterRegex, String joinDelimiter) {
		this.element = element;
		this.delimiterRegex = delimiterRegex;
		this.joinDelimiter = joinDelimiter;
	}

	public ListElementWrapper(List<XmlObject> element) {
		this(element, "\\s*;\\s*", ";");
	}

	private void store(List<String> list) {
		X.set(element, list, joinDelimiter);
	}

	private ArrayList<String> load() {
		return new ArrayList<>(X.list(element, delimiterRegex));
	}

	@Override
	public String get(int index) {
		return X.string(element).orElse("").split(delimiterRegex)[index];
	}

	@Override
	public int size() {
		return X.string(element).orElse("").split(delimiterRegex).length;
	}

	@Override
	public String set(int index, String value) {
		List<String> list = load();
		String replaced = list.set(index, value);
		store(list);
		return replaced;
	}

	@Override
	public void add(int index, String value) {
		List<String> list = load();
		list.add(index, value);
		store(list);
	}

	@Override
	public String remove(int index) {
		List<String> list = load();
		String removed = list.remove(index);
		store(list);
		return removed;
	}
}
