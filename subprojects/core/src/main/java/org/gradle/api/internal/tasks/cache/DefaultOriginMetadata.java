/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.internal.tasks.cache;

public class DefaultOriginMetadata implements OriginMetadata {
    private final String path;
    private final String type;
    private final String gradleVersion;
    private final long creationTime;

    final static OriginMetadata NULL = new DefaultOriginMetadata("unknown", "unknown", "unknown", 0);

    public DefaultOriginMetadata(String path, String type, String gradleVersion, long creationTime) {
        this.path = path;
        this.type = type;
        this.gradleVersion = gradleVersion;
        this.creationTime = creationTime;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getGradleVersion() {
        return gradleVersion;
    }
}