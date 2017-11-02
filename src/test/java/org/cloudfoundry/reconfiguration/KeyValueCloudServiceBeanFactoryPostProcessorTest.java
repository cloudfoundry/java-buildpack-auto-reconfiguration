/*
 * Copyright 2011-2017 the original author or authors.
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

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.RedisServiceInfo;
import org.springframework.data.redis.connection.RedisConnectionFactory;

public final class KeyValueCloudServiceBeanFactoryPostProcessorTest extends AbstractCloudServiceBeanFactoryPostProcessorTest {

    @Override
    BeanFactoryPostProcessor getBeanFactoryPostProcessor(Cloud cloud) {
        return new KeyValueCloudServiceBeanFactoryPostProcessor(cloud);
    }

    @Override
    Class<?> getServiceClass() {
        return RedisConnectionFactory.class;
    }

    @Override
    ServiceInfo getServiceInfo(String id) {
        return new RedisServiceInfo(id, "127.0.0.1", 4321, "service-password");
    }

}
