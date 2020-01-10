/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {
	public static Path file(String relative) {
		Path path = Paths.get("build/test-projects/" + relative);
		if (!path.toFile().canRead() || !path.toFile().isFile()) {
			throw new IllegalArgumentException("File not readable: " + path + ". Make sure Gradle task 'downloadTestResources' has been executed");
		}
		return path;
	}

	public static Path resource(String relative) {
		try {
			return Paths.get(TestUtils.class.getResource(relative).toURI());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	public static ProjectWrapper projectWrapper(String relative) {
		return new ProjectWrapper(file(relative));
	}
}
