/*
 * Copyright 2020 the original author or authors.
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
package org.openrewrite.maven.search;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.maven.tree.Maven;
import org.openrewrite.xml.marker.XmlSearchResult;
import org.openrewrite.xml.tree.Xml;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.openrewrite.Tree.randomId;

@EqualsAndHashCode(callSuper = true)
@Value
public class FindDependency extends Recipe {

    @Option(displayName = "Group",
            description = "The first part of a dependency coordinate 'com.google.guava:guava:VERSION'.",
            example = "com.google.guava")
    String groupId;

    @Option(displayName = "Artifact",
            description = "The second part of a dependency coordinate 'com.google.guava:guava:VERSION'.",
            example = "guava")
    String artifactId;

    UUID id = randomId();

    public static Set<Xml.Tag> find(Maven maven, String groupId, String artifactId) {
        Set<Xml.Tag> ds = new HashSet<>();
        new MavenVisitor() {
            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext context) {
                if (isDependencyTag(groupId, artifactId)) {
                    ds.add(tag);
                }
                return super.visitTag(tag, context);
            }
        }.visit(maven, new InMemoryExecutionContext());
        return ds;
    }

    @Override
    public String getDisplayName() {
        return "Find Maven dependency";
    }

    @Override
    public String getDescription() {
        return "Finds first-order dependency uses, i.e. dependencies that are defined directly in a project.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor() {
            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext context) {
                if (isDependencyTag(groupId, artifactId)) {
                    return tag.withMarkers(tag.getMarkers().addIfAbsent(new XmlSearchResult(id, FindDependency.this)));
                }
                return super.visitTag(tag, context);
            }
        };
    }
}
