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

package org.cloudfoundry.reconfiguration.testsupport;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
final class FakeFactoryBean implements FactoryBean<String> {

    private FakeFactoryBeanDependency fakeFactoryBeanDependency;

    @Autowired
    public FakeFactoryBean(FakeFactoryBeanDependency fakeFactoryBeanDependency) {
        this.fakeFactoryBeanDependency = fakeFactoryBeanDependency;
    }

    @Override
    public String getObject() throws Exception {
        return this.fakeFactoryBeanDependency.toString();
    }

    @Override
    public Class<?> getObjectType() {
        return String.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
