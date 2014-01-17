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

import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;

import javax.sql.DataSource;
import java.util.List;

/**
 * An implementation of {@link RelationalServiceInfoUtils} that uses a {@link Cloud}
 */
public final class CloudRelationalServiceInfoUtils implements RelationalServiceInfoUtils {

    private final Cloud cloud;

    /**
     * Creates a new instance
     *
     * @see #CloudRelationalServiceInfoUtils(Cloud)
     */
    public CloudRelationalServiceInfoUtils() {
        this(new StandardCloudUtils().getCloudFactory().getCloud());
    }

    /**
     * Creates a new instance using an already existing {@link Cloud}
     *
     * @param cloud the {@link Cloud} to use
     */
    public CloudRelationalServiceInfoUtils(Cloud cloud) {
        this.cloud = cloud;
    }

    @Override
    public String relationServiceInfoType() {
        List<ServiceInfo> serviceInfos = this.cloud.getServiceInfos(DataSource.class);

        if (serviceInfos.isEmpty()) {
            return NONE;
        } else if (serviceInfos.size() > 1) {
            return MULTIPLE;
        } else {
            ServiceInfo serviceInfo = serviceInfos.get(0);

            if (serviceInfo instanceof MysqlServiceInfo) {
                return MYSQL;
            } else if (serviceInfo instanceof PostgresqlServiceInfo) {
                return POSTGRESQL;
            } else {
                return UNKNOWN;
            }
        }
    }
}
