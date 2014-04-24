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

import org.springframework.cloud.CloudConnector;
import org.springframework.cloud.app.ApplicationInstanceInfo;
import org.springframework.cloud.service.ServiceInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CloudTestUtil {

    public static StubCloudConnector getTestCloudConnector(ServiceInfo... serviceInfos) {
        StubCloudConnector cloudConnector = new StubCloudConnector();

        for (ServiceInfo serviceInfo : serviceInfos) {
            cloudConnector.addServiceInfo(serviceInfo);
        }

        return cloudConnector;
    }

    private static final class StubApplicationInstanceInfo implements ApplicationInstanceInfo {

        private final String instanceId;

        private final String appId;

        private final Map<String, Object> properties;

        public StubApplicationInstanceInfo(String instanceId, String appId, Map<String, Object> properties) {
            this.instanceId = instanceId;
            this.appId = appId;
            this.properties = properties;
        }

        @Override
        public String getInstanceId() {
            return this.instanceId;
        }

        @Override
        public String getAppId() {
            return this.appId;
        }

        @Override
        public Map<String, Object> getProperties() {
            return this.properties;
        }
    }

    private static final class StubCloudConnector implements CloudConnector {

        private final List<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();

        private final ApplicationInstanceInfo applicationInstance = new StubApplicationInstanceInfo
                ("default-app-instance", "default-app", Collections.<String, Object>emptyMap());

        @Override
        public boolean isInMatchingCloud() {
            return true;
        }

        @Override
        public ApplicationInstanceInfo getApplicationInstanceInfo() {
            return this.applicationInstance;
        }

        @Override
        public List<ServiceInfo> getServiceInfos() {
            return this.serviceInfos;
        }

        private void addServiceInfo(ServiceInfo info) {
            this.serviceInfos.add(info);
        }

    }

}
