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

package org.cloudfoundry.reconfiguration.util;

import org.junit.Test;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;
import org.springframework.cloud.service.common.RelationalServiceInfo;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public final class CloudRelationalServiceInfoUtilsTest {

    private final Cloud cloud = mock(Cloud.class);

    private final CloudRelationalServiceInfoUtils relationalServiceInfoUtils = new CloudRelationalServiceInfoUtils
            (this.cloud);

    @Test
    public void none() {
        when(this.cloud.getServiceInfos(DataSource.class)).thenReturn(Collections.<ServiceInfo>emptyList());

        assertEquals(CloudRelationalServiceInfoUtils.NONE, this.relationalServiceInfoUtils.relationServiceInfoType());
    }

    @Test
    public void multiple() {
        PostgresqlServiceInfo serviceInfo1 = new PostgresqlServiceInfo("service-postgres",
                "postgres://service-user:service-password@127.0.0.1:4321/service-database");
        MysqlServiceInfo serviceInfo2 = new MysqlServiceInfo("service-mysql",
                "mysql://service-user:service-password@127.0.0.1:4321/service-database");
        when(this.cloud.getServiceInfos(DataSource.class)).thenReturn(Arrays.<ServiceInfo>asList(serviceInfo1,
                serviceInfo2));

        assertEquals(CloudRelationalServiceInfoUtils.MULTIPLE, this.relationalServiceInfoUtils.relationServiceInfoType());

    }

    @Test
    public void mysql() {
        MysqlServiceInfo serviceInfo = new MysqlServiceInfo("service-mysql",
                "mysql://service-user:service-password@127.0.0.1:4321/service-database");
        when(this.cloud.getServiceInfos(DataSource.class)).thenReturn(Arrays.<ServiceInfo>asList(serviceInfo));

        assertEquals(CloudRelationalServiceInfoUtils.MYSQL, this.relationalServiceInfoUtils.relationServiceInfoType());
    }

    @Test
    public void postgresql() {
        PostgresqlServiceInfo serviceInfo = new PostgresqlServiceInfo("service-postgres",
                "postgres://service-user:service-password@127.0.0.1:4321/service-database");
        when(this.cloud.getServiceInfos(DataSource.class)).thenReturn(Arrays.<ServiceInfo>asList(serviceInfo));

        assertEquals(CloudRelationalServiceInfoUtils.POSTGRESQL, this.relationalServiceInfoUtils.relationServiceInfoType());
    }

    @Test
    public void unknown() {
        GenericRelationalServiceInfo serviceInfo = new GenericRelationalServiceInfo("service-generic",
                "http://localhost");
        when(this.cloud.getServiceInfos(DataSource.class)).thenReturn(Arrays.<ServiceInfo>asList(serviceInfo));

        assertEquals(CloudRelationalServiceInfoUtils.UNKNOWN, this.relationalServiceInfoUtils.relationServiceInfoType());
    }

    private static final class GenericRelationalServiceInfo extends RelationalServiceInfo {

        public GenericRelationalServiceInfo(String id, String uriString) {
            super(id, uriString, DataSource.class.getCanonicalName());
        }
    }

}
