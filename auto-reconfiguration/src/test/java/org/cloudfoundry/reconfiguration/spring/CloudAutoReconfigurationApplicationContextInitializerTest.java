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
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public final class CloudAutoReconfigurationApplicationContextInitializerTest {

    private final AbstractApplicationContext applicationContext = mock(AbstractApplicationContext.class);

    private final Cloud cloud = mock(Cloud.class);

    private final CloudFactory cloudFactory = mock(CloudFactory.class);

    private final CloudUtils cloudUtils = mock(CloudUtils.class);

    private final CloudAutoReconfigurationApplicationContextInitializer applicationContextInitializer = new
            CloudAutoReconfigurationApplicationContextInitializer(this.cloudUtils);

    @Test
    public void getOrder() {
        assertEquals(300, this.applicationContextInitializer.getOrder());
    }

    @Test
    public void initializeNoCloud() {
        when(this.cloudUtils.isInCloud()).thenReturn(false);

        this.applicationContextInitializer.initialize(this.applicationContext);

        verifyZeroInteractions(this.applicationContext);
    }

    @Test
    public void initializeAlreadyApplied() {
        when(this.cloudUtils.isInCloud()).thenReturn(true);
        when(this.cloudUtils.getCloudFactory()).thenReturn(this.cloudFactory);
        when(this.cloudFactory.getCloud()).thenReturn(this.cloud);
        when(this.applicationContext.getBeanFactoryPostProcessors()).thenReturn(Arrays.<BeanFactoryPostProcessor>asList(
                new DataSourceCloudServiceBeanFactoryPostProcessor(this.cloud),
                new MongoCloudServiceBeanFactoryPostProcessor(this.cloud),
                new RabbitCloudServiceBeanFactoryPostProcessor(this.cloud),
                new RedisCloudServiceBeanFactoryPostProcessor(this.cloud)
        ));

        this.applicationContextInitializer.initialize(this.applicationContext);

        verify(this.applicationContext, times(0)).addBeanFactoryPostProcessor(isA
                (DataSourceCloudServiceBeanFactoryPostProcessor.class));
        verify(this.applicationContext, times(0)).addBeanFactoryPostProcessor(isA(MongoCloudServiceBeanFactoryPostProcessor
                .class));
        verify(this.applicationContext, times(0)).addBeanFactoryPostProcessor(isA(RabbitCloudServiceBeanFactoryPostProcessor
                .class));
        verify(this.applicationContext, times(0)).addBeanFactoryPostProcessor(isA(RedisCloudServiceBeanFactoryPostProcessor
                .class));
    }

    @Test
    public void initialize() {
        when(this.cloudUtils.isInCloud()).thenReturn(true);
        when(this.cloudUtils.getCloudFactory()).thenReturn(this.cloudFactory);
        when(this.cloudFactory.getCloud()).thenReturn(this.cloud);
        when(this.applicationContext.getBeanFactoryPostProcessors()).thenReturn(Collections
                .<BeanFactoryPostProcessor>emptyList());

        this.applicationContextInitializer.initialize(this.applicationContext);

        verify(this.applicationContext).addBeanFactoryPostProcessor(isA
                (DataSourceCloudServiceBeanFactoryPostProcessor.class));
        verify(this.applicationContext).addBeanFactoryPostProcessor(isA(MongoCloudServiceBeanFactoryPostProcessor
                .class));
        verify(this.applicationContext).addBeanFactoryPostProcessor(isA(RabbitCloudServiceBeanFactoryPostProcessor
                .class));
        verify(this.applicationContext).addBeanFactoryPostProcessor(isA(RedisCloudServiceBeanFactoryPostProcessor
                .class));
    }

}
