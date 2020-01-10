/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Describes a solution file as specified
 * <a href="https://docs.microsoft.com/en-us/visualstudio/extensibility/internals/solution-dot-sln-file?view=vs-2019">here</a>
 */
public class VsSolution {
	public Path path;
	public String newline;
	public boolean hasBom;
	public final List<String> headerLines = new ArrayList<>();
	public final List<VsSolutionProject> projects = new ArrayList<>();
	public final List<VsSolutionSection> globalSections = new ArrayList<>();

	public VsSolution(Path path) {
		VsSolutionParser.parse(this, path);
	}

	public VsSolutionProject byGuid(String guid) {
		for (VsSolutionProject project : projects) {
			if (project.guid.equals(guid)) {
				return project;
			}
		}
		return null;
	}

	public Optional<VsSolutionSection> globalSection(String label, String when) {
		for (VsSolutionSection gs : globalSections) {
			if (Objects.equals(label, gs.label) && Objects.equals(when, gs.when)) {
				return Optional.of(gs);
			}
		}
		return Optional.empty();
	}

	public void stringify(List<String> output) {
		output.addAll(headerLines);
		for (VsSolutionProject project : projects) {
			project.stringify(output);
		}
		output.add("Global");
		for (VsSolutionSection section : globalSections) {
			section.stringify(output);
		}
		output.add("EndGlobal");
		output.add("");
	}

	public String stringify() {
		List<String> lines = new ArrayList<>();
		stringify(lines);
		String content = String.join(newline, lines);
		if (hasBom) {
			content = TextFile.BOM + content;
		}
		return content;
	}

	public void save(Path path) {
		try {
			Files.write(path, stringify().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new IllegalArgumentException("Saving solution to " + path + " failed: " + e.getMessage(), e);
		}
	}
}
