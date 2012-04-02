package org.cloudfoundry.reconfiguration.play;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test of the {@link Configurer}
 *
 * @author Jennifer Hickey
 *
 */
public class ConfigurerTest {

	@Mock
	private AppConfiguration appConfiguration;

	@Mock
	private PropertySetter propertySetter;

	private Configurer configurer;

	private Properties appProps = new Properties();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		configurer = new Configurer(appConfiguration, propertySetter);
		appProps.put("db.default.driver", "org.h2.Driver");
		appProps.put("db.default.url", "jdbc:h2:mem:play");
		appProps.put("db.default.user", "sa");
		appProps.put("db.default.password", "admin");
	}

	@After
	public void tearDown() {
		System.clearProperty("db.default.url");
		System.clearProperty("db.default.user");
		System.clearProperty("db.default.password");
		System.clearProperty("db.default.driver");
		System.clearProperty("jpaplugin");
		System.clearProperty("cfjpaplugin");
	}

	@Test
	public void configurePlayDBSinglePostgresService() throws IOException {
		RdbmsServiceInfo serviceInfo = ServiceHelper.createServiceInfo("myservice", "localhost", 5678, "foo",
				"bar", "testdb", "postgresql-9.0");
		when(appConfiguration.getPlayConfiguration()).thenReturn(appProps);
		when(appConfiguration.getPlayDatabaseNames()).thenReturn(Collections.singleton("default"));
		when(appConfiguration.getCFConfiguration()).thenReturn(new Properties());
		when(appConfiguration.getDatabaseBinding()).thenReturn(serviceInfo);
		configurer.configure();
		verify(propertySetter).setCloudProperties();
		verify(propertySetter).setDatabaseProperties(Collections.singleton("default"));
		assertEquals(serviceInfo.getUrl(), System.getProperty("db.default.url"));
		assertEquals(serviceInfo.getUserName(), System.getProperty("db.default.user"));
		assertEquals(serviceInfo.getPassword(), System.getProperty("db.default.password"));
		assertEquals(PropertySetter.POSTGRES_DRIVER_CLASS, System.getProperty("db.default.driver"));
		assertEquals("disabled", System.getProperty("jpaplugin"));
		assertEquals("enabled", System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configurePlayDBSingleMySQLService() throws IOException {
		RdbmsServiceInfo serviceInfo = ServiceHelper.createServiceInfo("myservice", "localhost", 5678, "foo",
				"bar", "testdb", "mysql-5.1");
		when(appConfiguration.getPlayConfiguration()).thenReturn(appProps);
		when(appConfiguration.getPlayDatabaseNames()).thenReturn(Collections.singleton("default"));
		when(appConfiguration.getCFConfiguration()).thenReturn(new Properties());
		when(appConfiguration.getDatabaseBinding()).thenReturn(serviceInfo);
		configurer.configure();
		verify(propertySetter).setCloudProperties();
		verify(propertySetter).setDatabaseProperties(Collections.singleton("default"));
		assertEquals(serviceInfo.getUrl(), System.getProperty("db.default.url"));
		assertEquals(serviceInfo.getUserName(), System.getProperty("db.default.user"));
		assertEquals(serviceInfo.getPassword(), System.getProperty("db.default.password"));
		assertEquals(PropertySetter.MYSQL_DRIVER_CLASS, System.getProperty("db.default.driver"));
		assertEquals("disabled", System.getProperty("jpaplugin"));
		assertEquals("enabled", System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configureSinglePlayDBNoOrMultipleServices() throws IOException {
		when(appConfiguration.getPlayConfiguration()).thenReturn(appProps);
		when(appConfiguration.getPlayDatabaseNames()).thenReturn(Collections.singleton("default"));
		when(appConfiguration.getCFConfiguration()).thenReturn(new Properties());
		when(appConfiguration.getDatabaseBinding()).thenReturn(null);
		configurer.configure();
		verify(propertySetter).setCloudProperties();
		assertNull(System.getProperty("db.default.url"));
		assertNull(System.getProperty("db.default.user"));
		assertNull(System.getProperty("db.default.password"));
		assertNull(System.getProperty("db.default.driver"));
		assertNull(System.getProperty("jpaplugin"));
		assertNull(System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configureNoPlayDBs() throws IOException {
		when(appConfiguration.getPlayConfiguration()).thenReturn(appProps);
		when(appConfiguration.getPlayDatabaseNames()).thenReturn(new HashSet<String>());
		configurer.configure();
		verify(propertySetter).setCloudProperties();
		assertNull(System.getProperty("db.default.url"));
		assertNull(System.getProperty("db.default.user"));
		assertNull(System.getProperty("db.default.password"));
		assertNull(System.getProperty("db.default.driver"));
		assertNull(System.getProperty("jpaplugin"));
		assertNull(System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configureMultiplePlayDBs() throws IOException {
		when(appConfiguration.getPlayConfiguration()).thenReturn(appProps);
		Set<String> dbs = new HashSet<String>();
		dbs.add("default");
		dbs.add("other");
		when(appConfiguration.getPlayDatabaseNames()).thenReturn(dbs);
		configurer.configure();
		verify(propertySetter).setDatabaseProperties(dbs);
		verify(propertySetter).setCloudProperties();
		assertNull(System.getProperty("db.default.url"));
		assertNull(System.getProperty("db.default.user"));
		assertNull(System.getProperty("db.default.password"));
		assertNull(System.getProperty("db.default.driver"));
		assertNull(System.getProperty("jpaplugin"));
		assertNull(System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configureSinglePlayDBJpaDisabled() throws IOException {
		appProps.put("jpaplugin", "disabled");
		RdbmsServiceInfo serviceInfo = ServiceHelper.createServiceInfo("myservice", "localhost", 5678, "foo",
				"bar", "testdb", "mysql-5.1");
		when(appConfiguration.getPlayConfiguration()).thenReturn(appProps);
		when(appConfiguration.getPlayDatabaseNames()).thenReturn(Collections.singleton("default"));
		when(appConfiguration.getCFConfiguration()).thenReturn(new Properties());
		when(appConfiguration.getDatabaseBinding()).thenReturn(serviceInfo);
		configurer.configure();
		verify(propertySetter).setCloudProperties();
		verify(propertySetter).setDatabaseProperties(Collections.singleton("default"));
		assertEquals(serviceInfo.getUrl(), System.getProperty("db.default.url"));
		assertEquals(serviceInfo.getUserName(), System.getProperty("db.default.user"));
		assertEquals(serviceInfo.getPassword(), System.getProperty("db.default.password"));
		assertEquals(PropertySetter.MYSQL_DRIVER_CLASS, System.getProperty("db.default.driver"));
		assertEquals("disabled", System.getProperty("jpaplugin"));
		assertNull(System.getProperty("cfjpaplugin"));
	}

	@Test
	public void errorGettingPlayConfiguration() throws IOException {
		when(appConfiguration.getPlayConfiguration()).thenThrow(new FileNotFoundException("File not found"));
		configurer.configure();
		verify(propertySetter).setCloudProperties();
	}

	@Test
	public void autoconfigDisabledByConfigFile() throws IOException {
		Properties cfProps = new Properties();
		cfProps.put("autoconfig", "false");
		when(appConfiguration.getCFConfiguration()).thenReturn(cfProps);
		assertTrue(configurer.autoconfigDisabled(new Properties()));
	}

	@Test
	public void autoconfigNotDisabledByConfigFile() throws IOException {
		when(appConfiguration.getCFConfiguration()).thenReturn(new Properties());
		assertFalse(configurer.autoconfigDisabled(new Properties()));
	}

	@Test
	public void autoconfigDisabledConfigFileNotFound() throws IOException {
		when(appConfiguration.getCFConfiguration()).thenThrow(new FileNotFoundException());
		assertFalse(configurer.autoconfigDisabled(new Properties()));
	}

	@Test
	public void autoconfigDisabledErrorReadingConfigFile() throws IOException {
		when(appConfiguration.getCFConfiguration()).thenThrow(new IOException("Something bad happened"));
		assertFalse(configurer.autoconfigDisabled(new Properties()));
	}

	@Test
	public void autoconfigDisabledByUsageOfSysProps() throws IOException {
		Properties props = new Properties();
		props.put("db.default.url",
				"jdbc:mysql://${cloud.services.mysql.connection.host}:${cloud.services.mysql.connection.port}/"
						+ "${cloud.services.mysql.connection.name}");
		when(appConfiguration.getCFConfiguration()).thenReturn(new Properties());
		assertTrue(configurer.autoconfigDisabled(props));
	}

	@Test
	public void autoconfigNotDisabledByUsageOfSysProps() throws IOException {
		Properties props = new Properties();
		props.put("db.default.url", "jdbc:mysql://localhost:3306/testdb");
		when(appConfiguration.getCFConfiguration()).thenReturn(new Properties());
		assertFalse(configurer.autoconfigDisabled(props));
	}
}
