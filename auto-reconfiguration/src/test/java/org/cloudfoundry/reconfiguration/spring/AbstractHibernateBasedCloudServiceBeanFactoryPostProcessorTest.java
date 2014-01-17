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

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public abstract class AbstractHibernateBasedCloudServiceBeanFactoryPostProcessorTest extends
        AbstractCloudServiceBeanFactoryPostProcessorTest<SessionFactory> {

    protected AbstractHibernateBasedCloudServiceBeanFactoryPostProcessorTest(Class<SessionFactory> beanClass,
                                                                             String contextStem,
                                                                             ServiceInfo serviceInfo) {
        super(beanClass, contextStem, serviceInfo);
    }

    @Test
    public final void embeddedPropertiesReconfigure() {
        assertReconfigured("classpath:%s-embedded-properties.xml");
    }

    @Test
    public final void referencedMapReconfigure() {
        assertReconfigured("classpath:%s-referenced-map.xml");
    }

    @Test
    public final void propertyFactoryReconfigure() {
        assertReconfigured("classpath:%s-property-factory.xml");
    }

    @Test
    public final void propertyFactoryNestedSingleLocationReconfigure() {
        assertReconfigured("classpath:%s-property-factory-nested-single-location.xml");
    }

    @Test
    public final void propertyFactoryNestedMultipleLocationsReconfigure() {
        assertReconfigured("classpath:%s-property-factory-nested-multiple-locations.xml");
    }

    protected final void assertConfiguration(SessionFactory factory, String dialect) {
        Object actual = getDialect(factory);

        assertEquals(dialect, actual.toString());
    }

    private Object getDialect(SessionFactory factory) {
        Method method = ReflectionUtils.findMethod(factory.getClass(), "getDialect");
        return ReflectionUtils.invokeMethod(method, factory);
    }

}
