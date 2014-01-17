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

import org.cloudfoundry.reconfiguration.util.RelationalServiceInfoUtils;
import org.cloudfoundry.reconfiguration.util.Sets;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.Configuration;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public final class JPAPluginTest {

    private final Application application = mock(Application.class);

    private final Configuration configuration = mock(Configuration.class);

    private final Configuration jpaConfiguration = mock(Configuration.class);

    private final RelationalServiceInfoUtils relationalServiceInfoUtils = mock(RelationalServiceInfoUtils.class);

    private final JPAPlugin plugin = new JPAPlugin(this.application, this.configuration,
            this.relationalServiceInfoUtils);

    @Before
    public void setupMocks() {
        when(this.application.configuration()).thenReturn(this.configuration);
    }

    @Test
    public void getPropertiesPostgresService() {
        when(this.relationalServiceInfoUtils.relationServiceInfoType()).thenReturn(RelationalServiceInfoUtils
                .POSTGRESQL);

        assertEquals("org.hibernate.dialect.PostgreSQLDialect", this.plugin.getProperties().get("hibernate.dialect"));
    }

    @Test
    public void getPropertiesMySQLService() {
        when(this.relationalServiceInfoUtils.relationServiceInfoType()).thenReturn(RelationalServiceInfoUtils
                .MYSQL);

        assertEquals("org.hibernate.dialect.MySQLDialect", this.plugin.getProperties().get("hibernate.dialect"));
    }

    @Test
    public void getPropertiesMultipleService() {
        when(this.relationalServiceInfoUtils.relationServiceInfoType()).thenReturn(RelationalServiceInfoUtils
                .MULTIPLE);

        assertFalse(this.plugin.getProperties().containsKey("hibernate.dialect"));
    }

    @Test
    public void getPropertiesNoService() {
        when(this.relationalServiceInfoUtils.relationServiceInfoType()).thenReturn(RelationalServiceInfoUtils
                .NONE);

        assertFalse(this.plugin.getProperties().containsKey("hibernate.dialect"));
    }

    @Test
    public void enabled() {
        when(this.configuration.getString("cfjpaplugin")).thenReturn("enabled");

        assertTrue(this.plugin.enabled());
    }

    @Test
    public void disabledWithNullConfig() {
        assertFalse(this.plugin.enabled());
    }

    @Test
    public void disabled() {
        when(this.configuration.getString("cfjpaplugin")).thenReturn("disabled");

        assertFalse(this.plugin.enabled());
    }

    @Test
    public void emEmpty() {
        assertNull(this.plugin.em("alpha"));
    }

    @Test
    public void emNotEmpty() {
        when(this.configuration.getConfig("jpa")).thenReturn(jpaConfiguration);
        when(this.jpaConfiguration.keys()).thenReturn(Sets.<String>asSet("persistenceUnit"));
        this.plugin.onStart();

        assertNotNull(this.plugin.em("persistenceUnit"));
    }

    @Test
    public void onStop() {
        when(this.configuration.getConfig("jpa")).thenReturn(jpaConfiguration);
        when(this.jpaConfiguration.keys()).thenReturn(Sets.<String>asSet("persistenceUnit"));
        this.plugin.onStart();

        this.plugin.onStop();
    }

}
