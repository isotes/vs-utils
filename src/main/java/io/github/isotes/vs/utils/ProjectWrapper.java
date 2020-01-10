/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import io.github.isotes.vs.model.ItemGroupType;
import io.github.isotes.vs.model.ProjectConfigurationDocument;
import io.github.isotes.vs.model.ProjectDocument;
import io.github.isotes.vs.model.PropertyGroupType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Wrapper to provide utility accessors primarily for C/C++ projects
 *
 * <p>Get started:</p>
 * <ul>
 *     <li>Use {@link #getConfiguration(String)} to obtain a convenience wrapper providing access to the sections
 *     commonly present in C/C++ project files.</li>
 *     <li>Use {@link #save(Path)} and {@link #stringify()} to save a project file preserving original newline and BOM
 *     and trying to work with formatting inconsistencies of the upstream project to minimize unintended differences.
 *     </li>
 * </ul>
 */
public class ProjectWrapper {
	public final Path path;
	public final ProjectDocument projectDocument;
	public final ProjectDocument.Project project;
	public final String newline;
	public final boolean hasBom;


	private static final Pattern FIX_SELF_CLOSING_TAGS = Pattern.compile("^(\\s*)(<.*\\S)/>$", Pattern.MULTILINE);
	private static final Pattern FIX_IMPORT_GROUP = Pattern.compile("^(\\s*)(<ImportGroup Label=\".*\">)(</ImportGroup>)$", Pattern.MULTILINE);
	private static final Pattern FIX_MULTILINE_EMPTY_TAG = Pattern.compile(
			"^(\\s*)<(SccProjectName|SccAuxPath|SccLocalPath|SccProvider|ImageHasSafeExceptionHandlers|IgnoreAllDefaultLibraries|LinkLibraryDependencies|Command|Message)></\\w+>$",
			Pattern.MULTILINE);


	public ProjectWrapper(ProjectDocument projectDocument) {
		this.path = null;
		this.projectDocument = projectDocument;
		this.project = projectDocument.getProject();
		// Windows defaults
		this.hasBom = true;
		this.newline = "\r\n";
	}

	public ProjectWrapper(Path path) {
		TextFile textFile = new TextFile(path);
		this.path = path;
		this.newline = textFile.newline;
		this.hasBom = textFile.hasBom;
		try {
			this.projectDocument = ProjectDocument.Factory.parse(textFile.content);
			this.project = projectDocument.getProject();
		} catch (XmlException e) {
			throw new IllegalArgumentException("Parsing " + path + " failed: " + e.getMessage(), e);
		}
	}

	public List<ProjectConfiguration> configurations() {
		Optional<ItemGroupType> pcs = X.component("ProjectConfigurations", project.getItemGroupList(), ItemGroupType::getLabel);
		if (!pcs.isPresent()) {
			return Collections.emptyList();
		}
		return pcs.get().getItemList().stream()
				.filter(v -> v instanceof ProjectConfigurationDocument.ProjectConfiguration)
				.map(v -> new ProjectConfiguration((ProjectConfigurationDocument.ProjectConfiguration) v))
				.collect(Collectors.toList());
	}

	public ConfigurationWrapper getConfiguration(String config) {
		return new ConfigurationWrapper(this, config);
	}

	public PropertyGroupType getPropertyGroup(String label) {
		for (PropertyGroupType pg : project.getPropertyGroupList()) {
			if (Objects.equals(label, pg.getLabel())) {
				return pg;
			}
		}
		throw new IllegalArgumentException("Property group with label '" + label + "'not found");
	}

	public PropertyGroupWrapper getGlobalsPropertyGroup() {
		return new PropertyGroupWrapper(getPropertyGroup("Globals"));
	}

	public GenericPropertyGroup getUserMacrosPropertyGroup() {
		return new GenericPropertyGroup(getPropertyGroup("UserMacros"));
	}

	public String stringify(String newline, boolean withBom) {
		StringWriter sw = new StringWriter();
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSavePrettyPrint();
		try {
			projectDocument.save(sw, xmlOptions);
			String content = sw.toString();

			// add space before slash to self-closing tags
			content = FIX_SELF_CLOSING_TAGS.matcher(content).replaceAll("$1$2 />");
			// the import groups contain a line break
			content = FIX_IMPORT_GROUP.matcher(content).replaceAll("$1$2\n$1$3");
			// as do several other tags
			content = FIX_MULTILINE_EMPTY_TAG.matcher(content).replaceAll("$1<$2>\n$1</$2>");
			// newline as requested
			content = content.replace("\r\n", "\n").replace("\n", newline);
			// add xml version and encoding
			content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + newline + content;

			if (withBom) {
				content = TextFile.BOM + content;
			}
			return content;
		} catch (IOException e) {
			throw new IllegalStateException("Failed to convert project to string: " + e.getMessage(), e);
		}
	}

	public String stringify() {
		return stringify(this.newline, this.hasBom);
	}

	public void save(Path path) {
		try {
			Files.write(path, stringify().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new IllegalArgumentException("Saving project to " + path + " failed: " + e.getMessage(), e);
		}
	}
}
