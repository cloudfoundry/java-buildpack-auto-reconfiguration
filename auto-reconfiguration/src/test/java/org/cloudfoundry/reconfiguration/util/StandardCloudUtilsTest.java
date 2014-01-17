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

package org.cloudfoundry.reconfiguration.util;

import org.junit.Test;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.CloudFactory;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public final class StandardCloudUtilsTest {

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
}
