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

import org.cloudfoundry.reconfiguration.util.PropertyAugmenter;
import org.cloudfoundry.reconfiguration.util.StandardPropertyAugmenter;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.logging.Logger;

abstract class AbstractHibernateBasedCloudServiceBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final ManagedProperties MYSQL_PROPERTIES;

    private static final ManagedProperties POSTGRES_PROPERTIES;

    static {
        MYSQL_PROPERTIES = new ManagedProperties();
        MYSQL_PROPERTIES.put(new TypedStringValue("hibernate.dialect"),
                new TypedStringValue("org.hibernate.dialect.MySQLDialect"));

        POSTGRES_PROPERTIES = new ManagedProperties();
        POSTGRES_PROPERTIES.put(new TypedStringValue("hibernate.dialect"),
                new TypedStringValue("org.hibernate.dialect.PostgreSQLDialect"));
    }

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final List<String> beanClasses;

    private final Cloud cloud;

    private final PropertyAugmenter propertyAugmenter;

    private final String propertyName;

    protected AbstractHibernateBasedCloudServiceBeanFactoryPostProcessor(List<String> beanClasses, Cloud cloud,
                                                                         String propertyName) {
        this(beanClasses, cloud, new StandardPropertyAugmenter(), propertyName);
    }

    AbstractHibernateBasedCloudServiceBeanFactoryPostProcessor(List<String> beanClasses, Cloud cloud,
                                                               PropertyAugmenter propertyAugmenter,
                                                               String propertyName) {
        this.beanClasses = beanClasses;
        this.cloud = cloud;
        this.propertyAugmenter = propertyAugmenter;
        this.propertyName = propertyName;
    }

    @Override
    public final void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        for (ServiceInfo serviceInfo : this.cloud.getServiceInfos(DataSource.class)) {
            if (serviceInfo instanceof MysqlServiceInfo) {
                augment(beanFactory, MYSQL_PROPERTIES);
            } else if (serviceInfo instanceof PostgresqlServiceInfo) {
                augment(beanFactory, POSTGRES_PROPERTIES);
            }
        }
    }

    protected void augment(ConfigurableListableBeanFactory beanFactory, ManagedProperties additionalProperties) {
        for (String beanClass : this.beanClasses) {
            if (ClassUtils.isPresent(beanClass, null)) {
                this.logger.info(String.format("Auto-reconfiguring beans of type %s", beanClass));

                this.propertyAugmenter.augment(beanFactory, ClassUtils.resolveClassName(beanClass, null),
                        this.propertyName, additionalProperties);
            }
        }
    }

}
