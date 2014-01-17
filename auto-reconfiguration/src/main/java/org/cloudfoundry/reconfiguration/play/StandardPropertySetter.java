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

import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;
import org.springframework.cloud.service.common.RelationalServiceInfo;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

final class StandardPropertySetter implements PropertySetter {

    private final Cloud cloud;

    StandardPropertySetter(Cloud cloud) {
        this.cloud = cloud;
    }

    @Override
    public void setCloudProperties() {
        Properties cloudProperties = this.cloud.getCloudProperties();

        setProperties(cloudProperties);
        propagateRelationServiceInfos(cloudProperties);
    }

    @Override
    public void setDatabaseProperties(Set<String> names) {
        for (String name : names) {
            System.setProperty(String.format("applyEvolutions.%s", name), "true");
        }
    }

    private void propagateRelationServiceInfos(Properties cloudProperties) {
        for (ServiceInfo serviceInfo : this.cloud.getServiceInfos(DataSource.class)) {
            RelationalServiceInfo relationalServiceInfo = (RelationalServiceInfo) serviceInfo;

            System.setProperty(String.format("cloud.services.%s.connection.jdbcUrl", relationalServiceInfo.getId()),
                    relationalServiceInfo.getJdbcUrl());

            if (serviceInfo instanceof MysqlServiceInfo) {
                setDatabaseSpecificProperties(MYSQL_DRIVER_CLASS, cloudProperties, relationalServiceInfo, "mysql");
            } else if (serviceInfo instanceof PostgresqlServiceInfo) {
                setDatabaseSpecificProperties(POSTGRES_DRIVER_CLASS, cloudProperties, relationalServiceInfo,
                        "postgresql");
            }
        }
    }

    private void setDatabaseSpecificProperties(String driverClass, Properties cloudProperties, RelationalServiceInfo
            serviceInfo, String type) {
        System.setProperty(String.format("cloud.services.%s.connection.driver", serviceInfo.getId()), driverClass);

        if (cloudProperties.containsKey(String.format("cloud.services.%s.connection.name", type))) {
            System.setProperty(String.format("cloud.services.%s.connection.driver", type), driverClass);
            System.setProperty(String.format("cloud.services.%s.connection.jdbcUrl", type), serviceInfo.getJdbcUrl());
        }
    }

    private void setProperties(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            System.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
    }

}
