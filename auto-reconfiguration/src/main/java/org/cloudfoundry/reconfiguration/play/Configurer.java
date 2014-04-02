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
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

final class Configurer {

    private static final Logger LOGGER = Logger.getLogger(Configurer.class.getName());

    private Configurer() {
    }

    static void configure(ApplicationConfiguration applicationConfiguration,
                          Cloud cloud, PropertySetter propertySetter) {
        propertySetter.setCloudProperties();

        Set<String> databaseNames = applicationConfiguration.getDatabaseNames();
        propertySetter.setDatabaseProperties(databaseNames);

        if (databaseNames.isEmpty()) {
            LOGGER.info("No databases found. Skipping auto-reconfiguration.");
        } else if (databaseNames.size() > 1) {
            LOGGER.warning(String.format("Multiple (%d) databases found. Skipping auto-reconfiguration.",
                    databaseNames.size()));
        } else {
            processDatabase(applicationConfiguration, cloud, databaseNames.iterator().next());
        }
    }

    private static void processDatabase(ApplicationConfiguration applicationConfiguration, Cloud cloud, String name) {
        List<ServiceInfo> serviceInfos = cloud.getServiceInfos(DataSource.class);

        if (serviceInfos.isEmpty()) {
            LOGGER.info("No matching service found. Skipping auto-reconfiguration.");
        } else if (serviceInfos.size() > 1) {
            LOGGER.warning(String.format("More than one (%d) matching services found. Skipping auto-reconfiguration.",
                    serviceInfos.size()));
        } else {
            configureDatabase(name, (RelationalServiceInfo) serviceInfos.get(0));
            configureJpa(applicationConfiguration);
        }

    }

    private static void configureDatabase(String name, RelationalServiceInfo serviceInfo) {
        LOGGER.info(String.format("Auto-reconfiguring %s", name));

        System.setProperty(String.format("db.%s.url", name), serviceInfo.getJdbcUrl());
        System.setProperty(String.format("db.%s.user", name), serviceInfo.getUserName());
        System.setProperty(String.format("db.%s.password", name), serviceInfo.getPassword());

        if (serviceInfo instanceof MysqlServiceInfo) {
            System.setProperty(String.format("db.%s.driver", name), PropertySetter.MYSQL_DRIVER_CLASS);
        } else if (serviceInfo instanceof PostgresqlServiceInfo) {
            System.setProperty(String.format("db.%s.driver", name), PropertySetter.POSTGRES_DRIVER_CLASS);
        }
    }

    private static void configureJpa(ApplicationConfiguration applicationConfiguration) {
        String status = applicationConfiguration.getConfiguration().getProperty("jpaplugin");

        if (!"disabled".equals(status)) {
            LOGGER.info("Auto-reconfiguring JPA Plugin");
            System.setProperty("cfjpaplugin", "enabled");
        }

        System.setProperty("jpaplugin", "disabled");
    }

}
