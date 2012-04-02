package org.cloudfoundry.reconfiguration.play;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import play.Application;
import play.Configuration;

/**
 * Unit test of the {link {@link JPAPlugin}
 * @author Jennifer Hickey
 *
 */
public class JPAPluginTest {

	private JPAPlugin plugin;

	@Mock
	private Application application;

	@Mock
	private PlayConfigurer playConfigurer;

	@Mock
	private Configuration configuration;

	@Before
	public void onStartWithPostgresService() {
		MockitoAnnotations.initMocks(this);
		plugin = new JPAPlugin(application, playConfigurer);
	}

	@Test
	public void getPropertiesPostgresService() {
		RdbmsServiceInfo serviceInfo = createServiceInfo("myservice",
				"localhost", 5678, "foo", "bar", "testdb", "postgresql-9.0");
		when(playConfigurer.getDatabaseBinding()).thenReturn(serviceInfo);
		Map<String, String> expectedProps = new HashMap<String, String>();
		expectedProps.put("hibernate.dialect", JPAPlugin.POSTGRES_DIALECT);
		assertEquals(expectedProps, plugin.getProperties());
	}

	@Test
	public void getPropertiesMySQLService() {
		RdbmsServiceInfo serviceInfo = createServiceInfo("myservice",
				"localhost", 5678, "foo", "bar", "testdb", "mysql-5.1");
		when(playConfigurer.getDatabaseBinding()).thenReturn(serviceInfo);
		Map<String, String> expectedProps = new HashMap<String, String>();
		expectedProps.put("hibernate.dialect", JPAPlugin.MYSQL_DIALECT);
		assertEquals(expectedProps, plugin.getProperties());
	}

	@Test
	public void getPropertiesNoService() {
		when(playConfigurer.getDatabaseBinding()).thenReturn(null);
		Map<String, String> expectedProps = new HashMap<String, String>();
		assertEquals(expectedProps, plugin.getProperties());
	}

	@Test
	public void shouldBeEnabled() {
		when(application.configuration()).thenReturn(configuration);
		when(configuration.getString("cfjpaplugin")).thenReturn("enabled");
		assertTrue(plugin.enabled());
	}

	@Test
	public void shouldBeEnabledConfigNull() {
		when(application.configuration()).thenReturn(configuration);
		when(configuration.getString("cfjpaplugin")).thenReturn(null);
		assertFalse(plugin.enabled());
	}

	@Test
	public void shouldBeEnabledConfigDisabled() {
		when(application.configuration()).thenReturn(configuration);
		when(configuration.getString("cfjpaplugin")).thenReturn("disabled");
		assertFalse(plugin.enabled());
	}

	private RdbmsServiceInfo createServiceInfo(String serviceName,
			String hostname, int port, String user, String password,
			String name, String label) {
		Map<String, Object> serviceInfo = new HashMap<String, Object>();
		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("hostname", hostname);
		credentials.put("port", port);
		credentials.put("password", password);
		credentials.put("name", name);
		credentials.put("user", user);
		serviceInfo.put("credentials", credentials);
		serviceInfo.put("name", serviceName);
		serviceInfo.put("play", "free");
		serviceInfo.put("label", label);
		return new RdbmsServiceInfo(serviceInfo);
	}
}
