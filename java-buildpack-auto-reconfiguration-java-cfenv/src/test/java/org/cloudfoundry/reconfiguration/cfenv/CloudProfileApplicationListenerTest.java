/*
 * Copyright 2011-2020 the original author or authors.
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

package org.cloudfoundry.reconfiguration.cfenv;

import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

public final class CloudProfileApplicationListenerTest {

    @Test
    public void notInCloud() {
        MockCfUtils.mockNoVcapApplication();
        ConfigurableEnvironment environment = new StandardEnvironment();

        new CloudProfileApplicationListener().onApplicationEvent(new ApplicationEnvironmentPreparedEvent(new SpringApplication(), new String[0], environment));

        assertThat(environment.getActiveProfiles()).doesNotContain("cloud");
    }

    @Test
    public void profileActivated() {
        MockCfUtils.mockVcapApplication();
        ConfigurableEnvironment environment = new StandardEnvironment();

        new CloudProfileApplicationListener().onApplicationEvent(new ApplicationEnvironmentPreparedEvent(new SpringApplication(), new String[0], environment));

        assertThat(environment.getActiveProfiles()).contains("cloud");
    }

}
