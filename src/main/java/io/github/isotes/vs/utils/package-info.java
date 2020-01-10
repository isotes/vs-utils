/*
 * Copyright (c) 2020 Robert Sauter
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Utilities to work with <a href="https://github.com/microsoft/msbuild">MSBuild</a> solution and project files based
 * on the generated <a href="https://xmlbeans.apache.org/">XMLBeans</a> classes from the
 * <a href="https://github.com/microsoft/msbuild/blob/master/src/MSBuild/Microsoft.Build.xsd">MSBuild XML Schema</a>
 * (<a href="https://github.com/isotes/vs-model">packaged</a> on
 * <a href="https://search.maven.org/search?q=g:io.github.isotes%20a:vs-model">Maven Central</a>).
 *
 * <p>See the following starting points</p>
 * <ul>
 *     <li>To work with project files: {@link io.github.isotes.vs.utils.ProjectWrapper}</li>
 *     <li>To work with solution files: {@link io.github.isotes.vs.utils.VsSolution}</li>
 *     <li>To work with a configuration (e.g., Release|Win32) in C/C++ project files: {@link io.github.isotes.vs.utils.ConfigurationWrapper}</li>
 *     <li>To work with elements in project files: {@link io.github.isotes.vs.utils.X}</li>
 * </ul>
 *
 * <p>Based on the upstream schema, most elements may occur multiple times even if semantically only one is possible,
 * e.g., for specifying the compiler warning level. It seems that in the case of multiple occurrences that last one
 * takes precedent. Moreover, most simple elements that seem to be strings are simply specified as {@code xs:any}. The
 * class {@link io.github.isotes.vs.utils.X} contains methods to make working with these instances more convenient.</p>
 */
package io.github.isotes.vs.utils;
