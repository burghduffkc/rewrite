/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.gradle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openrewrite.DocumentExample;
import org.openrewrite.Issue;
import org.openrewrite.gradle.marker.GradleProject;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;

class ChangeDependencyGroupIdTest implements RewriteTest {

    @DocumentExample
    @Test
    void worksWithEmptyStringConfig() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupId("org.springframework.boot", "spring-boot-starter", "org.newboot", "")),
          buildGradle(
            """
              plugins {
                  id 'java-library'
              }
              
              repositories {
                  mavenCentral()
              }
              
              dependencies {
                  implementation 'org.springframework.boot:spring-boot-starter:2.5.4'
              }
              """,
            """
              plugins {
                  id 'java-library'
              }
              
              repositories {
                  mavenCentral()
              }
              
              dependencies {
                  implementation 'org.newboot:spring-boot-starter:2.5.4'
              }
              """
          )
        );
    }

    @ParameterizedTest
    @CsvSource(value = {"org.openrewrite:rewrite-core", "*:*"}, delimiterString = ":")
    void findDependency(String group, String artifact) {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupId(group, artifact, "org.dewrite", null))
            .beforeRecipe(withToolingApi()),
          buildGradle(
            """
              plugins {
                  id 'java-library'
              }
              
              repositories {
                  mavenCentral()
              }
              
              dependencies {
                  api 'org.openrewrite:rewrite-core:latest.release'
                  api "org.openrewrite:rewrite-core:latest.release"
              }
              """,
            """
              plugins {
                  id 'java-library'
              }
              
              repositories {
                  mavenCentral()
              }
              
              dependencies {
                  api 'org.dewrite:rewrite-core:latest.release'
                  api "org.dewrite:rewrite-core:latest.release"
              }
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(cu.getMarkers().findFirst(GradleProject.class))
                .map(gp -> gp.getConfiguration("api"))
                .map(conf -> conf.findRequestedDependency("org.dewrite", "rewrite-core"))
                .as("Requested dependency model should have been updated to have groupId org.dewrite")
                .isPresent())
          )
        );
    }

    @ParameterizedTest
    @CsvSource(value = {"org.openrewrite:rewrite-core", "*:*"}, delimiterString = ":")
    void findMapStyleDependency(String group, String artifact) {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupId(group, artifact, "org.dewrite", null)),
          buildGradle(
            """
              plugins {
                  id 'java-library'
              }
              
              repositories {
                  mavenCentral()
              }
              
              dependencies {
                  api group: 'org.openrewrite', name: 'rewrite-core', version: 'latest.release'
                  api group: "org.openrewrite", name: "rewrite-core", version: "latest.release"
              }
              """,
            """
              plugins {
                  id 'java-library'
              }
              
              repositories {
                  mavenCentral()
              }
              
              dependencies {
                  api group: 'org.dewrite', name: 'rewrite-core', version: 'latest.release'
                  api group: "org.dewrite", name: "rewrite-core", version: "latest.release"
              }
              """
          )
        );
    }

    @ParameterizedTest
    @CsvSource(value = {"org.openrewrite:rewrite-core", "*:*"}, delimiterString = ":")
    void worksWithoutVersion(String group, String artifact) {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupId(group, artifact, "org.dewrite", null)),
          buildGradle(
            """
              plugins {
                  id 'java-library'
              }
              
              repositories {
                   mavenCentral()
              }
              
              dependencies {
                  api 'org.openrewrite:rewrite-core'
                  api "org.openrewrite:rewrite-core"
                  api group: 'org.openrewrite', name: 'rewrite-core'
                  api group: "org.openrewrite", name: "rewrite-core"
              }
              """,
            """
              plugins {
                  id 'java-library'
              }
              
              repositories {
                   mavenCentral()
              }
              
              dependencies {
                  api 'org.dewrite:rewrite-core'
                  api "org.dewrite:rewrite-core"
                  api group: 'org.dewrite', name: 'rewrite-core'
                  api group: "org.dewrite", name: "rewrite-core"
              }
              """
          )
        );
    }

    @ParameterizedTest
    @CsvSource(value = {"org.openrewrite:rewrite-core", "*:*"}, delimiterString = ":")
    void worksWithClassifier(String group, String artifact) {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupId(group, artifact, "org.dewrite", null)),
          buildGradle(
            """
              plugins {
                  id 'java-library'
              }
              
              repositories {
                   mavenCentral()
              }
              
              dependencies {
                  api 'org.openrewrite:rewrite-core:latest.release:classifier'
                  api "org.openrewrite:rewrite-core:latest.release:classifier"
                  api group: 'org.openrewrite', name: 'rewrite-core', version: 'latest.release', classifier: 'classifier'
                  api group: "org.openrewrite", name: "rewrite-core", version: "latest.release", classifier: "classifier"
              }
              """,
            """
              plugins {
                  id 'java-library'
              }
              
              repositories {
                   mavenCentral()
              }
              
              dependencies {
                  api 'org.dewrite:rewrite-core:latest.release:classifier'
                  api "org.dewrite:rewrite-core:latest.release:classifier"
                  api group: 'org.dewrite', name: 'rewrite-core', version: 'latest.release', classifier: 'classifier'
                  api group: "org.dewrite", name: "rewrite-core", version: "latest.release", classifier: "classifier"
              }
              """
          )
        );
    }

    @ParameterizedTest
    @CsvSource(value = {"org.openrewrite:rewrite-core", "*:*"}, delimiterString = ":")
    void worksWithExt(String group, String artifact) {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupId(group, artifact, "org.dewrite", null)),
          buildGradle(
            """
              plugins {
                  id 'java-library'
              }
              
              repositories {
                   mavenCentral()
              }
              
              dependencies {
                  api 'org.openrewrite:rewrite-core@ext'
                  api "org.openrewrite:rewrite-core@ext"
                  api 'org.openrewrite:rewrite-core:latest.release@ext'
                  api "org.openrewrite:rewrite-core:latest.release@ext"
                  api 'org.openrewrite:rewrite-core:latest.release:classifier@ext'
                  api "org.openrewrite:rewrite-core:latest.release:classifier@ext"
                  api group: 'org.openrewrite', name: 'rewrite-core', ext: 'ext'
                  api group: "org.openrewrite", name: "rewrite-core", ext: "ext"
                  api group: 'org.openrewrite', name: 'rewrite-core', version: 'latest.release', ext: 'ext'
                  api group: "org.openrewrite", name: "rewrite-core", version: "latest.release", ext: "ext"
                  api group: 'org.openrewrite', name: 'rewrite-core', version: 'latest.release', classifier: 'classifier', ext: 'ext'
                  api group: "org.openrewrite", name: "rewrite-core", version: "latest.release", classifier: "classifier", ext: "ext"
              }
              """,
            """
              plugins {
                  id 'java-library'
              }
              
              repositories {
                   mavenCentral()
              }
              
              dependencies {
                  api 'org.dewrite:rewrite-core@ext'
                  api "org.dewrite:rewrite-core@ext"
                  api 'org.dewrite:rewrite-core:latest.release@ext'
                  api "org.dewrite:rewrite-core:latest.release@ext"
                  api 'org.dewrite:rewrite-core:latest.release:classifier@ext'
                  api "org.dewrite:rewrite-core:latest.release:classifier@ext"
                  api group: 'org.dewrite', name: 'rewrite-core', ext: 'ext'
                  api group: "org.dewrite", name: "rewrite-core", ext: "ext"
                  api group: 'org.dewrite', name: 'rewrite-core', version: 'latest.release', ext: 'ext'
                  api group: "org.dewrite", name: "rewrite-core", version: "latest.release", ext: "ext"
                  api group: 'org.dewrite', name: 'rewrite-core', version: 'latest.release', classifier: 'classifier', ext: 'ext'
                  api group: "org.dewrite", name: "rewrite-core", version: "latest.release", classifier: "classifier", ext: "ext"
              }
              """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite/issues/3239")
    @Test
    void worksWithGString() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupId("javax.validation", "validation-api", "jakarta.validation", null)),
          buildGradle(
            """
              dependencies {
                  def jakartaVersion = "2.0.1.Final"
                  implementation "javax.validation:validation-api:${jakartaVersion}"
              }
              """,
            """
              dependencies {
                  def jakartaVersion = "2.0.1.Final"
                  implementation "jakarta.validation:validation-api:${jakartaVersion}"
              }
              """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite/issues/3227")
    @Test
    void worksWithPlatform() {
        rewriteRun(
          spec -> spec.recipe(new ChangeDependencyGroupId("org.optaplanner", "*", "ai.timefold.solver", null)),
          buildGradle(
            """
              plugins {
                  id 'java-library'
              }
                
              repositories {
                  mavenCentral()
              }
                            
              dependencies {
                  implementation platform("org.optaplanner:optaplanner-bom:9.37.0.Final")
                  implementation "org.optaplanner:optaplanner-core"
              }
              """,
            """
              plugins {
                  id 'java-library'
              }
                
              repositories {
                  mavenCentral()
              }
                            
              dependencies {
                  implementation platform("ai.timefold.solver:optaplanner-bom:9.37.0.Final")
                  implementation "ai.timefold.solver:optaplanner-core"
              }
              """
          )
        );
    }
}
