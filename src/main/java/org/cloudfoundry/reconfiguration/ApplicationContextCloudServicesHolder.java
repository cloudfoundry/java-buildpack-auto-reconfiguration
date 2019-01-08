/*
 * Copyright 2011-2019 the original author or authors.
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

package org.cloudfoundry.reconfiguration;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cloudfoundry.reconfiguration.BeanUtils.hasBeansOfType;

final class ApplicationContextCloudServicesHolder implements CloudServicesHolder {

    private static final Logger LOGGER = Logger.getLogger(ApplicationContextCloudServicesHolder.class.getName());

    private final boolean isUsingCloudServices;

    ApplicationContextCloudServicesHolder(ApplicationContext applicationContext) {
        this.isUsingCloudServices = isUsingCloudServices(applicationContext);
    }

    @Override
    public void withCloudServices(Runnable ifUsingCloudServices, Runnable ifNotUsingCloudServices) {
        if (this.isUsingCloudServices) {
            ifUsingCloudServices.run();
        } else {
            ifNotUsingCloudServices.run();
        }
    }

    private static Stream<String> getCloudServiceClasses(Resource resource) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return in.lines()
                .map(String::trim)
                .collect(Collectors.toSet())
                .stream();
        } catch (IOException e) {
            LOGGER.warning(String.format("Unable to read cloud service classes from %s", resource.getFilename()));
            return Stream.empty();
        }
    }

    private static boolean isClassPresent(String className) {
        return ClassUtils.isPresent(className, null);
    }

    private static boolean isUsingCloudServices(ApplicationContext applicationContext) {
        try {
            return Arrays.stream(applicationContext.getResources("classpath*:/META-INF/cloud/cloud-services"))
                .flatMap(ApplicationContextCloudServicesHolder::getCloudServiceClasses)
                .filter(ApplicationContextCloudServicesHolder::isClassPresent)
                .anyMatch(klass -> hasBeansOfType(applicationContext, klass));
        } catch (IOException e) {
            LOGGER.warning("Unable to read cloud service classes");
            return false;
        }
    }

}
