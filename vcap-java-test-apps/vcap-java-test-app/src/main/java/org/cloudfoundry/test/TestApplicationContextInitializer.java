/*
 * Copyright 2009-2011 the original author or authors.
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
package org.cloudfoundry.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Selects a profile to use based on the value of the TEST_PROFILE environment
 * variable. If TEST_PROFILE is not specified, it defaults to the cloud-services
 * profile.
 *
 * @author Jennifer Hickey
 *
 */
public class TestApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private final Logger log = LoggerFactory.getLogger(TestApplicationContextInitializer.class);

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		String profile = applicationContext.getEnvironment().getProperty("TEST_PROFILE");
		log.info("Using profile " + profile);
		if (profile == null || profile.isEmpty()) {
			applicationContext.getEnvironment().setActiveProfiles("cloud-services");
		} else {
			applicationContext.getEnvironment().setActiveProfiles(profile);
		}
	}
}
