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

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

import static org.cloudfoundry.reconfiguration.ProfileUtils.activateProfile;

public final class CloudProfileApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private final CloudHolder cloudHolder;

    public CloudProfileApplicationContextInitializer() {
        this(SharedCloudHolder.getInstance());
    }

    CloudProfileApplicationContextInitializer(CloudHolder cloudHolder) {
        this.cloudHolder = cloudHolder;
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        activateProfile("cloud", applicationContext.getEnvironment(), this.cloudHolder);
    }

}
