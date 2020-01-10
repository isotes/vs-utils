/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import java.util.ArrayList;
import java.util.List;

/** A Section used in a solution */
@SuppressWarnings("CanBeFinal")
public class VsSolutionSection {
	// GlobalSection(SolutionConfigurationPlatforms) = preSolution -> tag(label) = when
	public int firstLine;
	public String tagIndent;
	public String tag;
	public String label;
	public String when;
	public String contentIndent;  // detected from the first line in content
	public final List<String> content = new ArrayList<>();

	public VsSolutionSection(int firstLine, String tagIndent, String tag, String label, String when) {
		this.firstLine = firstLine;
		this.tagIndent = tagIndent;
		this.tag = tag;
		this.label = label;
		this.when = when;
	}

	public void stringify(List<String> output) {
		output.add(tagIndent + tag + "(" + label + ") = " + when);
		for (String l : content) {
			output.add(contentIndent + l);
		}
		output.add(tagIndent + "End" + tag);
	}
}
