package org.cloudfoundry.reconfiguration.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

public class CloudApplicationContextInitializerTest {

	private CloudApplicationContextInitializer initializer;
	private ConfigurableApplicationContext applicationContext;
	private ConfigurableEnvironment environment;
	private CloudEnvironment cloudEnvironment;
	private Properties cloudProperties;

	@Before
	public void setup() {
		initializer = new CloudApplicationContextInitializer();
		applicationContext = new GenericApplicationContext();
		environment = new StandardEnvironment();
		applicationContext.setEnvironment(environment);
		cloudEnvironment = mock(CloudEnvironment.class);
		cloudProperties = new Properties();
		when(cloudEnvironment.getCloudProperties()).thenReturn(cloudProperties);
		initializer.setCloudFoundryEnvironment(cloudEnvironment);
	}

	@Test
	public void notCloudFoundry() {
		assertTrue(environment.acceptsProfiles("default"));

		initializer.initialize(applicationContext);

		assertTrue(environment.acceptsProfiles("default"));
		assertFalse(environment.acceptsProfiles("cloud"));
	}

	@Test
	public void cloudProfile() {
		assertTrue(environment.acceptsProfiles("default"));
		when(cloudEnvironment.isCloudFoundry()).thenReturn(true);

		initializer.initialize(applicationContext);

		assertTrue(environment.acceptsProfiles("cloud"));
		assertFalse(environment.acceptsProfiles("default"));
	}

	@Test
	public void propertySource() {
		cloudProperties.setProperty("foo", "bar");
		when(cloudEnvironment.isCloudFoundry()).thenReturn(true);

		initializer.initialize(applicationContext);

		assertEquals("bar", environment.getProperty("foo"));
		assertTrue(applicationContext.getEnvironment().getPropertySources().get("cloud") != null);
	}

	@Test
	public void getOrder() {
		Ordered ordered = initializer;
		assertEquals(0, ordered.getOrder());
	}

	@Test
	public void apiBreakage() {
		environment = mock(ConfigurableEnvironment.class);
		applicationContext.setEnvironment(environment);
		try {
			when(cloudEnvironment.isCloudFoundry()).thenReturn(true);
			when(environment.getPropertySources()).thenThrow(new IllegalAccessError("API breakage"));
			initializer.initialize(applicationContext);
		}
		catch (Throwable t) {
			fail("All throwables should be caught");
		}
		finally {
			verify(environment).getPropertySources();
		}
	}

}
