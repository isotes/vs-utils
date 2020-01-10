/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import io.github.isotes.vs.model.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/** Wrapper to collect the elements for a configuration (e.g., Release|Win32) in C/C++ projects
 *
 * <p>Note: Most fields can be null.</p>
 */
public class ConfigurationWrapper {
	public final ProjectWrapper projectWrapper;
	public final ProjectDocument.Project project;
	public final String name;

	public final PropertyGroupWrapper propertyGroup;
	public final ImportGroupType importGroup;
	public final ItemDefinitionGroupType itemDefinitionGroup;
	public final ClCompileDocument.ClCompile clCompile;
	public final LinkItem lib;
	public final LinkItem link;
	public final ProjectReferenceDocument.ProjectReference projectReference;

	public ConfigurationWrapper(ProjectWrapper projectWrapper, String config) {
		this.projectWrapper = projectWrapper;
		this.project = projectWrapper.project;
		this.name = config;
		propertyGroup = X.configComponent(config, project.getPropertyGroupList(), PropertyGroupType::getCondition).map(PropertyGroupWrapper::new).orElse(null);
		importGroup = X.configComponent(config, project.getImportGroupList(), ImportGroupType::getCondition).orElse(null);
		itemDefinitionGroup = X.configComponent(config, project.getItemDefinitionGroupList(), ItemDefinitionGroupType::getCondition).orElse(null);

		ClCompileDocument.ClCompile clCompile = null;
		LinkItem lib = null;
		LinkItem link = null;
		ProjectReferenceDocument.ProjectReference projectReference = null;
		if (itemDefinitionGroup != null) {
			link = X.optionalComponent(itemDefinitionGroup.getLinkList()).orElse(null);
			lib = X.optionalComponent(itemDefinitionGroup.getLibList()).orElse(null);
			for (SimpleItemType si : itemDefinitionGroup.getItemList()) {
				if (si instanceof ClCompileDocument.ClCompile) {
					clCompile = (ClCompileDocument.ClCompile) si;
				} else if (si instanceof ProjectReferenceDocument.ProjectReference) {
					projectReference = (ProjectReferenceDocument.ProjectReference) si;
				}
			}
		}
		this.clCompile = clCompile;
		this.lib = lib;
		this.link = link;
		this.projectReference = projectReference;
	}

	public ListElementWrapper getPreprocessorDefinitions() {
		return new ListElementWrapper(clCompile.getPreprocessorDefinitionsList());
	}

	public ListElementWrapper getAdditionalIncludeDirectories() {
		return new ListElementWrapper(clCompile.getAdditionalIncludeDirectoriesList());
	}

	public ListElementWrapper getAdditionalDependencies() {
		return new ListElementWrapper(link.getAdditionalDependenciesList());
	}

	public ListElementWrapper getAdditionalLibraryDirectories() {
		return new ListElementWrapper(link.getAdditionalLibraryDirectoriesList());
	}

	public String getOutputFile() {
		if (lib != null) {
			return X.string(lib.getOutputFileList()).orElseThrow(() -> new IllegalArgumentException("Expected OutputFile in Lib"));
		}
		return X.string(link.getOutputFileList()).orElseThrow(() -> new IllegalArgumentException("Expected OutputFile in Link"));
	}

	public Path getOutputDirectory() {
		List<String> pc = Arrays.asList(getOutputFile().split("[\\\\/]"));
		return projectWrapper.path.getParent().resolve(String.join("/", pc.subList(0, pc.size() - 1)));
	}

	public boolean isLibrary() {
		return lib != null;
	}

}
