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

import com.datastax.driver.core.Cluster;
import org.junit.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.AmqpServiceInfo;
import org.springframework.cloud.service.common.CassandraServiceInfo;
import org.springframework.cloud.service.common.MongoServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;
import org.springframework.cloud.service.common.RedisServiceInfo;
import org.springframework.cloud.service.common.SmtpServiceInfo;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.mail.javamail.JavaMailSender;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public final class CloudServiceReconfigurationBeanFactoryPostProcessorTest {

    @Test
    public void hasMultipleBeansColumn() {
        hasMultipleBeans(Cluster.class);
    }

    @Test
    public void hasMultipleBeansDocument() {
        hasMultipleBeans(MongoDbFactory.class);
    }

    @Test
    public void hasMultipleBeansKeyValue() {
        hasMultipleBeans(RedisConnectionFactory.class);
    }

    @Test
    public void hasMultipleBeansMessaging() {
        hasMultipleBeans(ConnectionFactory.class);
    }

    @Test
    public void hasMultipleBeansRelational() {
        hasMultipleBeans(DataSource.class);
    }

    @Test
    public void hasMultipleBeansSmtp() {
        hasMultipleBeans(JavaMailSender.class);
    }

    @Test
    public void hasMultipleCloudServicesColumn() {
        hasMultipleCloudServices(Cluster.class, CloudServiceReconfigurationBeanFactoryPostProcessorTest::getColumnServiceInfo);
    }

    @Test
    public void hasMultipleCloudServicesDocument() {
        hasMultipleCloudServices(MongoDbFactory.class, CloudServiceReconfigurationBeanFactoryPostProcessorTest::getDocumentServiceInfo);
    }

    @Test
    public void hasMultipleCloudServicesKeyValue() {
        hasMultipleCloudServices(RedisConnectionFactory.class, CloudServiceReconfigurationBeanFactoryPostProcessorTest::getKeyValueServiceInfo);
    }

    @Test
    public void hasMultipleCloudServicesMessaging() {
        hasMultipleCloudServices(ConnectionFactory.class, CloudServiceReconfigurationBeanFactoryPostProcessorTest::getMessagingServiceInfo);
    }

    @Test
    public void hasMultipleCloudServicesRelational() {
        hasMultipleCloudServices(DataSource.class, CloudServiceReconfigurationBeanFactoryPostProcessorTest::getRelationalServiceInfo);
    }

    @Test
    public void hasMultipleCloudServicesSmtp() {
        hasMultipleCloudServices(JavaMailSender.class, CloudServiceReconfigurationBeanFactoryPostProcessorTest::getSmtpServiceInfo);
    }

    @Test
    public void hasZeroBeansColumn() {
        hasZeroBeans(Cluster.class);
    }

    @Test
    public void hasZeroBeansDocument() {
        hasZeroBeans(MongoDbFactory.class);
    }

    @Test
    public void hasZeroBeansKeyValue() {
        hasZeroBeans(RedisConnectionFactory.class);
    }

    @Test
    public void hasZeroBeansMessaging() {
        hasZeroBeans(ConnectionFactory.class);
    }

    @Test
    public void hasZeroBeansRelational() {
        hasZeroBeans(DataSource.class);
    }

    @Test
    public void hasZeroBeansSmtp() {
        hasZeroBeans(JavaMailSender.class);
    }

    @Test
    public void hasZeroCloudServicesColumn() {
        hasZeroCloudServices(Cluster.class);
    }

    @Test
    public void hasZeroCloudServicesDocument() {
        hasZeroCloudServices(MongoDbFactory.class);
    }

    @Test
    public void hasZeroCloudServicesKeyValue() {
        hasZeroCloudServices(RedisConnectionFactory.class);
    }

    @Test
    public void hasZeroCloudServicesMessaging() {
        hasZeroCloudServices(ConnectionFactory.class);
    }

    @Test
    public void hasZeroCloudServicesRelational() {
        hasZeroCloudServices(DataSource.class);
    }

    @Test
    public void hasZeroCloudServicesSmtp() {
        hasZeroCloudServices(JavaMailSender.class);
    }

    @Test
    public void reconfiguredColumn() {
        reconfigured(Cluster.class, CloudServiceReconfigurationBeanFactoryPostProcessorTest::getColumnServiceInfo);
    }

    @Test
    public void reconfiguredDocument() {
        reconfigured(MongoDbFactory.class, CloudServiceReconfigurationBeanFactoryPostProcessorTest::getDocumentServiceInfo);
    }

    @Test
    public void reconfiguredKeyValue() {
        reconfigured(RedisConnectionFactory.class, CloudServiceReconfigurationBeanFactoryPostProcessorTest::getKeyValueServiceInfo);
    }

    @Test
    public void reconfiguredMessaging() {
        reconfigured(ConnectionFactory.class, CloudServiceReconfigurationBeanFactoryPostProcessorTest::getMessagingServiceInfo);
    }

    @Test
    public void reconfiguredRelational() {
        reconfigured(DataSource.class, CloudServiceReconfigurationBeanFactoryPostProcessorTest::getRelationalServiceInfo);
    }

    @Test
    public void reconfiguredSmtp() {
        reconfigured(JavaMailSender.class, CloudServiceReconfigurationBeanFactoryPostProcessorTest::getSmtpServiceInfo);
    }

    @Test
    public void usingCloudServices() {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        Cloud cloud = MockCloudHolder.create(getRelationalServiceInfo("test-cloud-service-1"));
        applicationContext.registerBeanDefinition("test-cloud", BeanDefinitionBuilder.genericBeanDefinition(Cloud.class).getBeanDefinition());
        applicationContext.registerBeanDefinition("test-bean-1", BeanDefinitionBuilder.genericBeanDefinition(DataSource.class).getBeanDefinition());

        new CloudServiceReconfigurationBeanFactoryPostProcessor(applicationContext, cloud).postProcessBeanFactory(applicationContext.getBeanFactory());

        assertThat(applicationContext.getBeanNamesForType(DataSource.class)).containsOnly("test-bean-1");
    }

    private static CassandraServiceInfo getColumnServiceInfo(String id) {
        return new CassandraServiceInfo(id, Collections.singletonList("10.0.0.1"), 9042, "walter", "white");
    }

    private static MongoServiceInfo getDocumentServiceInfo(String id) {
        return new MongoServiceInfo(id, "127.0.0.1", 27017, "service-username", "service-password", "service-database");
    }

    private static RedisServiceInfo getKeyValueServiceInfo(String id) {
        return new RedisServiceInfo(id, "127.0.0.1", 4321, "service-password");
    }

    private static AmqpServiceInfo getMessagingServiceInfo(String id) {
        return new AmqpServiceInfo(id, "127.0.0.1", 4321, "service-username", "service-password", "service-virtual-host");
    }

    private static PostgresqlServiceInfo getRelationalServiceInfo(String id) {
        return new PostgresqlServiceInfo(id, "postgres://service-user:service-password@127.0.0.1:4321/service-database");
    }

    private static SmtpServiceInfo getSmtpServiceInfo(String id) {
        return new SmtpServiceInfo(id, "test-host", -1, "test-username", "test-password");
    }

    private static void hasMultipleBeans(Class<?> serviceClass) {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        Cloud cloud = MockCloudHolder.create();
        applicationContext.registerBeanDefinition("test-bean-1", BeanDefinitionBuilder.genericBeanDefinition(serviceClass).getBeanDefinition());
        applicationContext.registerBeanDefinition("test-bean-2", BeanDefinitionBuilder.genericBeanDefinition(serviceClass).getBeanDefinition());

        new CloudServiceReconfigurationBeanFactoryPostProcessor(applicationContext, cloud).postProcessBeanFactory(applicationContext.getBeanFactory());

        assertThat(applicationContext.getBeanNamesForType(serviceClass)).containsOnly("test-bean-1", "test-bean-2");
    }

    private static void hasMultipleCloudServices(Class<?> serviceClass, Function<String, ServiceInfo> serviceInfo) {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        Cloud cloud = MockCloudHolder.create(serviceInfo.apply("test-cloud-service-1"), serviceInfo.apply("test-cloud-service-2"));
        applicationContext.registerBeanDefinition("test-bean-1", BeanDefinitionBuilder.genericBeanDefinition(serviceClass).getBeanDefinition());

        new CloudServiceReconfigurationBeanFactoryPostProcessor(applicationContext, cloud).postProcessBeanFactory(applicationContext.getBeanFactory());

        assertThat(applicationContext.getBeanNamesForType(serviceClass)).containsOnly("test-bean-1");
    }

    private static void hasZeroBeans(Class<?> serviceClass) {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        Cloud cloud = MockCloudHolder.create();

        new CloudServiceReconfigurationBeanFactoryPostProcessor(applicationContext, cloud).postProcessBeanFactory(applicationContext.getBeanFactory());

        assertThat(applicationContext.getBeanNamesForType(serviceClass)).isEmpty();
    }

    private static void hasZeroCloudServices(Class<?> serviceClass) {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        Cloud cloud = MockCloudHolder.create();
        applicationContext.registerBeanDefinition("test-bean-1", BeanDefinitionBuilder.genericBeanDefinition(serviceClass).getBeanDefinition());

        new CloudServiceReconfigurationBeanFactoryPostProcessor(applicationContext, cloud).postProcessBeanFactory(applicationContext.getBeanFactory());

        assertThat(applicationContext.getBeanNamesForType(serviceClass)).containsOnly("test-bean-1");
    }

    private static void reconfigured(Class<?> serviceClass, Function<String, ServiceInfo> serviceInfo) {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        Cloud cloud = MockCloudHolder.create(serviceInfo.apply("test-cloud-service-1"));
        applicationContext.registerBeanDefinition("test-bean-1", BeanDefinitionBuilder.genericBeanDefinition(serviceClass).getBeanDefinition());

        new CloudServiceReconfigurationBeanFactoryPostProcessor(applicationContext, cloud).postProcessBeanFactory(applicationContext.getBeanFactory());

        assertThat(applicationContext.getBeanNamesForType(serviceClass)).containsOnly("test-cloud-service-1");
    }


}
