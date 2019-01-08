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

import org.junit.Test;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public final class CloudAutoReconfigurationServletContainerInitializerTest {

    @Test
    public void onStartup() throws ServletException {
        ServletContext servletContext = new MockServletContext();

        new CloudAutoReconfigurationServletContainerInitializer().onStartup(null, servletContext);

        assertThat(servletContext.getInitParameter("globalInitializerClasses")).isEqualTo(getGlobalInitializerClasses());
    }

    private String getGlobalInitializerClasses() {
        return Stream
            .of(
                CloudProfileApplicationContextInitializer.class.getCanonicalName(),
                CloudPropertySourceApplicationContextInitializer.class.getCanonicalName(),
                CloudServiceReconfigurationApplicationContextInitializer.class.getCanonicalName())
            .collect(Collectors.joining(","));
    }

}
