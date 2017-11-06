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

package org.gradle.integtests.resolve.maven

import org.gradle.integtests.fixtures.AbstractHttpDependencyResolutionTest
import org.gradle.integtests.fixtures.resolve.ResolveTestFixture

class MavenBomResolveIntegrationTest extends AbstractHttpDependencyResolutionTest {
    def resolve = new ResolveTestFixture(buildFile)

    def setup() {
        resolve.prepare()
        settingsFile << """
            rootProject.name = 'testproject'
        """
        buildFile << """
            repositories { maven { url "${mavenHttpRepo.uri}" } }
            configurations { compile }
        """
    }

    def "interprets dependencies declared in BOMs of transitive dependencies as optional dependencies"() {
        given:
        def moduleB = mavenHttpRepo.module('group', 'moduleB', '1.0').allowAll().publish()
        def moduleCVersion1 = mavenHttpRepo.module('group', 'moduleC', '1.0').allowAll().publish()
        mavenHttpRepo.module('group', 'moduleC', '2.0').allowAll().publish()
        mavenHttpRepo.module('group', 'moduleA', '1.0').allowAll().dependsOn(moduleB).dependsOn(moduleCVersion1).publish()

        moduleB.pomFile.text = moduleB.pomFile.text.replace("</project>", '''
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>group</groupId>
            <artifactId>moduleC</artifactId>
            <version>2.0</version>
        </dependency>
    </dependencies>
</dependencyManagement>
</project>
''')
        buildFile << """
            dependencies {
                compile "group:moduleA:1.0"
            }
        """

        when:
        succeeds 'checkDep'

        then:
        resolve.expectGraph {
            root(':', ':testproject:') {
                module("group:moduleA:1.0") {
                    module('group:moduleB:1.0') {
                        module('group:moduleC:2.0')
                    }
                    edge('group:moduleC:1.0', 'group:moduleC:2.0').byConflictResolution()
                }
            }
        }

    }
}
