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

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.Cloud;
import org.springframework.util.ClassUtils;

import java.util.logging.Logger;

import static org.cloudfoundry.reconfiguration.BeanUtils.getCountOfBeansForType;
import static org.cloudfoundry.reconfiguration.BeanUtils.getSingleBeanName;

final class CloudServiceUtils {

    private static final Logger LOGGER = Logger.getLogger(CloudServiceUtils.class.getName());

    private CloudServiceUtils() {
    }

    static void replaceBeanOfType(String className, DefaultListableBeanFactory beanFactory, Cloud cloud) {
        if (!ClassUtils.isPresent(className, null)) {
            return;
        }

        Class<?> klass = ClassUtils.resolveClassName(className, null);

        if (!hasSingleBean(beanFactory, klass) || !hasSingleCloudService(cloud, klass)) {
            return;
        }

        String beanName = getSingleBeanName(beanFactory, klass);
        String cloudServiceBeanName = getSingleCloudServiceName(cloud, klass);

        beanFactory.registerSingleton(cloudServiceBeanName, getSingleCloudService(cloud, klass));
        beanFactory.removeBeanDefinition(beanName);
        beanFactory.registerAlias(cloudServiceBeanName, beanName);

        LOGGER.info(String.format("'%s' bean of type with '%s' reconfigured with '%s' bean", beanName, className, cloudServiceBeanName));
    }

    private static Object getSingleCloudService(Cloud cloud, Class<?> klass) {
        return cloud.getSingletonServiceConnector(klass, null);
    }

    private static String getSingleCloudServiceName(Cloud cloud, Class<?> klass) {
        return cloud.getServiceInfos(klass).get(0).getId();
    }

    private static boolean hasSingleBean(ListableBeanFactory beanFactory, Class<?> klass) {
        int size = getCountOfBeansForType(beanFactory, klass);

        if (size == 0) {
            LOGGER.fine(String.format("Skipping reconfiguration of '%s' because there were no beans of type", klass.getName()));
            return false;
        }

        if (size > 1) {
            LOGGER.fine(String.format("Skipping reconfiguration of '%s' because there multiple beans of type", klass.getName()));
            return false;
        }

        return true;
    }

    private static boolean hasSingleCloudService(Cloud cloud, Class<?> klass) {
        int size = cloud.getServiceInfos(klass).size();

        if (size == 0) {
            LOGGER.fine(String.format("Skipping reconfiguration of '%s' because there were no services of type", klass.getName()));
            return false;
        }

        if (size > 1) {
            LOGGER.fine(String.format("Skipping reconfiguration of '%s' because there multiple services of type", klass.getName()));
            return false;
        }

        return true;
    }

}
