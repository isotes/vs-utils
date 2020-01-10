/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Read a text file and detect newline and optional UTF-8 BOM */
public class TextFile {
	public static final char BOM = '\ufeff';

	public final Path path;
	public final Charset charset;
	public final String content;
	public final String newline;
	public final boolean hasBom;

	public TextFile(Path path, Charset charset) {
		try {
			this.path = path;
			this.charset = charset;
			byte[] bytes = Files.readAllBytes(path);
			this.hasBom = bytes[0] == (byte) 0xef && bytes[1] == (byte) 0xbb && bytes[2] == (byte) 0xbf;
			int offset = hasBom ? 3 : 0;
			this.content = new String(bytes, offset, bytes.length - offset, charset);
			int firstLinefeed = content.indexOf(0x0a);
			this.newline = (firstLinefeed > 0 && content.charAt(firstLinefeed - 1) == '\r') ? "\r\n" : "\n";
		} catch (IOException e) {
			throw new IllegalArgumentException("Reading " + path + " failed: " + e.getMessage(), e);
		}
	}

	public TextFile(Path path) {
		this(path, StandardCharsets.UTF_8);
	}
}
