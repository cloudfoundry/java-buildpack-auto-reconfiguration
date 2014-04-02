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

package org.cloudfoundry.reconfiguration.play;

import org.cloudfoundry.reconfiguration.util.Sets;
import org.junit.After;
import org.junit.Test;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ConfigurerTest {

    private final ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);

    private final Cloud cloud = mock(Cloud.class);

    private final PropertySetter propertySetter = mock(PropertySetter.class);

    private final Properties configuration;

    {
        this.configuration = new Properties();
        this.configuration.put("db.default.driver", "org.h2.Driver");
        this.configuration.put("db.default.url", "jdbc:h2:mem:play");
        this.configuration.put("db.default.user", "sa");
        this.configuration.put("db.default.password", "admin");
    }

    @After
    public void tearDown() {
        System.clearProperty("db.default.url");
        System.clearProperty("db.default.user");
        System.clearProperty("db.default.password");
        System.clearProperty("db.default.driver");
        System.clearProperty("jpaplugin");
        System.clearProperty("cfjpaplugin");
    }

    @Test
    public void configurePlayDBSinglePostgresService() {
        PostgresqlServiceInfo serviceInfo = new PostgresqlServiceInfo("service-mysql",
                "postgres://service-user:service-password@127.0.0.1:4321/service-database");
        when(this.cloud.getServiceInfos(DataSource.class)).thenReturn(Arrays.<ServiceInfo>asList(serviceInfo));
        when(this.applicationConfiguration.getConfiguration()).thenReturn(this.configuration);
        when(this.applicationConfiguration.getDatabaseNames()).thenReturn(Sets.asSet("default"));

        Configurer.configure(this.applicationConfiguration, this.cloud, this.propertySetter);

        verify(this.propertySetter).setCloudProperties();
        verify(this.propertySetter).setDatabaseProperties(Sets.asSet("default"));
        assertEquals(serviceInfo.getJdbcUrl(), System.getProperty("db.default.url"));
        assertEquals(serviceInfo.getUserName(), System.getProperty("db.default.user"));
        assertEquals(serviceInfo.getPassword(), System.getProperty("db.default.password"));
        assertEquals(PropertySetter.POSTGRES_DRIVER_CLASS, System.getProperty("db.default.driver"));
        assertEquals("disabled", System.getProperty("jpaplugin"));
        assertEquals("enabled", System.getProperty("cfjpaplugin"));
    }

    @Test
    public void configurePlayDBSingleMySQLService() {
        MysqlServiceInfo serviceInfo = new MysqlServiceInfo("service-mysql",
                "mysql://service-user:service-password@127.0.0.1:4321/service-database");
        when(this.cloud.getServiceInfos(DataSource.class)).thenReturn(Arrays.<ServiceInfo>asList(serviceInfo));
        when(this.applicationConfiguration.getConfiguration()).thenReturn(this.configuration);
        when(this.applicationConfiguration.getDatabaseNames()).thenReturn(Sets.asSet("default"));

        Configurer.configure(this.applicationConfiguration, this.cloud, this.propertySetter);

        verify(this.propertySetter).setCloudProperties();
        verify(this.propertySetter).setDatabaseProperties(Sets.asSet("default"));
        assertEquals(serviceInfo.getJdbcUrl(), System.getProperty("db.default.url"));
        assertEquals(serviceInfo.getUserName(), System.getProperty("db.default.user"));
        assertEquals(serviceInfo.getPassword(), System.getProperty("db.default.password"));
        assertEquals(PropertySetter.MYSQL_DRIVER_CLASS, System.getProperty("db.default.driver"));
        assertEquals("disabled", System.getProperty("jpaplugin"));
        assertEquals("enabled", System.getProperty("cfjpaplugin"));

    }

    @Test
    public void configureSinglePlayDBNoServices() {
        when(this.applicationConfiguration.getConfiguration()).thenReturn(this.configuration);
        when(this.applicationConfiguration.getDatabaseNames()).thenReturn(Sets.asSet("default"));

        Configurer.configure(this.applicationConfiguration, this.cloud, this.propertySetter);

        verify(this.propertySetter).setCloudProperties();
        verify(this.propertySetter).setDatabaseProperties(Sets.asSet("default"));
        assertNull(System.getProperty("db.default.url"));
        assertNull(System.getProperty("db.default.user"));
        assertNull(System.getProperty("db.default.password"));
        assertNull(System.getProperty("db.default.driver"));
        assertNull(System.getProperty("jpaplugin"));
        assertNull(System.getProperty("cfjpaplugin"));
    }

    @Test
    public void configureSinglePlayDBMultipleServices() {
        MysqlServiceInfo serviceInfo1 = new MysqlServiceInfo("service-mysql",
                "mysql://service-user:service-password@127.0.0.1:4321/service-database");
        PostgresqlServiceInfo serviceInfo2 = new PostgresqlServiceInfo("service-mysql",
                "postgres://service-user:service-password@127.0.0.1:4321/service-database");
        when(this.cloud.getServiceInfos(DataSource.class)).thenReturn(Arrays.<ServiceInfo>asList(serviceInfo1,
                serviceInfo2));
        when(this.applicationConfiguration.getConfiguration()).thenReturn(this.configuration);
        when(this.applicationConfiguration.getDatabaseNames()).thenReturn(Sets.asSet("default"));

        Configurer.configure(this.applicationConfiguration, this.cloud, this.propertySetter);

        verify(this.propertySetter).setCloudProperties();
        verify(this.propertySetter).setDatabaseProperties(Sets.asSet("default"));
        assertNull(System.getProperty("db.default.url"));
        assertNull(System.getProperty("db.default.user"));
        assertNull(System.getProperty("db.default.password"));
        assertNull(System.getProperty("db.default.driver"));
        assertNull(System.getProperty("jpaplugin"));
        assertNull(System.getProperty("cfjpaplugin"));
    }

    @Test
    public void configureNoPlayDBs() {
        when(this.applicationConfiguration.getConfiguration()).thenReturn(this.configuration);
        when(this.applicationConfiguration.getDatabaseNames()).thenReturn(Collections.<String>emptySet());

        Configurer.configure(this.applicationConfiguration, this.cloud, this.propertySetter);

        verify(this.propertySetter).setCloudProperties();
        verify(this.propertySetter).setDatabaseProperties(Collections.<String>emptySet());
        assertNull(System.getProperty("db.default.url"));
        assertNull(System.getProperty("db.default.user"));
        assertNull(System.getProperty("db.default.password"));
        assertNull(System.getProperty("db.default.driver"));
        assertNull(System.getProperty("jpaplugin"));
        assertNull(System.getProperty("cfjpaplugin"));
    }

    @Test
    public void configureMultiplePlayDBs() {
        PostgresqlServiceInfo serviceInfo = new PostgresqlServiceInfo("service-mysql",
                "postgres://service-user:service-password@127.0.0.1:4321/service-database");
        when(this.cloud.getServiceInfos(DataSource.class)).thenReturn(Arrays.<ServiceInfo>asList(serviceInfo));
        when(this.applicationConfiguration.getConfiguration()).thenReturn(this.configuration);
        when(this.applicationConfiguration.getDatabaseNames()).thenReturn(Sets.asSet("default", "other"));

        Configurer.configure(this.applicationConfiguration, this.cloud, this.propertySetter);

        verify(this.propertySetter).setCloudProperties();
        verify(this.propertySetter).setDatabaseProperties(Sets.asSet("default", "other"));
        assertNull(System.getProperty("db.default.url"));
        assertNull(System.getProperty("db.default.user"));
        assertNull(System.getProperty("db.default.password"));
        assertNull(System.getProperty("db.default.driver"));
        assertNull(System.getProperty("jpaplugin"));
        assertNull(System.getProperty("cfjpaplugin"));
    }

    @Test
    public void configureSinglePlayDBJpaDisabled() {
        this.configuration.put("jpaplugin", "disabled");

        PostgresqlServiceInfo serviceInfo = new PostgresqlServiceInfo("service-mysql",
                "postgres://service-user:service-password@127.0.0.1:4321/service-database");
        when(this.cloud.getServiceInfos(DataSource.class)).thenReturn(Arrays.<ServiceInfo>asList(serviceInfo));
        when(this.applicationConfiguration.getConfiguration()).thenReturn(this.configuration);
        when(this.applicationConfiguration.getDatabaseNames()).thenReturn(Sets.asSet("default"));

        Configurer.configure(this.applicationConfiguration, this.cloud, this.propertySetter);

        verify(this.propertySetter).setCloudProperties();
        verify(this.propertySetter).setDatabaseProperties(Sets.asSet("default"));
        assertEquals(serviceInfo.getJdbcUrl(), System.getProperty("db.default.url"));
        assertEquals(serviceInfo.getUserName(), System.getProperty("db.default.user"));
        assertEquals(serviceInfo.getPassword(), System.getProperty("db.default.password"));
        assertEquals(PropertySetter.POSTGRES_DRIVER_CLASS, System.getProperty("db.default.driver"));
        assertEquals("disabled", System.getProperty("jpaplugin"));
        assertNull(System.getProperty("cfjpaplugin"));
    }

}
