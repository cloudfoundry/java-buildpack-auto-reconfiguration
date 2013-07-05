/*
 * Copyright 2013 the original author or authors.
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

package org.cloudfoundry.reconfiguration.play;

import org.cloudfoundry.runtime.env.CloudEnvironment;

import play.*;

/**
 * The {@link PlayPlugin} class is a Play plugin which overrides a Play
 * application's configuration in order to reconfigure it for running in
 * Cloud Foundry.
 */
public class PlayPlugin extends Plugin {
	
	/**
	 * Override the application's configuration. This is done statically
	 * and therefore globally by setting Java system properties.
	 * 
	 * @param application the application to be overridden
	 */
	public PlayPlugin(Application _) {
		CloudEnvironment environment = new CloudEnvironment();
		new Configurer(new AppConfiguration(environment), new PropertySetter(environment)).configure();
	}

} 
