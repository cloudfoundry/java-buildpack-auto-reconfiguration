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
import org.springframework.cloud.service.common.RelationalServiceInfo;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public final class StandardPropertySetterTest {

    private final Cloud cloud = mock(Cloud.class);

    private final StandardPropertySetter propertySetter = new StandardPropertySetter(this.cloud);

    @After
    public void tearDown() {
        for (Iterator<Object> i = System.getProperties().keySet().iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            if (key.startsWith("cloud.services") || key.startsWith("applyEvolutions")) {
                i.remove();
            }
        }
    }

    @Test
    public void setCloudPropertiesMultipleMysql() {
        Properties cloudProperties = new Properties();
        cloudProperties.put("cloud.services.alpha.connection.username", "alpha-user");
        cloudProperties.put("cloud.services.alpha.connection.name", "alpha-database");
        cloudProperties.put("cloud.services.postgresql.connection.name", "alpha-database");
        when(this.cloud.getCloudProperties()).thenReturn(cloudProperties);

        RelationalServiceInfo alpha = new PostgresqlServiceInfo("alpha",
                "postgres://alpha-user:alpha-password@127.0.0.1:4321/alpha-database");
        RelationalServiceInfo bravo = new MysqlServiceInfo("bravo",
                "mysql://bravo-user:bravo-password@127.0.0.1:4321/bravo-database");
        RelationalServiceInfo charlie = new MysqlServiceInfo("charlie",
                "mysql://charlie-user:charlie-password@127.0.0.1:4321/charlie-database");
        when(this.cloud.getServiceInfos(DataSource.class)).thenReturn(Arrays.<ServiceInfo>asList(alpha, bravo,
                charlie));

        this.propertySetter.setCloudProperties();

        assertCloudServiceSystemProperty("alpha.connection.username", "alpha-user");
        assertCloudServiceSystemProperty("alpha.connection.name", "alpha-database");
        assertCloudServiceSystemProperty("postgresql.connection.name", "alpha-database");

        assertCloudServiceSystemProperty("alpha.connection.driver", PropertySetter.POSTGRES_DRIVER_CLASS);
        assertCloudServiceSystemProperty("postgresql.connection.driver", PropertySetter.POSTGRES_DRIVER_CLASS);
        assertCloudServiceSystemProperty("alpha.connection.jdbcUrl", alpha.getJdbcUrl());
        assertCloudServiceSystemProperty("postgresql.connection.jdbcUrl", alpha.getJdbcUrl());

        assertCloudServiceSystemProperty("bravo.connection.driver", PropertySetter.MYSQL_DRIVER_CLASS);
        assertCloudServiceSystemProperty("bravo.connection.jdbcUrl", bravo.getJdbcUrl());
        assertCloudServiceSystemProperty("charlie.connection.driver", PropertySetter.MYSQL_DRIVER_CLASS);
        assertCloudServiceSystemProperty("charlie.connection.jdbcUrl", charlie.getJdbcUrl());
        assertCloudServicesSystemPropertyNull("mysql.connection.driver");
        assertCloudServicesSystemPropertyNull("mysql.connection.jdbcUrl");

    }

    @Test
    public void setCloudPropertiesMultiplePostgres() {
        Properties cloudProperties = new Properties();
        cloudProperties.put("cloud.services.alpha.connection.username", "alpha-user");
        cloudProperties.put("cloud.services.alpha.connection.name", "alpha-database");
        cloudProperties.put("cloud.services.mysql.connection.name", "alpha-database");
        when(this.cloud.getCloudProperties()).thenReturn(cloudProperties);

        RelationalServiceInfo alpha = new MysqlServiceInfo("alpha",
                "mysql://alpha-user:alpha-password@127.0.0.1:4321/alpha-database");
        RelationalServiceInfo bravo = new PostgresqlServiceInfo("bravo",
                "postgres://bravo-user:bravo-password@127.0.0.1:4321/bravo-database");
        RelationalServiceInfo charlie = new PostgresqlServiceInfo("charlie",
                "postgres://charlie-user:charlie-password@127.0.0.1:4321/charlie-database");
        when(this.cloud.getServiceInfos(DataSource.class)).thenReturn(Arrays.<ServiceInfo>asList(alpha, bravo,
                charlie));

        this.propertySetter.setCloudProperties();

        assertCloudServiceSystemProperty("alpha.connection.username", "alpha-user");
        assertCloudServiceSystemProperty("alpha.connection.name", "alpha-database");
        assertCloudServiceSystemProperty("mysql.connection.name", "alpha-database");

        assertCloudServiceSystemProperty("alpha.connection.driver", PropertySetter.MYSQL_DRIVER_CLASS);
        assertCloudServiceSystemProperty("mysql.connection.driver", PropertySetter.MYSQL_DRIVER_CLASS);
        assertCloudServiceSystemProperty("alpha.connection.jdbcUrl", alpha.getJdbcUrl());
        assertCloudServiceSystemProperty("mysql.connection.jdbcUrl", alpha.getJdbcUrl());

        assertCloudServiceSystemProperty("bravo.connection.driver", PropertySetter.POSTGRES_DRIVER_CLASS);
        assertCloudServiceSystemProperty("bravo.connection.jdbcUrl", bravo.getJdbcUrl());
        assertCloudServiceSystemProperty("charlie.connection.driver", PropertySetter.POSTGRES_DRIVER_CLASS);
        assertCloudServiceSystemProperty("charlie.connection.jdbcUrl", charlie.getJdbcUrl());
        assertCloudServicesSystemPropertyNull("postgresql.connection.driver");
        assertCloudServicesSystemPropertyNull("postgresql.connection.jdbcUrl");
    }

    @Test
    public void setDatabaseProperties() {
        this.propertySetter.setDatabaseProperties(Sets.asSet("default", "other"));

        assertSystemProperty("applyEvolutions.default", true);
        assertSystemProperty("applyEvolutions.other", true);
    }

    private void assertCloudServiceSystemProperty(String key, Object value) {
        assertSystemProperty(String.format("cloud.services.%s", key), value);
    }

    private void assertCloudServicesSystemPropertyNull(String key) {
        assertNull(System.getProperty(String.format("cloud.services.%s", key)));
    }

    private void assertSystemProperty(String key, Object value) {
        assertEquals(value.toString(), System.getProperty(key));
    }
}
