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
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.cloud.Cloud;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;

import java.util.logging.Logger;

/**
 * An {@link ApplicationContextInitializer} implementation that adds cloud service {@link BeanFactoryPostProcessor}s.
 * This support is {@link Ordered} at {@code 300} to allow other {@link ApplicationContextInitializer}s to position
 * themselves around it.
 */
public final class CloudAutoReconfigurationApplicationContextInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final int ORDER = 300;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final CloudUtils cloudUtils;

    /**
     * Creates a new instance
     */
    public CloudAutoReconfigurationApplicationContextInitializer() {
        this(new StandardCloudUtils());
    }

    CloudAutoReconfigurationApplicationContextInitializer(CloudUtils cloudUtils) {
        this.cloudUtils = cloudUtils;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (this.cloudUtils.isInCloud()) {
            addAutoReconfiguration(applicationContext);
        } else {
            this.logger.warning("Not running in a cloud. Skipping cloud service initialization.");
        }
    }

    private void addAutoReconfiguration(ConfigurableApplicationContext applicationContext) {
        this.logger.info("Adding cloud service auto-reconfiguration to ApplicationContext");

        Cloud cloud = this.cloudUtils.getCloudFactory().getCloud();

        addBeanFactoryPostProcessor(applicationContext, new DataSourceCloudServiceBeanFactoryPostProcessor(cloud));
        addBeanFactoryPostProcessor(applicationContext, new MongoCloudServiceBeanFactoryPostProcessor(cloud));
        addBeanFactoryPostProcessor(applicationContext, new RabbitCloudServiceBeanFactoryPostProcessor(cloud));
        addBeanFactoryPostProcessor(applicationContext, new RedisCloudServiceBeanFactoryPostProcessor(cloud));
    }

    private void addBeanFactoryPostProcessor(ConfigurableApplicationContext applicationContext,
                                             BeanFactoryPostProcessor beanFactoryPostProcessor) {
        if (hasBeanFactoryPostProcessor(applicationContext, beanFactoryPostProcessor)) {
            this.logger.fine(String.format("'%s' already in list of BeanFactoryPostProcessors",
                    beanFactoryPostProcessor.getClass()));
        } else {
            this.logger.fine(String.format("Adding '%s' to the list of BeanFactoryPostProcessors",
                    beanFactoryPostProcessor.getClass()));
            applicationContext.addBeanFactoryPostProcessor(beanFactoryPostProcessor);
        }
    }

    private boolean hasBeanFactoryPostProcessor(ConfigurableApplicationContext applicationContext,
                                                BeanFactoryPostProcessor beanFactoryPostProcessor) {
        for (BeanFactoryPostProcessor candidate : ((AbstractApplicationContext) applicationContext)
                .getBeanFactoryPostProcessors()) {
            if (candidate.getClass().equals(beanFactoryPostProcessor.getClass())) {
                return true;
            }
        }

        return false;
    }
}
