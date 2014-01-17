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

import org.springframework.cloud.CloudException;
import org.springframework.cloud.CloudFactory;

/**
 * Standard implementation of {@link CloudUtils}
 */
public final class StandardCloudUtils implements CloudUtils {

    private final Object monitor = new Object();

    private volatile CloudFactory cloudFactory;

    /**
     * Creates a new instance
     */
    public StandardCloudUtils() {
    }

    StandardCloudUtils(CloudFactory cloudFactory) {
        this.cloudFactory = cloudFactory;
    }

    @Override
    public CloudFactory getCloudFactory() {
        synchronized (this.monitor) {
            if (this.cloudFactory == null) {
                this.cloudFactory = new CloudFactory();
            }

            return this.cloudFactory;
        }
    }

    @Override
    public Boolean isInCloud() {
        try {
            getCloudFactory().getCloud();
            return true;
        } catch (CloudException e) {
            return false;
        }
    }

}
