/*
 * Copyright 2011-2017 the original author or authors.
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

package org.cloudfoundry.reconfiguration.util;

import org.junit.Test;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StandardCloudUtilsTest {

    private final ApplicationContext applicationContext = mock(ApplicationContext.class);

    private final CloudFactory cloudFactory = mock(CloudFactory.class);

    private final Cloud cloud = mock(Cloud.class);

    private final StandardCloudUtils cloudUtils = new StandardCloudUtils(this.cloudFactory);

    @Test
    public void getCloudFactoryNew() {
        assertNotSame(this.cloudFactory, new StandardCloudUtils().getCloudFactory());
    }

    @Test
    public void getCloudFactoryExisting() {
        assertSame(this.cloudFactory, this.cloudUtils.getCloudFactory());
    }

    @Test
    public void isInCloudTrue() {
        when(this.cloudFactory.getCloud()).thenReturn(this.cloud);

        assertTrue(this.cloudUtils.isInCloud());
    }

    @Test
    public void isInCloudFalse() {
        when(this.cloudFactory.getCloud()).thenThrow(new CloudException(""));

        assertFalse(this.cloudUtils.isInCloud());
    }

    @Test
    public void isUsingCloudServices() throws IOException {
        when(this.applicationContext.getResources("classpath*:/META-INF/cloud/cloud-services")).thenReturn(
                new Resource[]{new FileSystemResource("src/test/resources/cloud-services")});
        when(this.applicationContext.getBeanNamesForType(UnusedCloudService.class, true,
                false)).thenReturn(new String[0]);
        when(this.applicationContext.getBeanNamesForType(UsedCloudService.class, true,
                false)).thenReturn(new String[]{"used-cloud-service-bean-name"});

        assertTrue(this.cloudUtils.isUsingCloudServices(this.applicationContext));
    }

    @Test
    public void isUsingCloudServicesInvalidCloudService() throws IOException {
        when(this.applicationContext.getResources("classpath*:/META-INF/cloud/cloud-services")).thenReturn(
                new Resource[]{new FileSystemResource("src/test/resources/invalid-cloud-services")});

        assertFalse(this.cloudUtils.isUsingCloudServices(this.applicationContext));

        verify(this.applicationContext, times(0)).getBeanNamesForType(any(Class.class));
    }

    public static final class UnusedCloudService {

    }

    public static final class UsedCloudService {

    }
}

