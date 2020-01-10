/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

class ProjectWrapperTest {
	public static final String VCXPROJ = "Hilo2015/Common/Common.vcxproj";
	private final ProjectWrapper project = TestUtils.projectWrapper(VCXPROJ);

	@Test
	void configurations() {
		assertThat(project.configurations().stream().map(v -> v.name))
				.containsExactly("Debug|Win32", "Debug|x64", "Release|Win32", "Release|x64");
	}

	@Test
	void stringify() throws Exception {
		for (String pn : Arrays.asList("Annotator", "Browser", "Common", "RegistrationHelper")) {
			Path original = TestUtils.file("Hilo2015/" + pn + "/" + pn + ".vcxproj");
			ProjectWrapper pw = new ProjectWrapper(original);
			String expected = new String(Files.readAllBytes(original));
			assertThat(pw.stringify()).isEqualTo(expected);
		}
	}
}
