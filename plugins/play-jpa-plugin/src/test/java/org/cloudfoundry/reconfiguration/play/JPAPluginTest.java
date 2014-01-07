package org.cloudfoundry.reconfiguration.play;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;

import play.Application;
import play.Configuration;

/**
 * Unit test of the {link {@link JPAPlugin}
 *
 * @author Jennifer Hickey
 *
 */
public class JPAPluginTest {

	private JPAPlugin plugin;

	@Mock
	private Application application;

	@Mock
	private AppConfiguration appConfiguration;

	@Mock
	private Configuration configuration;

	@Before
	public void onStartWithPostgresService() {
		MockitoAnnotations.initMocks(this);
		plugin = new JPAPlugin(application, appConfiguration);
	}

	@Test
	public void getPropertiesPostgresService() {
		when(appConfiguration.getDatabaseBinding()).thenReturn(new PostgresqlServiceInfo("mydb", "postgres://user:password@hostname:1234/mydb"));
		Map<String, String> expectedProps = new HashMap<String, String>();
		expectedProps.put("hibernate.dialect", JPAPlugin.POSTGRES_DIALECT);
		assertEquals(expectedProps, plugin.getProperties());
	}

	@Test
	public void getPropertiesMySQLService() {
		when(appConfiguration.getDatabaseBinding()).thenReturn(new MysqlServiceInfo("mydb", "mysql://user:password@hostname:1234/mydb"));
		Map<String, String> expectedProps = new HashMap<String, String>();
		expectedProps.put("hibernate.dialect", JPAPlugin.MYSQL_DIALECT);
		assertEquals(expectedProps, plugin.getProperties());
	}

	@Test
	public void getPropertiesNoService() {
		when(appConfiguration.getDatabaseBinding()).thenReturn(null);
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
}
