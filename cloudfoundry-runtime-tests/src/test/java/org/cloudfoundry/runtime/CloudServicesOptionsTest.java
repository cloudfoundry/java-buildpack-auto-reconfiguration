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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Integration test that deploys the test webapp and ensures that all
 * dependencies are properly resolved and injected and advanced options
 * have been applied to the services using the <cloud> namespace
 *
 * @author Jennifer Hickey
 * @author Thomas Risberg
 *
 */
public class CloudServicesOptionsTest extends CloudServicesTest {

	@Before
	public void setUp() throws IOException {
		super.setUp();
	}

	@Override
	protected String getProfileName() {
		return "cloud-services-options";
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	/**
	 * Verifies that the advanced config options have been applied to the cloud services
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void cloudServiceOptionsApplied() throws IOException {
		List<String> serviceNames = createTestServices();
		createAndStartApp("vcap-java-test-app", serviceNames);
		assertTrue("Test application is not available", testAppCreator.isAppAvailable(computeAppUrl(), 500l, 120000l));
		Map<String, Object> cloudProps = restTemplate.getForObject(computeAppUrl() + "/properties", Map.class);
		assertFalse(cloudProps.isEmpty());
		String charSet = restTemplate.getForObject(computeAppUrl() + "/mysql/char-set", String.class);
		assertEquals("Wrong charSet", "utf8", charSet);
		String maxActive = restTemplate.getForObject(computeAppUrl() + "/mysql/max-active", String.class);
		assertEquals("Wrong max-active", "13", maxActive);
		String maxWait = restTemplate.getForObject(computeAppUrl() + "/redis/max-wait", String.class);
		assertEquals("Wrong max-wait", "5500", maxWait);
		String writeConcern = restTemplate.getForObject(computeAppUrl() + "/mongo/write-concern", String.class);
		assertTrue("Wrong fsync in WriteConcern", writeConcern.contains("\"fsync\" : true"));
		assertTrue("Wrong w in WriteConcern", writeConcern.contains("\"w\" : 1"));
		restTemplate.getForObject(computeAppUrl() + "/rabbit", String.class);
		restTemplate.getForObject(computeAppUrl() + "/postgres", String.class);
	}

	private List<String> createTestServices() {
		List<String> serviceNames = Arrays.asList(new String[] { DB_SERVICE_NAME, MONGO_SERVICE_NAME,
				REDIS_SERVICE_NAME, RABBIT_SERVICE_NAME, POSTGRES_SERVICE_NAME });
		testAppCreator.createService(DB_SERVICE_NAME, "mysql");
		testAppCreator.createService(MONGO_SERVICE_NAME, "mongodb");
		testAppCreator.createService(REDIS_SERVICE_NAME, "redis");
		testAppCreator.createService(RABBIT_SERVICE_NAME, "rabbitmq");
		testAppCreator.createService(POSTGRES_SERVICE_NAME, "postgresql");
		return serviceNames;
	}

}
