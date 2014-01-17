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

import org.junit.Test;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public abstract class AbstractJpaBasedCloudServiceBeanFactoryPostProcessorTest extends
        AbstractCloudServiceBeanFactoryPostProcessorTest<EntityManagerFactory> {

    public AbstractJpaBasedCloudServiceBeanFactoryPostProcessorTest(Class<EntityManagerFactory> beanClass,
                                                                    String contextStem, ServiceInfo serviceInfo) {
        super(beanClass, contextStem, serviceInfo);
    }

    @Test
    public final void typedStringValueReconfigure() {
        assertReconfigured("classpath:%s-typed-string-value.xml");
    }

    protected final void assertConfiguration(EntityManagerFactory factory, String dialect) {
        EntityManager entityManager = factory.createEntityManager();

        Object session = getSession(entityManager);
        Object sessionFactory = getSessionFactory(session);
        Object actual = getDialect(sessionFactory);

        assertEquals(dialect, actual.toString());
    }

    private Object getSession(EntityManager manager) {
        Method method = ReflectionUtils.findMethod(manager.getClass(), "getDelegate");
        return ReflectionUtils.invokeMethod(method, manager);
    }

    private Object getSessionFactory(Object session) {
        Method method = ReflectionUtils.findMethod(session.getClass(), "getFactory");
        return ReflectionUtils.invokeMethod(method, session);
    }

    private Object getDialect(Object factory) {
        Method method = ReflectionUtils.findMethod(factory.getClass(), "getDialect");
        return ReflectionUtils.invokeMethod(method, factory);
    }

}
