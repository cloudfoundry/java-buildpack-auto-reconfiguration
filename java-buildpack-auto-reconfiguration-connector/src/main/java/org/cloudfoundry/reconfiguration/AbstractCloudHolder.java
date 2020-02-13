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

import java.util.function.Consumer;

abstract class AbstractCloudHolder implements CloudHolder {

    private final Cloud cloud;

    private final boolean isInCloud;

    AbstractCloudHolder(Cloud cloud) {
        this(cloud, cloud != null);
    }

    AbstractCloudHolder(Cloud cloud, boolean isInCloud) {
        this.cloud = cloud;
        this.isInCloud = isInCloud;
    }

    @Override
    public final void withCloud(Runnable ifNotInCloud, Consumer<Cloud> ifInCloud) {
        if (this.isInCloud) {
            ifInCloud.accept(this.cloud);
        } else {
            ifNotInCloud.run();
        }
    }

}
