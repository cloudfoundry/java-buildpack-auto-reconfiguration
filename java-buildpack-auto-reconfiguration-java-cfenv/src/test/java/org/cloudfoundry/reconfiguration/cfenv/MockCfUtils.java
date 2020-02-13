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

import mockit.Mock;
import mockit.MockUp;

import java.util.Map;

public class MockCfUtils {

    public static void mockVcapApplication() {
        Map<String, String> env = System.getenv();
        new MockUp<System>() {
            @Mock
            public String getenv(String name) {
                if (name.equalsIgnoreCase("VCAP_APPLICATION")) {
                    return "{\"instance_id\":\"123\"}";
                }
                return env.get(name);
            }
        };
    }

    public static void mockNoVcapApplication() {
        Map<String, String> env = System.getenv();
        new MockUp<System>() {
            @Mock
            public String getenv(String name) {
                if (name.equalsIgnoreCase("VCAP_APPLICATION")) {
                    return null;
                }
                if (name.equalsIgnoreCase("VCAP_SERVICES")) {
                    return null;
                }
                return env.get(name);
            }
        };
    }

}
