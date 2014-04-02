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
import org.cloudfoundry.reconfiguration.util.StandardCloudUtils;
import org.springframework.cloud.Cloud;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

import java.util.logging.Logger;

/**
 * An {@link ApplicationContextInitializer} implementation that adds a {@code cloud} {@link PropertySource} to the
 * collection of {@link PropertySource}s in the {@link ConfigurableApplicationContext}.  This support is {@link Ordered}
 * at {@code 200} to allow other {@link ApplicationContextInitializer}s to position themselves around it.
 */
public final class CloudPropertySourceApplicationContextInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final String CLOUD_PROPERTY_SOURCE = "cloud";

    private static final int ORDER = 200;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final CloudUtils cloudUtils;

    /**
     * Creates a new instance
     */
    public CloudPropertySourceApplicationContextInitializer() {
        this(new StandardCloudUtils());
    }

    CloudPropertySourceApplicationContextInitializer(CloudUtils cloudUtils) {
        this.cloudUtils = cloudUtils;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (this.cloudUtils.isInCloud()) {
            addCloudPropertySource(applicationContext);
        } else {
            this.logger.warning("Not running in a cloud. Skipping 'cloud' PropertySource initialization.");
        }
    }

    private void addCloudPropertySource(ConfigurableApplicationContext applicationContext) {
        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();

        if (hasCloudPropertySource(propertySources)) {
            this.logger.fine("'cloud' already in PropertySources");
        } else {
            this.logger.info("Adding 'cloud' PropertySource to ApplicationContext");

            Cloud cloud = this.cloudUtils.getCloudFactory().getCloud();
            PropertySource<?> cloudPropertySource = new PropertiesPropertySource(CLOUD_PROPERTY_SOURCE,
                    cloud.getCloudProperties());

            propertySources.addLast(cloudPropertySource);
        }
    }

    private boolean hasCloudPropertySource(PropertySources propertySources) {
        return propertySources.contains(CLOUD_PROPERTY_SOURCE);
    }

}
