/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.isotes.vs.utils;

import io.github.isotes.vs.model.ImportType;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

class ConfigurationWrapperTest {
	public static final String CONFIG = "Release|x64";
	public static final String LIB_VCXPROJ = "Hilo2015/Common/Common.vcxproj";
	public static final String APP_VCXPROJ = "Hilo2015/Annotator/Annotator.vcxproj";
	private final ConfigurationWrapper libCfg = TestUtils.projectWrapper(LIB_VCXPROJ).getConfiguration(CONFIG);
	private final ConfigurationWrapper appCfg = TestUtils.projectWrapper(APP_VCXPROJ).getConfiguration(CONFIG);


	@Test
	void propertyGroup() {
		assertThat(libCfg.propertyGroup).containsExactly(
				"ConfigurationType", "StaticLibrary",
				"UseDebugLibraries", "false",
				"WholeProgramOptimization", "true",
				"CharacterSet", "Unicode",
				"PlatformToolset", "v140");
	}

	@Test
	void importGroup() {
		List<ImportType> importList = libCfg.importGroup.getImportList();
		assertThat(importList.size()).isEqualTo(2);
		assertThat(importList.get(0).getProject()).isEqualTo("$(UserRootDir)\\Microsoft.Cpp.$(Platform).user.props");
		assertThat(importList.get(1).getProject()).isEqualTo("..\\Annotator\\ATLDeprecated.props");
	}

	@Test
	void clCompile() {
		assertThat(X.string(libCfg.clCompile.getWarningLevelList())).hasValue("Level4");
		assertThat(X.string(libCfg.clCompile.getOptimizationList())).hasValue("MaxSpeed");
		assertThat(X.list(libCfg.clCompile.getPreprocessorDefinitionsList()))
				.containsExactly("WIN32", "NDEBUG", "_LIB", "%(PreprocessorDefinitions)");
		assertThat(X.string(libCfg.clCompile.getStringPoolingList())).isEmpty();
	}

	@Test
	void link() {
		assertThat(X.string(libCfg.link.getEnableCOMDATFoldingList())).hasValue("true");
		assertThat(X.string(libCfg.link.getProfileList())).isEmpty();
	}

	@Test
	void lib() {
		assertThat(X.string(libCfg.lib.getTargetMachineList())).hasValue("MachineX64");
		assertThat(X.string(libCfg.lib.getDriverList())).isEmpty();
	}

	@Test
	void projectReference() {
		assertThat(X.string(libCfg.projectReference.getExcludeAssetsList())).isEmpty();
	}

	@Test
	void maps() {
		assertThat(appCfg.getPreprocessorDefinitions()).containsExactly("WIN32", "NDEBUG", "_WINDOWS", "%(PreprocessorDefinitions)");
		assertThat(appCfg.getAdditionalIncludeDirectories()).containsExactly("..\\Common\\Include", ".");
		assertThat(appCfg.getAdditionalDependencies()).containsExactly("d2d1.lib", "dwrite.lib", "windowscodecs.lib",
				"shlwapi.lib", "structuredquery.lib", "Propsys.lib", "Comctl32.lib", "%(AdditionalDependencies)");
		assertThat(appCfg.getAdditionalLibraryDirectories()).containsExactly("$(SolutionDir)$(Configuration)\\");
	}

	@Test
	void getOutputFile() {
		assertThat(libCfg.getOutputFile()).isEqualTo("$(OutDir)$(TargetName)$(TargetExt)");
	}

	@Test
	void getOutputPath() throws URISyntaxException {
		assertThat(libCfg.getOutputDirectory()).isEqualTo(TestUtils.file(LIB_VCXPROJ).getParent());
	}

	@Test
	void isLibrary() {
		assertThat(libCfg.isLibrary()).isTrue();
		assertThat(appCfg.isLibrary()).isFalse();
	}
}
