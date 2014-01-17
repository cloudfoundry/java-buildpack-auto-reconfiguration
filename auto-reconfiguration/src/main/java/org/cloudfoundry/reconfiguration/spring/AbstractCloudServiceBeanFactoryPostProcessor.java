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

package org.cloudfoundry.reconfiguration.spring;

import org.cloudfoundry.reconfiguration.util.Sets;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

abstract class AbstractCloudServiceBeanFactoryPostProcessor implements
        BeanFactoryPostProcessor, Ordered {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final Cloud cloud;

    protected AbstractCloudServiceBeanFactoryPostProcessor(Cloud cloud) {
        this.cloud = cloud;
    }

    @Override
    public final int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public final void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        String beanClass = getBeanClass();

        if (ClassUtils.isPresent(beanClass, null)) {
            this.logger.info(String.format("Auto-reconfiguring beans of type %s", beanClass));

            boolean reconfigured = processBeans((DefaultListableBeanFactory) beanFactory,
                    ClassUtils.resolveClassName(beanClass, null));

            if (reconfigured) {
                postReconfiguration(beanFactory);
            }
        }
    }

    protected abstract String getBeanClass();

    protected abstract String getServiceBeanName();

    protected void filterBeanNames(ConfigurableListableBeanFactory beanFactory, Set<String> beanNames) {
    }

    protected void postReconfiguration(ConfigurableListableBeanFactory beanFactory) {
    }

    private boolean processBeans(DefaultListableBeanFactory beanFactory, Class<?> beanClass) {
        Set<String> beanNames = Sets.asSet(beanFactory.getBeanNamesForType(beanClass, true, false));
        filterBeanNames(beanFactory, beanNames);

        if (beanNames.isEmpty()) {
            this.logger.info(String.format("No beans of type %s found. Skipping auto-reconfiguration.",
                    beanClass.getName()));
        } else if (beanNames.size() > 1) {
            this.logger.warning(String.format("Multiple (%d) beans of type %s found. Skipping auto-reconfiguration.",
                    beanNames.size(), beanClass.getName()));
        } else {
            return processBean(beanFactory, beanClass, beanNames.iterator().next());
        }

        return false;
    }

    private boolean processBean(DefaultListableBeanFactory beanFactory, Class<?> beanClass, String beanName) {
        List<ServiceInfo> serviceInfos = this.cloud.getServiceInfos(beanClass);

        if (serviceInfos.isEmpty()) {
            this.logger.info("No matching service found. Skipping auto-reconfiguration.");
        } else if (serviceInfos.size() > 1) {
            this.logger.warning(String.format("More than one (%d) matching service found. Skipping " +
                    "auto-reconfiguration.", serviceInfos.size()));
        } else {
            return reconfigureBean(beanFactory, beanClass, beanName);
        }

        return false;
    }

    private boolean reconfigureBean(DefaultListableBeanFactory beanFactory, Class<?> beanClass, String beanName) {
        Object serviceConnector = this.cloud.getSingletonServiceConnector(beanClass, null);
        beanFactory.registerSingleton(getServiceBeanName(), serviceConnector);
        beanFactory.removeBeanDefinition(beanName);
        beanFactory.registerAlias(getServiceBeanName(), beanName);

        return true;
    }

}
