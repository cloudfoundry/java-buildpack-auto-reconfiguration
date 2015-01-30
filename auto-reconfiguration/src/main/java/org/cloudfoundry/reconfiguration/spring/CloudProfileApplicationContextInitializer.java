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
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

/**
 * An {@link ApplicationContextInitializer} implementation that adds the {@code cloud} profile to the collection of of
 * active Spring profiles.  This support is {@link Ordered} at {@code 100} to allow other {@link
 * ApplicationContextInitializer}s to position themselves around it.
 */
public final class CloudProfileApplicationContextInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final int ORDER = 100;

	private final CloudProfileApplicationListener listener;

    /**
     * Creates a new instance
     */
    public CloudProfileApplicationContextInitializer() {
        this(new StandardCloudUtils());
    }

    CloudProfileApplicationContextInitializer(CloudUtils cloudUtils) {
        this.listener = new CloudProfileApplicationListener(cloudUtils);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        listener.addCloudProfile(applicationContext.getEnvironment());
    }

}
