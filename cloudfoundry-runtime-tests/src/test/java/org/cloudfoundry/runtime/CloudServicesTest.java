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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.CloudApplication;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.runtime.test.util.ApplicationCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Integration test that deploys the test webapp and ensures that all
 * dependencies specified by the cloud namespace are properly resolved
 *
 * @author Jennifer Hickey
 *
 */
public class CloudServicesTest {

	private static final String CLOUD_CONTROLLER_URL = System
			.getProperty("vcap.target", "https://api.cloudfoundry.com");
	private static final String TEST_USER_EMAIL = System.getProperty("vcap.email", "java-client-test-user@vmware.com");
	private static final String TEST_USER_PASS = System.getProperty("vcap.passwd");
	private static final String TEST_NAMESPACE = System.getProperty("vcap.test.namespace",
			TEST_USER_EMAIL.substring(0, TEST_USER_EMAIL.indexOf('@')));
	private static final String APP_NAME = namespacedAppName("cf_runtime_test");
	protected static final String DB_SERVICE_NAME = "test_mysql_database";
	protected static final String MONGO_SERVICE_NAME = "test_mongo";
	protected static final String REDIS_SERVICE_NAME = "test_redis";
	protected static final String RABBIT_SERVICE_NAME = "test_rabbit";
	protected static final String POSTGRES_SERVICE_NAME = "test_postgres_database";
	private static final String TEST_APP_DIR = "target/generated-test-resources";
	protected RestTemplate restTemplate = new RestTemplate();
	protected ApplicationCreator testAppCreator;
	private CloudFoundryClient client;

	@BeforeClass
	public static void printTargetInfo() {
		System.out.println("Running tests on " + CLOUD_CONTROLLER_URL + " on behalf of " + TEST_USER_EMAIL);
		if (TEST_USER_PASS == null) {
			fail("System property vcap.passwd must be specified, supply -Dvcap.passwd=<password>");
		}
	}

	@Before
	public void setUp() throws IOException {
		client = new CloudFoundryClient(TEST_USER_EMAIL, TEST_USER_PASS, CLOUD_CONTROLLER_URL);
		this.testAppCreator = new ApplicationCreator(client);
		try {
			client.register(TEST_USER_EMAIL, TEST_USER_PASS);
		} catch (Exception ex) {
			// Ignore... may happen if tear down failed to run properly
			// or the user was register outside this test or we don't have
			// privileges to register a new user.
			// Even if we make a wrong assumption here, the login that follows
			// will fail.
		}
		client.login();

		// clean up old apps and services
		client.deleteAllApplications();
		client.deleteAllServices();

	}

	private List<String> createServices() {
		List<String> serviceNames = Arrays.asList(new String[] { DB_SERVICE_NAME, MONGO_SERVICE_NAME,
				REDIS_SERVICE_NAME, RABBIT_SERVICE_NAME, POSTGRES_SERVICE_NAME });
		testAppCreator.createService(DB_SERVICE_NAME, "mysql");
		testAppCreator.createService(MONGO_SERVICE_NAME, "mongodb");
		testAppCreator.createService(REDIS_SERVICE_NAME, "redis");
		testAppCreator.createService(RABBIT_SERVICE_NAME, "rabbitmq");
		testAppCreator.createService(POSTGRES_SERVICE_NAME, "postgresql");
		return serviceNames;
	}

	protected void createAndStartApp(String appName, List<String> serviceNames) throws IOException {
		File file = new File(TEST_APP_DIR + "/" + appName + ".war");
		assertTrue("Expected test app at " + file.getCanonicalPath(), file.exists());
		List<String> uris = new ArrayList<String>();
		uris.add(computeAppUrl());
		CloudApplication app = testAppCreator.createAndUploadTestApp(file, APP_NAME, serviceNames, uris);
		setTestProfile(app);
		client.startApplication(APP_NAME);
	}

	private void setTestProfile(CloudApplication app) {
		Map<String, String> env = app.getEnvAsMap();
		env.put("TEST_PROFILE", getProfileName());
		client.updateApplicationEnv(app.getName(), env);
	}

	protected String getProfileName() {
		return "cloud-services";
	}

	@After
	public void tearDown() {
		client.deleteAllApplications();
		client.deleteAllServices();
	}

	/**
	 * Verifies that all dependencies specified by the cloud namespace in the
	 * test webapp are properly resolved
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void cloudServiceBindings() throws IOException {
		List<String> serviceNames = createServices();
		createAndStartApp("vcap-java-test-app", serviceNames);
		assertTrue("Test application is not available", testAppCreator.isAppAvailable(computeAppUrl(), 500l, 120000l));
		Map<String, Object> cloudProps = restTemplate.getForObject(computeAppUrl() + "/properties", Map.class);
		assertFalse(cloudProps.isEmpty());
		// Check for 404s on rest of dependencies
		restTemplate.getForObject(computeAppUrl() + "/mysql", String.class);
		restTemplate.getForObject(computeAppUrl() + "/mongo", String.class);
		restTemplate.getForObject(computeAppUrl() + "/redis/class", String.class);
		restTemplate.getForObject(computeAppUrl() + "/rabbit", String.class);
		restTemplate.getForObject(computeAppUrl() + "/postgres", String.class);
	}

	protected String computeAppUrl() {
		return client.getCloudControllerUrl().toString().replace("api", APP_NAME);
	}

	private static String namespacedAppName(String basename) {
		return TEST_NAMESPACE + "-" + basename;
	}
}
