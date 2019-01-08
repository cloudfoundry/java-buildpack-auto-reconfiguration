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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.Cloud;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import java.util.logging.Logger;

import static org.cloudfoundry.reconfiguration.CloudServiceUtils.replaceBeanOfType;

final class CloudServiceReconfigurationBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final ApplicationContext applicationContext;

    private final Cloud cloud;

    CloudServiceReconfigurationBeanFactoryPostProcessor(ApplicationContext applicationContext, Cloud cloud) {
        this.applicationContext = applicationContext;
        this.cloud = cloud;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        new ApplicationContextCloudServicesHolder(this.applicationContext)
            .withCloudServices(
                () -> this.logger.info("Skipping reconfiguration because cloud services already configured"),
                () -> {
                    replaceBeanOfType("com.datastax.driver.core.Cluster", (DefaultListableBeanFactory) beanFactory, this.cloud);
                    replaceBeanOfType("org.springframework.data.mongodb.MongoDbFactory", (DefaultListableBeanFactory) beanFactory, this.cloud);
                    replaceBeanOfType("org.springframework.data.redis.connection.RedisConnectionFactory", (DefaultListableBeanFactory) beanFactory, this.cloud);
                    replaceBeanOfType("org.springframework.amqp.rabbit.connection.ConnectionFactory", (DefaultListableBeanFactory) beanFactory, this.cloud);
                    replaceBeanOfType("javax.sql.DataSource", (DefaultListableBeanFactory) beanFactory, this.cloud);
                    replaceBeanOfType("org.springframework.mail.javamail.JavaMailSender", (DefaultListableBeanFactory) beanFactory, this.cloud);
                }
            );
    }

}
