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

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.common.RabbitServiceInfo;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;

public final class RabbitCloudServiceBeanFactoryPostProcessorTest extends
        AbstractCloudServiceBeanFactoryPostProcessorTest<CachingConnectionFactory> {

    private static final RabbitServiceInfo SERVICE_INFO = new RabbitServiceInfo("service-rabbit", "127.0.0.1", 4321,
            "service-username", "service-password", "service-virtual-host");

    public RabbitCloudServiceBeanFactoryPostProcessorTest() {
        super(CachingConnectionFactory.class, "rabbit", SERVICE_INFO);
    }

    @Override
    protected BeanFactoryPostProcessor getInstance(Cloud cloud) {
        return new RabbitCloudServiceBeanFactoryPostProcessor(cloud);
    }

    @Override
    protected void assertLocalConfiguration(CachingConnectionFactory factory) {
        assertConfiguration(factory, "local-username", "local-password", "localhost", 1234,
                "local-virtual-host");
    }

    @Override
    protected void assertServiceConfiguration(CachingConnectionFactory factory) {
        assertConfiguration(factory, "service-username", "service-password", "127.0.0.1", 4321,
                "service-virtual-host");
    }

    private void assertConfiguration(CachingConnectionFactory factory, String username, String password, String host,
                                     int port, String virtualHost) {
        ConnectionFactory connectionFactory = (ConnectionFactory) ReflectionTestUtils.getField(factory,
                "rabbitConnectionFactory");
        assertEquals(username, connectionFactory.getUsername());
        assertEquals(password, connectionFactory.getPassword());

        assertEquals(host, factory.getHost());
        assertEquals(port, factory.getPort());
        assertEquals(virtualHost, factory.getVirtualHost());
    }

}
