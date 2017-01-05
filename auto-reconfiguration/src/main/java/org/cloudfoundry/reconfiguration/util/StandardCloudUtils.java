/*
 * Copyright 2011-2017 the original author or authors.
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
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Standard implementation of {@link CloudUtils}
 */
public final class StandardCloudUtils implements CloudUtils {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final Object monitor = new Object();

    private volatile CloudFactory cloudFactory;

    private volatile Boolean usingCloudServices;

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

    @Override
    public Boolean isUsingCloudServices(ApplicationContext applicationContext) {
        synchronized (this.monitor) {
            if (this.usingCloudServices == null) {
                this.usingCloudServices = calculateUsingCloudServices(applicationContext);
            }

            return this.usingCloudServices;
        }
    }

    private Boolean calculateUsingCloudServices(ApplicationContext applicationContext) {
        try {
            for (Resource resource : applicationContext.getResources("classpath*:/META-INF/cloud/cloud-services")) {
                for (String cloudServiceClass : getCloudServiceClasses(resource)) {
                    if (hasBeanOfType(applicationContext, cloudServiceClass)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            this.logger.warning("Unable to read cloud service classes");
        }

        return false;
    }

    private Boolean hasBeanOfType(ApplicationContext applicationContext, String cloudServiceClassName) {
        if (ClassUtils.isPresent(cloudServiceClassName, null)) {
            Class<?> cloudServiceClass = ClassUtils.resolveClassName(cloudServiceClassName, null);
            return applicationContext.getBeanNamesForType(cloudServiceClass, true, false).length != 0;
        }

        return false;
    }

    private Set<String> getCloudServiceClasses(Resource resource) {
        Set<String> cloudServiceClasses = new HashSet<String>();

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(resource.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                cloudServiceClasses.add(line.trim());
            }
        } catch (IOException e) {
            this.logger.warning(String.format("Unable to read cloud service classes from %s", resource.getFilename()));
        } finally {
            IoUtils.closeQuietly(in);
        }

        return cloudServiceClasses;
    }

}
