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

package org.cloudfoundry.reconfiguration.grails

import org.cloudfoundry.reconfiguration.spring.CloudAutoReconfigurationApplicationContextInitializer
import org.cloudfoundry.reconfiguration.spring.CloudProfileApplicationContextInitializer
import org.cloudfoundry.reconfiguration.spring.CloudPropertySourceApplicationContextInitializer

class JavaBuildpackAutoReconfigurationGrailsPlugin {
    def version = '1.9.0.BUILD-SNAPSHOT'

    def doWithSpring = {
        def applicationContext = springConfig.unrefreshedApplicationContext

        new CloudProfileApplicationContextInitializer().initialize(applicationContext)
        new CloudPropertySourceApplicationContextInitializer().initialize(applicationContext)
        new CloudAutoReconfigurationApplicationContextInitializer().initialize(applicationContext)
    }
}
