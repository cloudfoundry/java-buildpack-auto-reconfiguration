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
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public final class CloudPropertySourceApplicationContextInitializerTest {

    private final ConfigurableApplicationContext applicationContext = mock(ConfigurableApplicationContext.class);

    private final Cloud cloud = mock(Cloud.class);

    private final CloudFactory cloudFactory = mock(CloudFactory.class);

    private final CloudUtils cloudUtils = mock(CloudUtils.class);

    private final ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);

    private final MutablePropertySources propertySources = mock(MutablePropertySources.class);

    private final CloudPropertySourceApplicationContextInitializer applicationContextInitializer = new
            CloudPropertySourceApplicationContextInitializer(this.cloudUtils);

    @Test
    public void getOrder() {
        assertEquals(200, this.applicationContextInitializer.getOrder());
    }

    @Test
    public void initializeNoCloud() {
        when(this.cloudUtils.isInCloud()).thenReturn(false);

        this.applicationContextInitializer.initialize(this.applicationContext);

        verifyZeroInteractions(this.propertySources);
    }

    @Test
    public void initializeAlreadyApplied() {
        when(this.cloudUtils.isInCloud()).thenReturn(true);
        when(this.applicationContext.getEnvironment()).thenReturn(this.environment);
        when(this.environment.getPropertySources()).thenReturn(this.propertySources);
        when(this.propertySources.contains("cloud")).thenReturn(true);

        this.applicationContextInitializer.initialize(this.applicationContext);

        verify(this.propertySources, times(0)).addLast(isA(PropertySource.class));
    }

    @Test
    public void initialize() {
        when(this.cloudUtils.isInCloud()).thenReturn(true);
        when(this.applicationContext.getEnvironment()).thenReturn(this.environment);
        when(this.environment.getPropertySources()).thenReturn(this.propertySources);
        when(this.propertySources.contains("cloud")).thenReturn(false);
        when(this.cloudUtils.getCloudFactory()).thenReturn(this.cloudFactory);
        when(this.cloudFactory.getCloud()).thenReturn(this.cloud);
        when(this.cloud.getCloudProperties()).thenReturn(new Properties());

        this.applicationContextInitializer.initialize(this.applicationContext);

        verify(this.propertySources).addLast(isA(PropertySource.class));
    }

}
