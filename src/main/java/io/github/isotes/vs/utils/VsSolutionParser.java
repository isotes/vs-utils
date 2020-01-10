/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Internal helper class to parse solution files for {@link io.github.isotes.vs.utils.VsSolution} */
public class VsSolutionParser {
	// Project("{F184B08F-C81C-45F6-A57F-5ABD9991F28F}") = "Project1", "Project1.vbproj", "{8CDD8387-B905-44A8-B5D5-07BB50E05BEA}"
	private final static Pattern PROJECT_RE = Pattern.compile("^Project\\(\"([^\"]+)\"\\) = \"([^\"]+)\", \"([^\"]+)\", \"([^\"]+)\"$");
	// ProjectSection(ProjectDependencies) = postProject
	private final static Pattern SECTION_RE = Pattern.compile("^(\\s*)(\\w+)\\((\\w+)\\) = (\\w+)$");

	private final VsSolution solution;
	private final String fileName;
	private final String[] lines;
	private int index;

	public static void parse(VsSolution solution, Path fileName) {
		TextFile textFile = new TextFile(fileName);
		parse(solution, fileName, textFile.content, textFile.newline, textFile.hasBom);
	}

	public static void parse(VsSolution solution, String content) {
		parse(solution, Paths.get("/from/string"), content, "\n", false);
	}

	public static void parse(VsSolution solution, Path path, String content, String newline, boolean hasBom) {
		solution.path = path;
		solution.newline = newline;
		solution.hasBom = hasBom;
		new VsSolutionParser(solution, content.split("\r?\n"));
	}


	private VsSolutionParser(VsSolution solution, String[] lines) {
		this.solution = solution;
		this.fileName = solution.path.toString();
		this.lines = lines;
		parseHeader();
		parseProjects();
		parseGlobal();
	}

	private void parseHeader() {
		while (!startsWith("Project(")) {
			solution.headerLines.add(consume());
		}
	}

	private void parseProjects() {
		while (startsWith("Project(")) {
			solution.projects.add(parseProject());
		}
	}

	private VsSolutionProject parseProject() {
		String l = consume();
		Matcher m = PROJECT_RE.matcher(l);
		if (!m.find()) {
			throw ex("Expected Project Reference");
		}
		VsSolutionProject vsp = new VsSolutionProject(prevLineNumber(), solution.path.getParent(),
				m.group(1), m.group(2), m.group(3), m.group(4));
		while (!skipIfStartsWith("EndProject")) {
			vsp.sections.add(parseSection());
		}
		return vsp;
	}

	private void parseGlobal() {
		if (!skipIfStartsWith("Global")) {
			throw ex("Global section expected");
		}
		while (!skipIfStartsWith("EndGlobal")) {
			solution.globalSections.add(parseSection());
		}
	}

	private VsSolutionSection parseSection() {
		String l = consume();
		Matcher m = SECTION_RE.matcher(l);
		if (!m.find()) {
			throw ex("Expected Section");
		}
		VsSolutionSection vss = new VsSolutionSection(prevLineNumber(), m.group(1), m.group(2), m.group(3), m.group(4));
		while (!skipIfStartsWith("End" + vss.tag)) {
			if (vss.contentIndent == null) {
				vss.contentIndent = indent();
			}
			vss.content.add(consumeContent());
		}
		if (vss.contentIndent == null) {
			vss.contentIndent = vss.tagIndent + "\t";  // best effort
		}
		return vss;
	}

	// "lexer"

	private void skip() {
		index += 1;
	}

	private String consume() {
		return lines[index++];
	}

	private String indent() {
		StringBuilder in = new StringBuilder();
		for (int i = 0; i < lines[index].length(); i++) {
			char c = lines[index].charAt(i);
			if (!Character.isWhitespace(c)) {
				break;
			}
			in.append(c);
		}
		return in.toString();
	}

	private String content() {
		return lines[index].substring(indent().length());
	}

	private String consumeContent() {
		String s = content();
		index += 1;
		return s;
	}

	private boolean startsWith(String prefix) {
		return content().startsWith(prefix);
	}

	private boolean skipIfStartsWith(String prefix) {
		if (content().startsWith(prefix)) {
			skip();
			return true;
		}
		return false;
	}

	private int prevLineNumber() {
		// refer to previous line but start line numbers at 1 -> evens out to 'index'
		return index;
	}

	private IllegalArgumentException ex(String message) {
		return new IllegalArgumentException(fileName + ":" + prevLineNumber() + ": " + message);
	}
}
