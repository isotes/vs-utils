/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** A Project used in a solution */
@SuppressWarnings("CanBeFinal")
public class VsSolutionProject {
	// Project("{F184B08F-C81C-45F6-A57F-5ABD9991F28F}") = "Project1", "Project1.vbproj", "{8CDD8387-B905-44A8-B5D5-07BB50E05BEA}"
	public int firstLine;
	public String typeGuid;
	public String name;
	public String fileName;
	public String guid;
	public final List<VsSolutionSection> sections = new ArrayList<>();
	public Path path;

	public VsSolutionProject(int firstLine, Path parent, String typeGuid, String name, String fileName, String guid) {
		this.firstLine = firstLine;
		this.typeGuid = typeGuid;
		this.name = name;
		this.fileName = fileName;
		this.guid = guid;
		this.path = parent.resolve(fileName.replace('\\', '/'));
	}

	public ProjectType projectType() {
		return ProjectType.byGuid(typeGuid);
	}

	/** @return the list of the GUIDs of dependency projects */
	public List<String> dependencies() {
		// 	ProjectSection(ProjectDependencies) = postProject
		//		{E2238402-DD1F-4DFD-A740-E6770766015E} = {E2238402-DD1F-4DFD-A740-E6770766015E}
		//	EndProjectSection
		List<String> r = new ArrayList<>();
		for (VsSolutionSection section : sections) {
			if ("ProjectDependencies".equals(section.label)) {
				for (String line : section.content) {
					String[] lr = line.split(" = ", 2);
					r.add(lr[0]);
				}
			}
		}
		return r;
	}

	/**
	 * Get the dependency projects
	 * @param solution the solution used for resolving the GUIDs of the dependencies
	 * @return the list of projects
	 */
	public List<VsSolutionProject> dependencies(VsSolution solution) {
		List<String> guids = dependencies();
		List<VsSolutionProject> r = new ArrayList<>(guids.size());
		for (String guid : guids) {
			VsSolutionProject project = solution.byGuid(guid);
			if (project == null) {
				throw new IllegalArgumentException("Unknown project GUID " + guid + " as dependency of " + name);
			}
			r.add(project);
		}
		return r;
	}

	public void stringify(List<String> output) {
		output.add("Project(\"" + typeGuid + "\") = \"" + name + "\", \"" + fileName + "\", \"" + guid + "\"");
		for (VsSolutionSection section : sections) {
			section.stringify(output);
		}
		output.add("EndProject");
	}
}
