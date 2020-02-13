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

import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfEnvSingleton;

import java.util.function.Consumer;

public class DefaultCfEnvHolder implements CfEnvHolder {

    private final CfEnv cfEnv;

    public DefaultCfEnvHolder() {
        this.cfEnv = CfEnvSingleton.getCfEnvInstance();
    }

    @Override
    public final void withCfEnv(Runnable ifNotInCf, Consumer<CfEnv> ifInCf) {
        if (this.cfEnv.isInCf()) {
            ifInCf.accept(this.cfEnv);
        } else {
            ifNotInCf.run();
        }
    }
}
