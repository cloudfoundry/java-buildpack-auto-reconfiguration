/*
 * Copyright 2011-2020 the original author or authors.
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

package org.cloudfoundry.reconfiguration.cfenv;

import org.springframework.core.env.ConfigurableEnvironment;

import java.util.logging.Logger;

final class ProfileUtils {

    private static final Logger LOGGER = Logger.getLogger(ProfileUtils.class.getName());

    private ProfileUtils() {
    }

    static void activateProfile(String profile, ConfigurableEnvironment environment, CfEnvHolder cfEnvHolder) {
        cfEnvHolder.withCfEnv(
            () -> LOGGER.warning(String.format("Skipping '%s' profile activation because not in cloud foundry", profile)),
            cfEnv -> {
                environment.addActiveProfile(profile);
                LOGGER.info(String.format("'%s' profile activated", profile));
            });
    }

}
