/*
 * Copyright 2011-2014 the original author or authors.
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

package org.cloudfoundry.reconfiguration.util;

import org.springframework.cloud.CloudFactory;

/**
 * Utilities for dealing with {@link CloudFactory}s
 */
public interface CloudUtils {

    /**
     * Get an existing {@link CloudFactory}. If the {@link CloudFactory} does not already exist, it is created and then
     * returned.
     *
     * @return the existing {@link CloudFactory} instance if one exists.  If an instance does not exist, a new one is
     * created and returned.
     */
    CloudFactory getCloudFactory();

    /**
     * Whether the current application is in a cloud
     *
     * @return {@code true} if the application is running in a cloud, {@code false} otherwise
     */

    Boolean isInCloud();

}
