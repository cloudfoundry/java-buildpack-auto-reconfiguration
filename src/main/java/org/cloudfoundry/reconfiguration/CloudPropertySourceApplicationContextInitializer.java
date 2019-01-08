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
import org.springframework.core.env.PropertiesPropertySource;

import java.util.logging.Logger;

public final class CloudPropertySourceApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final CloudHolder cloudHolder;

    public CloudPropertySourceApplicationContextInitializer() {
        this(SharedCloudHolder.getInstance());
    }

    CloudPropertySourceApplicationContextInitializer(CloudHolder cloudHolder) {
        this.cloudHolder = cloudHolder;
    }

    @Override
    public int getOrder() {
        return 200;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        this.cloudHolder.withCloud(
            () -> this.logger.warning(String.format("Skipping '%s' property source addition because not in a cloud", "cloud")),
            cloud -> {
                applicationContext.getEnvironment().getPropertySources().addLast(new PropertiesPropertySource("cloud", cloud.getCloudProperties()));
                this.logger.info("'cloud' property source added");
            });
    }

}
