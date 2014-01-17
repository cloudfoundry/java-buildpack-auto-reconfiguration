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

import org.cloudfoundry.reconfiguration.util.CloudUtils;
import org.cloudfoundry.reconfiguration.util.StandardCloudUtils;
import org.springframework.cloud.Cloud;
import play.core.server.NettyServer;

/**
 * Wrapper that takes care of environment initialization and auto-reconfiguration before starting the main Play
 * Framework class.
 */
public final class Bootstrap {

    private static final CloudUtils CLOUD_UTILS = new StandardCloudUtils();

    private Bootstrap() {
    }

    /**
     * Entry to execution
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (CLOUD_UTILS.isInCloud()) {
            Cloud cloud = CLOUD_UTILS.getCloudFactory().getCloud();
            Configurer.configure(new StandardApplicationConfiguration(), cloud, new StandardPropertySetter(cloud));
        }

        NettyServer.main(args);
    }

}
