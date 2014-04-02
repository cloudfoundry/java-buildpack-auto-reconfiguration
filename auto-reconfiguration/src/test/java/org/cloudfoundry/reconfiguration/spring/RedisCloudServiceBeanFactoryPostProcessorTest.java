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

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.common.RedisServiceInfo;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import static org.junit.Assert.assertEquals;

public final class RedisCloudServiceBeanFactoryPostProcessorTest extends
        AbstractCloudServiceBeanFactoryPostProcessorTest<JedisConnectionFactory> {

    private static final RedisServiceInfo SERVICE_INFO = new RedisServiceInfo("service-redis", "127.0.0.1", 4321,
            "service-password");

    public RedisCloudServiceBeanFactoryPostProcessorTest() {
        super(JedisConnectionFactory.class, "redis", SERVICE_INFO);
    }

    @Override
    protected BeanFactoryPostProcessor getInstance(Cloud cloud) {
        return new RedisCloudServiceBeanFactoryPostProcessor(cloud);
    }

    @Override
    protected void assertLocalConfiguration(JedisConnectionFactory factory) {
        assertConfiguration(factory, "local-password", "localhost", 1234);
    }

    @Override
    protected void assertServiceConfiguration(JedisConnectionFactory factory) {
        assertConfiguration(factory, "service-password", "127.0.0.1", 4321);
    }

    private void assertConfiguration(JedisConnectionFactory factory, String password, String host, int port) {
        assertEquals(password, factory.getPassword());
        assertEquals(host, factory.getHostName());
        assertEquals(port, factory.getPort());
    }

}
