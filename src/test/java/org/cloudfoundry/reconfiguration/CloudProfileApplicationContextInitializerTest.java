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

import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CloudProfileApplicationContextInitializerTest {

    @Test
    public void notInCloud() {
        ConfigurableApplicationContext applicationContext = mock(ConfigurableApplicationContext.class);
        ConfigurableEnvironment environment = new StandardEnvironment();
        when(applicationContext.getEnvironment()).thenReturn(environment);
        CloudHolder cloudHolder = new MockCloudHolder(false);

        new CloudProfileApplicationContextInitializer(cloudHolder).initialize(applicationContext);

        assertThat(environment.getActiveProfiles()).doesNotContain("cloud");
    }

    @Test
    public void profileActivated() {
        ConfigurableApplicationContext applicationContext = mock(ConfigurableApplicationContext.class);
        ConfigurableEnvironment environment = new StandardEnvironment();
        when(applicationContext.getEnvironment()).thenReturn(environment);
        CloudHolder cloudHolder = new MockCloudHolder(true);

        new CloudProfileApplicationContextInitializer(cloudHolder).initialize(applicationContext);

        assertThat(environment.getActiveProfiles()).contains("cloud");
    }

}
