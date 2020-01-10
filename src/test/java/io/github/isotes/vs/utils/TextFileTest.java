/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;

class TextFileTest {

	@Test
	void load() {
		TextFile textFile = new TextFile(TestUtils.file("Hilo2015/Common/Common.vcxproj"));
		assertThat(textFile.charset).isEqualTo(StandardCharsets.UTF_8);
		assertThat(textFile.newline).isEqualTo("\n");
		assertThat(textFile.hasBom).isEqualTo(true);
	}

	@Test
	void loadCrlfHasbom() {
		TextFile textFile = new TextFile(TestUtils.resource("text-crlf-hasbom.txt"));
		assertThat(textFile.charset).isEqualTo(StandardCharsets.UTF_8);
		assertThat(textFile.newline).isEqualTo("\r\n");
		assertThat(textFile.hasBom).isEqualTo(true);
	}

	@Test
	void loadLfNobom() {
		TextFile textFile = new TextFile(TestUtils.resource("text-lf-nobom.txt"));
		assertThat(textFile.charset).isEqualTo(StandardCharsets.UTF_8);
		assertThat(textFile.newline).isEqualTo("\n");
		assertThat(textFile.hasBom).isEqualTo(false);
	}
}
