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
import org.springframework.context.support.StaticApplicationContext;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public final class CloudServiceReconfigurationApplicationContextInitializerTest {

    @Test
    public void notInCloud() {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        CloudHolder cloudHolder = new MockCloudHolder(false);

        new CloudServiceReconfigurationApplicationContextInitializer(cloudHolder).initialize(applicationContext);

        assertThat(applicationContext.getBeanFactoryPostProcessors()).isEmpty();
    }

    @Test
    public void reconfigurationEnabled() {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        CloudHolder cloudHolder = new MockCloudHolder(true);

        new CloudServiceReconfigurationApplicationContextInitializer(cloudHolder).initialize(applicationContext);

        assertThat(applicationContext.getBeanFactoryPostProcessors())
            .flatExtracting(input -> Collections.<Class<?>>singleton(input.getClass()))
            .contains(
                CloudServiceReconfigurationBeanFactoryPostProcessor.class);
    }

}
