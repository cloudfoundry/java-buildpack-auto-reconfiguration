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

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.MapPropertySource;

import java.util.logging.Logger;

public final class CloudPropertySourceApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final CfEnvHolder cfEnvHolder;

    CloudPropertySourceApplicationContextInitializer() {
        this.cfEnvHolder = new DefaultCfEnvHolder();
    }

    @Override
    public int getOrder() {
        return 200;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        this.cfEnvHolder.withCfEnv(
            () -> this.logger.warning(String.format("Skipping '%s' property source addition because not in a cloud", "cloud")),
            cfEnv -> {
                applicationContext.getEnvironment().getPropertySources().addLast(new MapPropertySource("cloud", cfEnv.getApp().getMap()));
                this.logger.info("'cloud' property source added");
            });
    }

}
