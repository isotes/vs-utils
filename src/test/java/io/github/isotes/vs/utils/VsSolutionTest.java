/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;

class VsSolutionTest {
	public static final Path SLN = TestUtils.file("Hilo2015/Hilo.sln");

	@Test
	void readWrite() throws Exception {
		VsSolution solution = new VsSolution(SLN);
		String expected = new String(Files.readAllBytes(SLN));
		assertThat(solution.stringify()).isEqualTo(expected);
	}

	@Test
	void accessors() throws Exception {
		VsSolution solution = new VsSolution(SLN);

		VsSolutionProject pBrowser = solution.projects.get(0);
		VsSolutionProject pCommon = solution.projects.get(1);
		VsSolutionProject pAnnotator = solution.projects.get(2);
		VsSolutionProject pRegistrationHelper = solution.projects.get(3);
		assertThat(pBrowser.projectType()).isEqualTo(ProjectType.WINDOWS_VISUAL_CPP);
		assertThat(pBrowser.name).isEqualTo("Browser");
		assertThat(pBrowser.fileName).isEqualTo("Browser\\Browser.vcxproj");
		assertThat(pBrowser.guid).isEqualTo("{C7EA8C82-5E8A-4A75-BF2E-5E3D13F13AD1}");
		assertThat(pBrowser.dependencies()).containsExactly("{5FF3964A-3D01-4C51-89B7-C87252756BD2}");
		assertThat(solution.byGuid(pBrowser.dependencies().get(0))).isSameInstanceAs(pAnnotator);
		assertThat(pCommon.dependencies()).isEmpty();
		assertThat(pAnnotator.dependencies(solution)).containsExactly(pCommon);
		assertThat(pRegistrationHelper.guid).isEqualTo("{2851BF7E-A889-41A1-9FD1-8BCB09248EEF}");
	}

	// used as the introductory example in the Readme
	@Test
	void projects() throws Exception {
		Path solutionPath = SLN;
		VsSolution solution = new VsSolution(solutionPath);
		for (VsSolutionProject vsp : solution.projects) {
			ProjectWrapper project = new ProjectWrapper(vsp.path);
			for (ProjectConfiguration projectConfiguration : project.configurations()) {
				ConfigurationWrapper cfg = project.getConfiguration(projectConfiguration.name);
				cfg.propertyGroup.put("PlatformToolset", "v142");
				X.set(cfg.clCompile.getMultiProcessorCompilationList(), "true");
				if (X.string(cfg.clCompile.getWarningLevelList()).orElse("").equals("Level4")) {
					X.set(cfg.clCompile.getDisableSpecificWarningsList(), "4710;4711");
				}
			}
//			project.save(project.path);
		}
	}
}
