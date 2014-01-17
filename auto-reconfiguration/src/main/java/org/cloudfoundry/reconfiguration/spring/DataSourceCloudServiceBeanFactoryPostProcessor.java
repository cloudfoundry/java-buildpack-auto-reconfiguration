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
import org.springframework.cloud.Cloud;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

final class DataSourceCloudServiceBeanFactoryPostProcessor extends AbstractCloudServiceBeanFactoryPostProcessor {

    private static final String BEAN_CLASS = "javax.sql.DataSource";

    private static final String SERVICE_BEAN_NAME = "__cloudDataSource";

    private static final String DELEGATING_DATA_SOURCE_CLASS = "org.springframework.jdbc.datasource" +
            ".DelegatingDataSource";

    private final List<BeanFactoryPostProcessor> ormBeanFactoryPostProcessors;

    DataSourceCloudServiceBeanFactoryPostProcessor(Cloud cloud) {
        super(cloud);
        this.ormBeanFactoryPostProcessors = Arrays.<BeanFactoryPostProcessor>asList(
                new HibernateCloudServiceBeanFactoryPostProcessor(cloud),
                new JpaCloudServiceBeanFactoryPostProcessor(cloud));
    }

    @Override
    protected void filterBeanNames(ConfigurableListableBeanFactory beanFactory, Set<String> beanNames) {
        if (ClassUtils.isPresent(DELEGATING_DATA_SOURCE_CLASS, null)) {
            Set<String> delegatingBeanNames = Sets.asSet(beanFactory.getBeanNamesForType(
                    ClassUtils.resolveClassName(DELEGATING_DATA_SOURCE_CLASS, null), true, false));

            beanNames.removeAll(delegatingBeanNames);
        }
    }

    @Override
    protected String getBeanClass() {
        return BEAN_CLASS;
    }

    @Override
    protected String getServiceBeanName() {
        return SERVICE_BEAN_NAME;
    }

    @Override
    protected void postReconfiguration(ConfigurableListableBeanFactory beanFactory) {
        for (BeanFactoryPostProcessor postProcessor : this.ormBeanFactoryPostProcessors) {
            postProcessor.postProcessBeanFactory(beanFactory);
        }
    }

}
