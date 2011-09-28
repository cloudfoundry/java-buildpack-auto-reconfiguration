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
package org.cloudfoundry.runtime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Integration test that deploys the test webapp and ensures that all
 * dependencies are properly resolved and injected when using
 * <cloud:service-scan>
 *
 * @author Jennifer Hickey
 *
 */
public class CloudServicesScannerTest extends CloudServicesTest {

	@Before
	public void setUp() throws IOException {
		super.setUp();
	}

	@Override
	protected String getProfileName() {
		return "service-scan";
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	/**
	 * Verifies that service scan will create beans for bound services when we
	 * do not have services of all types deployed
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void serviceScanMissingSomeServices() throws IOException {
		List<String> serviceNames = createServicesMinusMongo();
		createAndStartApp("vcap-java-test-app", serviceNames);
		assertTrue("Test application is not available", testAppCreator.isAppAvailable(computeAppUrl(), 500l, 120000l));
		Map<String, Object> cloudProps = restTemplate.getForObject(computeAppUrl() + "/properties", Map.class);
		assertFalse(cloudProps.isEmpty());
		// Check for 404s on rest of dependencies
		restTemplate.getForObject(computeAppUrl() + "/mysql", String.class);
		restTemplate.getForObject(computeAppUrl() + "/redis/class", String.class);
		restTemplate.getForObject(computeAppUrl() + "/rabbit", String.class);
		restTemplate.getForObject(computeAppUrl() + "/postgres", String.class);
		try {
			restTemplate.getForObject(computeAppUrl() + "/mongo", String.class);
			fail("Mongo service bean should not be created");
		} catch (HttpClientErrorException e) {
			if (!(e.getStatusCode().equals(HttpStatus.NOT_FOUND))) {
				fail("Expected a 404 when looking for mongo service bean.  Got: " + e);
			}
		}
	}

	/**
	 * Verifies that service scan will not throw any Exceptions if the app is
	 * missing dependencies such as spring-data-mongo, etc. The
	 * missing-deps-test-app uses cloud:service-scan and is actually missing
	 * every service dependency (spring-amqp, spring-data, a DataSource), but
	 * the app should start successfully as long as there are no services bound
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void serviceScanMissingDependencies() throws IOException {
		createAndStartApp("missing-deps-test-app", Collections.EMPTY_LIST);
		assertTrue("Test application is not available", testAppCreator.isAppAvailable(computeAppUrl(), 500l, 120000l));
	}

	private List<String> createServicesMinusMongo() {
		List<String> serviceNames = Arrays.asList(new String[] { DB_SERVICE_NAME, REDIS_SERVICE_NAME,
				RABBIT_SERVICE_NAME, POSTGRES_SERVICE_NAME });
		testAppCreator.createService(DB_SERVICE_NAME, "mysql");
		testAppCreator.createService(REDIS_SERVICE_NAME, "redis");
		testAppCreator.createService(RABBIT_SERVICE_NAME, "rabbitmq");
		testAppCreator.createService(POSTGRES_SERVICE_NAME, "postgresql");
		return serviceNames;
	}
}
