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

import org.cloudfoundry.reconfiguration.util.CloudUtils;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.CloudTestUtil;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.Ordered;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public abstract class AbstractCloudServiceBeanFactoryPostProcessorTest<T> {

    protected final Class<T> beanClass;

    protected final String contextStem;

    protected final ServiceInfo serviceInfo;

    private final ApplicationContext applicationContext = mock(ApplicationContext.class);

    private final CloudUtils cloudUtils = mock(CloudUtils.class);

    protected AbstractCloudServiceBeanFactoryPostProcessorTest(Class<T> beanClass, String contextStem,
                                                               ServiceInfo serviceInfo) {
        this.beanClass = beanClass;
        this.contextStem = contextStem;
        this.serviceInfo = serviceInfo;
    }

    @Test
    public final void getOrder() {
        assertEquals(Ordered.LOWEST_PRECEDENCE, ((Ordered) getInstance(null, null)).getOrder());
    }

    @Test
    public final void reconfigure() {
        assertReconfigured("classpath:%s-single.xml");
    }

    @Test
    public final void noReconfigureUsingCloudServices() {
        CloudUtils cloudUtils = getCloudUtils(this.serviceInfo);
        when(cloudUtils.isUsingCloudServices(this.applicationContext)).thenReturn(true);
        BeanFactoryPostProcessor beanFactoryPostProcessor = getInstance(this.applicationContext, cloudUtils);
        String contextLocation = String.format("classpath:%s-single.xml", this.contextStem);

        ApplicationContext applicationContext = getApplicationContext(contextLocation, beanFactoryPostProcessor);

        assertLocalConfiguration(applicationContext.getBean(this.beanClass));
    }

    @Test
    public final void noReconfigureNoBeans() {
        CloudUtils cloudUtils = getCloudUtils(this.serviceInfo);
        BeanFactoryPostProcessor beanFactoryPostProcessor = getInstance(this.applicationContext, cloudUtils);
        String contextLocation = String.format("classpath:%s-none.xml", this.contextStem);

        ApplicationContext applicationContext = getApplicationContext(contextLocation, beanFactoryPostProcessor);

        Map<String, T> beans = applicationContext.getBeansOfType(this.beanClass);
        assertEquals(0, beans.size());
    }

    @Test
    public final void noReconfigureMultipleBeans() {
        CloudUtils cloudUtils = getCloudUtils(this.serviceInfo);
        BeanFactoryPostProcessor beanFactoryPostProcessor = getInstance(this.applicationContext, cloudUtils);
        String contextLocation = String.format("classpath:%s-multiple.xml", this.contextStem);

        ApplicationContext applicationContext = getApplicationContext(contextLocation, beanFactoryPostProcessor);

        Map<String, T> factories = applicationContext.getBeansOfType(this.beanClass);
        assertEquals(2, factories.size());

        for (T bean : factories.values()) {
            assertLocalConfiguration(bean);
        }
    }

    @Test
    public final void noReconfigureNoServices() {
        CloudUtils cloudUtils = getCloudUtils();
        BeanFactoryPostProcessor beanFactoryPostProcessor = getInstance(this.applicationContext, cloudUtils);
        String contextLocation = String.format("classpath:%s-single.xml", this.contextStem);

        ApplicationContext applicationContext = getApplicationContext(contextLocation, beanFactoryPostProcessor);

        assertLocalConfiguration(applicationContext.getBean(this.beanClass));
    }

    @Test
    public final void noReconfigureMultipleServices() {
        CloudUtils cloudUtils = getCloudUtils(this.serviceInfo, this.serviceInfo);
        BeanFactoryPostProcessor beanFactoryPostProcessor = getInstance(this.applicationContext, cloudUtils);
        String contextLocation = String.format("classpath:%s-single.xml", this.contextStem);

        ApplicationContext applicationContext = getApplicationContext(contextLocation, beanFactoryPostProcessor);

        assertLocalConfiguration(applicationContext.getBean(this.beanClass));
    }

    protected final void assertReconfigured(String contextLocationFormat) {
        CloudUtils cloudUtils = getCloudUtils(this.serviceInfo);
        BeanFactoryPostProcessor beanFactoryPostProcessor = getInstance(this.applicationContext, cloudUtils);
        String contextLocation = String.format(contextLocationFormat, this.contextStem);

        ApplicationContext applicationContext = getApplicationContext(contextLocation, beanFactoryPostProcessor);

        assertServiceConfiguration(applicationContext.getBean(this.beanClass));
    }

    protected final ApplicationContext getApplicationContext(String contextLocation,
                                                             BeanFactoryPostProcessor beanFactoryPostProcessor) {
        GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
        applicationContext.load(contextLocation);
        applicationContext.addBeanFactoryPostProcessor(beanFactoryPostProcessor);
        applicationContext.refresh();

        return applicationContext;
    }

    protected final CloudUtils getCloudUtils(ServiceInfo... serviceInfos) {
        CloudFactory cloudFactory = new CloudFactory();
        cloudFactory.registerCloudConnector(CloudTestUtil.getTestCloudConnector(serviceInfos));

        when(this.cloudUtils.getCloudFactory()).thenReturn(cloudFactory);

        return this.cloudUtils;
    }

    protected abstract BeanFactoryPostProcessor getInstance(ApplicationContext applicationContext,
                                                            CloudUtils cloudUtils);

    protected abstract void assertLocalConfiguration(T instance);

    protected abstract void assertServiceConfiguration(T instance);

}
