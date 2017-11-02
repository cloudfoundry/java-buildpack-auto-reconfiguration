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

package org.cloudfoundry.reconfiguration;

import org.junit.Test;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.ServiceInfo;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractCloudServiceBeanFactoryPostProcessorTest {

    @Test
    public final void hasMultipleBeans() {
        Cloud cloud = MockCloudHolder.create();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("test-bean-1", BeanDefinitionBuilder.genericBeanDefinition(getServiceClass()).getBeanDefinition());
        beanFactory.registerBeanDefinition("test-bean-2", BeanDefinitionBuilder.genericBeanDefinition(getServiceClass()).getBeanDefinition());

        getBeanFactoryPostProcessor(cloud).postProcessBeanFactory(beanFactory);

        assertThat(beanFactory.getBeanNamesForType(getServiceClass())).containsOnly("test-bean-1", "test-bean-2");
    }

    @Test
    public final void hasMultipleCloudServices() {
        Cloud cloud = MockCloudHolder.create(getServiceInfo("test-cloud-service-1"), getServiceInfo("test-cloud-service-2"));
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("test-bean-1", BeanDefinitionBuilder.genericBeanDefinition(getServiceClass()).getBeanDefinition());

        getBeanFactoryPostProcessor(cloud).postProcessBeanFactory(beanFactory);

        assertThat(beanFactory.getBeanNamesForType(getServiceClass())).containsOnly("test-bean-1");
    }

    @Test
    public final void hasZeroBeans() {
        Cloud cloud = MockCloudHolder.create();
        ConfigurableListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        getBeanFactoryPostProcessor(cloud).postProcessBeanFactory(beanFactory);

        assertThat(beanFactory.getBeanNamesForType(getServiceClass())).isEmpty();
    }

    @Test
    public final void hasZeroCloudServices() {
        Cloud cloud = MockCloudHolder.create();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("test-bean-1", BeanDefinitionBuilder.genericBeanDefinition(getServiceClass()).getBeanDefinition());

        getBeanFactoryPostProcessor(cloud).postProcessBeanFactory(beanFactory);

        assertThat(beanFactory.getBeanNamesForType(getServiceClass())).containsOnly("test-bean-1");
    }

    @Test
    public final void reconfigured() {
        Cloud cloud = MockCloudHolder.create(getServiceInfo("test-cloud-service-1"));
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("test-bean-1", BeanDefinitionBuilder.genericBeanDefinition(getServiceClass()).getBeanDefinition());

        getBeanFactoryPostProcessor(cloud).postProcessBeanFactory(beanFactory);

        assertThat(beanFactory.getBeanNamesForType(getServiceClass())).containsOnly("test-cloud-service-1");
    }

    abstract BeanFactoryPostProcessor getBeanFactoryPostProcessor(Cloud cloud);

    abstract Class<?> getServiceClass();

    abstract ServiceInfo getServiceInfo(String id);

}
