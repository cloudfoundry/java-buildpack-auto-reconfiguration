package org.cloudfoundry.reconfiguration.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.cloudfoundry.reconfiguration.AbstractCloudConfigurerTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.test.CloudTestUtil.StubServiceInfo;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

public class CloudApplicationContextInitializerTest extends AbstractCloudConfigurerTest {

	private CloudApplicationContextInitializer initializer;
	private ConfigurableApplicationContext applicationContext;
	private ConfigurableEnvironment environment;

	@Before
	public void setup() {
		initializer = new CloudApplicationContextInitializer();
	}

	@Test
	public void notCloudFoundry() {
	    applicationContext = new GenericApplicationContext();
	    environment = applicationContext.getEnvironment();
		assertTrue(environment.acceptsProfiles("default"));

		initializer.initialize(applicationContext);

		assertTrue(environment.acceptsProfiles("default"));
		assertFalse(environment.acceptsProfiles("cloud"));
	}

	@Test
	public void cloudProfile() {
        applicationContext = (ConfigurableApplicationContext) getTestApplicationContext(null);
		initializer.initialize(applicationContext);
        environment = applicationContext.getEnvironment();

		assertTrue(environment.acceptsProfiles("cloud"));
		assertFalse(environment.acceptsProfiles("default"));
	}

	@Test
	public void propertySource() {
        StubServiceInfo stubServiceInfo = new StubServiceInfo("my-stub", "cloudhost", 1234, "myuser", "mypass");
        applicationContext = (ConfigurableApplicationContext) getTestApplicationContext(null, stubServiceInfo);
        initializer.initialize(applicationContext);
        environment = applicationContext.getEnvironment();

        assertEquals("my-stub", environment.getProperty("cloud.services.my-stub.id"));
        assertEquals("cloudhost", environment.getProperty("cloud.services.my-stub.connection.host"));
        assertEquals("1234", environment.getProperty("cloud.services.my-stub.connection.port"));
        assertEquals("myuser", environment.getProperty("cloud.services.my-stub.connection.username"));
        assertEquals("mypass", environment.getProperty("cloud.services.my-stub.connection.password"));

        assertTrue(applicationContext.getEnvironment().getPropertySources().get("cloud") != null);
	}

	@Test
	public void getOrder() {
		Ordered ordered = initializer;
		assertEquals(0, ordered.getOrder());
	}
}
