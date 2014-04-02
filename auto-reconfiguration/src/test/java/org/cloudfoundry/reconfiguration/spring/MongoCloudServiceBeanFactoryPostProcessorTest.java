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

import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.common.MongoServiceInfo;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;

public final class MongoCloudServiceBeanFactoryPostProcessorTest extends
        AbstractCloudServiceBeanFactoryPostProcessorTest<SimpleMongoDbFactory> {

    private static final MongoServiceInfo SERVICE_INFO = new MongoServiceInfo("service-mongo", "127.0.0.1", 4321,
            "service-username", "service-password", "service-database");

    public MongoCloudServiceBeanFactoryPostProcessorTest() {
        super(SimpleMongoDbFactory.class, "mongo", SERVICE_INFO);
    }

    @Override
    protected BeanFactoryPostProcessor getInstance(Cloud cloud) {
        return new MongoCloudServiceBeanFactoryPostProcessor(cloud);
    }

    @Override
    protected void assertLocalConfiguration(SimpleMongoDbFactory factory) {
        assertConfiguration(factory, "local-database", "local-username", "local-password", "localhost", 1234);
    }

    @Override
    protected void assertServiceConfiguration(SimpleMongoDbFactory factory) {
        assertConfiguration(factory, "service-database", "service-username", "service-password", "127.0.0.1", 4321);
    }

    private void assertConfiguration(SimpleMongoDbFactory factory, String database, String username, String password,
                                     String host, int port) {
        assertEquals(database, ReflectionTestUtils.getField(factory, "databaseName"));

        UserCredentials userCredentials = (UserCredentials) ReflectionTestUtils.getField(factory, "credentials");
        assertEquals(username, userCredentials.getUsername());
        assertEquals(password, userCredentials.getPassword());

        Mongo mongo = (Mongo) ReflectionTestUtils.getField(factory, "mongo");
        ServerAddress serverAddress = mongo.getAddress();
        assertEquals(host, serverAddress.getHost());
        assertEquals(port, serverAddress.getPort());
    }
}
