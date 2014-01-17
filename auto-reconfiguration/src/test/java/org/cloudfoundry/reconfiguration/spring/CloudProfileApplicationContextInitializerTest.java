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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public final class CloudProfileApplicationContextInitializerTest {

    private final ConfigurableApplicationContext applicationContext = mock(ConfigurableApplicationContext.class);

    private final CloudUtils cloudUtils = mock(CloudUtils.class);

    private final ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);

    private final CloudProfileApplicationContextInitializer applicationContextInitializer = new
            CloudProfileApplicationContextInitializer(this.cloudUtils);

    @Test
    public void getOrder() {
        assertEquals(100, this.applicationContextInitializer.getOrder());
    }

    @Test
    public void initializeNoCloud() {
        when(this.cloudUtils.isInCloud()).thenReturn(false);

        this.applicationContextInitializer.initialize(this.applicationContext);

        verifyZeroInteractions(this.environment);
    }

    @Test
    public void initializeAlreadyApplied() {
        when(this.cloudUtils.isInCloud()).thenReturn(true);
        when(this.applicationContext.getEnvironment()).thenReturn(this.environment);
        when(this.environment.getActiveProfiles()).thenReturn(new String[]{"alpha", "cloud"});

        this.applicationContextInitializer.initialize(this.applicationContext);

        verify(this.environment, times(0)).addActiveProfile("cloud");
    }

    @Test
    public void initialize() {
        when(this.cloudUtils.isInCloud()).thenReturn(true);
        when(this.applicationContext.getEnvironment()).thenReturn(this.environment);
        when(this.environment.getActiveProfiles()).thenReturn(new String[0]);

        this.applicationContextInitializer.initialize(this.applicationContext);

        verify(this.environment).addActiveProfile("cloud");
    }

}
