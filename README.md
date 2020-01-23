# vs-utils  [![License](https://img.shields.io/github/license/isotes/vs-utils)](LICENSE)  [![Build Status](https://travis-ci.com/isotes/vs-utils.svg?branch=master)](https://travis-ci.com/isotes/vs-utils)  [![Javadoc](https://img.shields.io/badge/docs-javadoc-blue)](https://isotes.github.io/javadoc/vs-utils-1.0.0/)  [![Maven Central](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fio%2Fgithub%2Fisotes%2Fvs-utils%2Fmaven-metadata.xml)](https://search.maven.org/search?q=g:io.github.isotes%20a:vs-utils)

Library to work with [MSBuild](https://github.com/microsoft/msbuild) solution and project files based on the Java [model](https://github.com/isotes/vs-model) generated from the official XML Schema files.


## Overview
The scope of this project is rather limited and its existence stems mostly from problems of using the official .net API provided by Microsoft. The package has been used as a building block for migrating and normalizing VC++ projects and performing temporary changes in the context of Continuous Integration workloads. It has not been tested with any other project types. No MSBuild installation is required.

The [model](https://github.com/isotes/vs-model) is generated with [XMLBeans](https://xmlbeans.apache.org/) based on the official XML Schema files provided for [MSBuild](https://github.com/microsoft/msbuild) project files.

One the one hand, this projects adds support for reading and writing solution files. On the other hand, it contains helper classes to simplify working with project files, especially VC++ `*.vcxproj` files, and to work around idiosyncrasies of the upstream model and applications. For example, the write routine tries to make sure files are as close to the ones written by Visual Studio as possible, even if the upstream handling of empty tags is inconsistent. Furthermore, based on the upstream schema, most elements may occur multiple times even if semantically only one is possible, e.g., for specifying the compiler warning level. MSBuild seems to use the last occurrence while evaluating the project. Moreover, most simple elements that seem to be strings are simply specified as `xs:any`. The utility class [X](https://isotes.github.io/javadoc/vs-utils-1.0.0/io/github/isotes/vs/utils/X.html) contains methods to make working with these elements more convenient.


## Use
See [Maven Central](https://search.maven.org/search?q=g:io.github.isotes%20a:vs-utils) for the current Maven coordinates. This library requires Java 8 but should also work with newer versions and has a single dependency on the [vs-model](https://search.maven.org/search?q=g:io.github.isotes%20a:vs-model) package.

The following introductory example shows how to modify project files. More information is in the [API documentation](https://isotes.github.io/javadoc/vs-utils-1.0.0/).

```java
VsSolution solution = new VsSolution(solutionPath));
for (VsSolutionProject vsp : solution.projects) {
    ProjectWrapper project = new ProjectWrapper(vsp.path);
    for (ProjectConfiguration projectConfiguration : project.configurations()) {
        ConfigurationWrapper cfg = projectConfiguration.resolve();
        cfg.propertyGroup.put("PlatformToolset", "v142");
        X.set(cfg.clCompile.getMultiProcessorCompilationList(), "true");
        if (X.string(cfg.clCompile.getWarningLevelList()).orElse("").equals("Level4")) {
            X.set(cfg.clCompile.getDisableSpecificWarningsList(), "4710;4711");
        }
    }
    project.save(project.path);
}
```


## License
[Apache 2.0](LICENSE)

Note: For the unit tests, the Gradle build downloads the [MIT](https://github.com/microsoft/VCSamples/blob/master/license.txt) licensed '[Hilo](https://github.com/microsoft/VCSamples/tree/master/VC2015Samples/Hilo/C%2B%2B)' solution and project files from the [VCSamples](https://github.com/microsoft/VCSamples) repository.
