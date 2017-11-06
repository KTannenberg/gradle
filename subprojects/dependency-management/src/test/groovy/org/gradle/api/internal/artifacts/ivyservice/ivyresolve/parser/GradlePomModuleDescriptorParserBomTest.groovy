/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser

import org.gradle.internal.component.external.descriptor.MavenScope

import static org.gradle.api.internal.component.ArtifactType.MAVEN_POM

class GradlePomModuleDescriptorParserBomTest extends AbstractGradlePomModuleDescriptorParserTest {

    def "interprets dependencies declared in BOMs as optional dependencies"() {
        given:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-a</groupId>
    <artifactId>module-a</artifactId>
    <version>1.0</version>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>group-b</groupId>
                <artifactId>module-b</artifactId>
                <version>1.0</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
"""

        when:
        parsePom()

        then:
        def dep = single(metadata.dependencies)
        dep.requested == moduleId('group-b', 'module-b', '1.0')
        dep.scope == MavenScope.Compile
        hasDefaultDependencyArtifact(dep)
        dep.optional
    }

    def "interprets dependencies declared in parent BOMs as optional dependencies"() {
        given:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-a</groupId>
    <artifactId>module-a</artifactId>
    <version>1.0</version>

    <parent>
        <groupId>group-a</groupId>
        <artifactId>parent</artifactId>
        <version>1.0</version>
    </parent>
</project>
"""
        def parent = tmpDir.file("parent.xml") << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-a</groupId>
    <artifactId>parent</artifactId>
    <version>1.0</version>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>group-b</groupId>
                <artifactId>module-b</artifactId>
                <version>1.0</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
"""
        parseContext.getMetaDataArtifact({ it.requested.name == 'parent' }, MAVEN_POM) >> asResource(parent)

        when:
        parsePom()

        then:
        def dep = single(metadata.dependencies)
        dep.requested == moduleId('group-b', 'module-b', '1.0')
        dep.scope == MavenScope.Compile
        hasDefaultDependencyArtifact(dep)
        dep.optional
    }


    def "dependencyManagement block is ignored if dependencies are present"() {
        given:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-a</groupId>
    <artifactId>module-a</artifactId>
    <version>1.0</version>
    
    <dependencies>
        <dependency>
            <groupId>group-b</groupId>
            <artifactId>module-b</artifactId>
            <version>1.0</version>
        </dependency>
    </dependencies>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>group-c</groupId>
                <artifactId>module-c</artifactId>
                <version>2.0</version>
            </dependency>
        </dependencies>
        <dependencies>
            <dependency>
                <groupId>group-d</groupId>
                <artifactId>module-d</artifactId>
                <version>2.0</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
"""

        when:
        parsePom()

        then:
        def dep = single(metadata.dependencies)
        dep.requested == moduleId('group-b', 'module-b', '1.0')
        !dep.optional
    }

}
