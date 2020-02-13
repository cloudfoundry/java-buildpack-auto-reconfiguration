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

import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.CloudFactory;

final class SharedCloudHolder extends AbstractCloudHolder {

    private static CloudHolder CLOUD_HOLDER = new SharedCloudHolder();

    private SharedCloudHolder() {
        super(create());
    }

    static CloudHolder getInstance() {
        return CLOUD_HOLDER;
    }

    private static Cloud create() {
        try {
            return new CloudFactory().getCloud();
        } catch (CloudException e) {
            return null;
        }
    }

}
