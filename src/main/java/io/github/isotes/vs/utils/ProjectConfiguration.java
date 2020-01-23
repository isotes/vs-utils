/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import io.github.isotes.vs.model.ProjectConfigurationDocument;

/** Details of a {@link io.github.isotes.vs.model.ProjectConfigurationDocument.ProjectConfiguration} for C/C++ projects */
public class ProjectConfiguration {
	public final ProjectWrapper project;
	public final String name;
	public final String configuration;
	public final String platform;

	public ProjectConfiguration(ProjectWrapper project, String name, String configuration, String platform) {
		this.project = project;
		this.name = name;
		this.configuration = configuration;
		this.platform = platform;
	}

	public ProjectConfiguration(ProjectWrapper project, ProjectConfigurationDocument.ProjectConfiguration cfg) {
		this(project, cfg.getInclude(), X.getString(cfg.getConfiguration()), X.getString(cfg.getPlatform()));
	}

	public ConfigurationWrapper resolve() {
		return project.getConfiguration(name);
	}
}
