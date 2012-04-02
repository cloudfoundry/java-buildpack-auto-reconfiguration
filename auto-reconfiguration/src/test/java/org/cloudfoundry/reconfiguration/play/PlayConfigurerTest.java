package org.cloudfoundry.reconfiguration.play;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test of the {@link PlayConfigurer}
 *
 * @author jencompgeek
 *
 */
public class PlayConfigurerTest {

	@Mock
	private CloudEnvironment cloudEnvironment;

	private PlayConfigurer configurer;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		configurer = new PlayConfigurer(cloudEnvironment);
	}

	@After
	public void tearDown() {
		System.clearProperty("db.default.url");
		System.clearProperty("db.default.user");
		System.clearProperty("db.default.password");
		System.clearProperty("applyEvolutions.default");
		System.clearProperty("db.default.driver");
		System.clearProperty("jpaplugin");
		System.clearProperty("cfjpaplugin");
	}

	@Test
	public void configureSinglePlayDBSinglePostgresService() {
		System.setProperty("config.file",
				"org/cloudfoundry/reconfiguration/play/play-single-db.conf");
		RdbmsServiceInfo serviceInfo = createServiceInfo("myservice",
				"localhost", 5678, "foo", "bar", "testdb", "postgresql-9.0");
		when(cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class))
				.thenReturn(Collections.singletonList(serviceInfo));
		configurer.configure();
		assertEquals(serviceInfo.getUrl(), System.getProperty("db.default.url"));
		assertEquals(serviceInfo.getUserName(),
				System.getProperty("db.default.user"));
		assertEquals(serviceInfo.getPassword(),
				System.getProperty("db.default.password"));
		assertEquals(PlayConfigurer.POSTGRES_DRIVER_CLASS,
				System.getProperty("db.default.driver"));
		assertEquals("true", System.getProperty("applyEvolutions.default"));
		assertEquals("disabled", System.getProperty("jpaplugin"));
		assertEquals("enabled", System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configureSinglePlayDBSingleMySQLService() {
		System.setProperty("config.file",
				"org/cloudfoundry/reconfiguration/play/play-single-db.conf");
		RdbmsServiceInfo serviceInfo = createServiceInfo("myservice",
				"localhost", 5678, "foo", "bar", "testdb", "mysql-5.1");
		when(cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class))
				.thenReturn(Collections.singletonList(serviceInfo));
		configurer.configure();
		assertEquals(serviceInfo.getUrl(), System.getProperty("db.default.url"));
		assertEquals(serviceInfo.getUserName(),
				System.getProperty("db.default.user"));
		assertEquals(serviceInfo.getPassword(),
				System.getProperty("db.default.password"));
		assertEquals(PlayConfigurer.MYSQL_DRIVER_CLASS,
				System.getProperty("db.default.driver"));
		assertEquals("true", System.getProperty("applyEvolutions.default"));
		assertEquals("disabled", System.getProperty("jpaplugin"));
		assertEquals("enabled", System.getProperty("cfjpaplugin"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void configureSinglePlayDBNoServices() {
		System.setProperty("config.file",
				"org/cloudfoundry/reconfiguration/play/play-single-db.conf");
		when(cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class))
				.thenReturn(Collections.EMPTY_LIST);
		configurer.configure();
		assertNull(System.getProperty("db.default.url"));
		assertNull(System.getProperty("db.default.user"));
		assertNull(System.getProperty("db.default.password"));
		assertNull(System.getProperty("db.default.driver"));
		assertEquals("true",System.getProperty("applyEvolutions.default"));
		assertNull(System.getProperty("jpaplugin"));
		assertNull(System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configureSinglePlayDBMultipleDBServices() {
		System.setProperty("config.file",
				"org/cloudfoundry/reconfiguration/play/play-single-db.conf");
		RdbmsServiceInfo mysqlServiceInfo = createServiceInfo("myservice",
				"localhost", 5678, "foo", "bar", "testdb", "mysql-5.1");
		RdbmsServiceInfo postgresServiceInfo = createServiceInfo("myservice",
				"localhost", 5678, "foo", "bar", "testdb", "postgresql-9.0");
		List<RdbmsServiceInfo> services = new ArrayList<RdbmsServiceInfo>();
		services.add(mysqlServiceInfo);
		services.add(postgresServiceInfo);
		when(cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class))
				.thenReturn(services);
		configurer.configure();
		assertNull(System.getProperty("db.default.url"));
		assertNull(System.getProperty("db.default.user"));
		assertNull(System.getProperty("db.default.password"));
		assertNull(System.getProperty("db.default.driver"));
		assertEquals("true",System.getProperty("applyEvolutions.default"));
		assertNull(System.getProperty("jpaplugin"));
		assertNull(System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configureSinglePlayDBMultipleDBServicesOneNamedProduction() {
		System.setProperty("config.file",
				"org/cloudfoundry/reconfiguration/play/play-single-db.conf");
		RdbmsServiceInfo mysqlServiceInfo = createServiceInfo("myservice",
				"localhost", 5678, "foo", "bar", "testdb", "mysql-5.1");
		RdbmsServiceInfo postgresServiceInfo = createServiceInfo(
				"myservice-production", "localhost", 5678, "foo", "bar",
				"testdb", "postgresql-9.0");
		List<RdbmsServiceInfo> services = new ArrayList<RdbmsServiceInfo>();
		services.add(mysqlServiceInfo);
		services.add(postgresServiceInfo);
		when(cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class))
				.thenReturn(services);
		configurer.configure();
		assertEquals(postgresServiceInfo.getUrl(),
				System.getProperty("db.default.url"));
		assertEquals(postgresServiceInfo.getUserName(),
				System.getProperty("db.default.user"));
		assertEquals(postgresServiceInfo.getPassword(),
				System.getProperty("db.default.password"));
		assertEquals(PlayConfigurer.POSTGRES_DRIVER_CLASS,
				System.getProperty("db.default.driver"));
		assertEquals("true", System.getProperty("applyEvolutions.default"));
		assertEquals("disabled", System.getProperty("jpaplugin"));
		assertEquals("enabled", System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configureSinglePlayDBMultipleDBServicesOneNamedProd() {
		System.setProperty("config.file",
				"org/cloudfoundry/reconfiguration/play/play-single-db.conf");
		RdbmsServiceInfo mysqlServiceInfo = createServiceInfo("myservice",
				"localhost", 5678, "foo", "bar", "testdb", "mysql-5.1");
		RdbmsServiceInfo postgresServiceInfo = createServiceInfo(
				"myservice-prod", "localhost", 5678, "foo", "bar", "testdb",
				"postgresql-9.0");
		List<RdbmsServiceInfo> services = new ArrayList<RdbmsServiceInfo>();
		services.add(mysqlServiceInfo);
		services.add(postgresServiceInfo);
		when(cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class))
				.thenReturn(services);
		configurer.configure();
		assertEquals(postgresServiceInfo.getUrl(),
				System.getProperty("db.default.url"));
		assertEquals(postgresServiceInfo.getUserName(),
				System.getProperty("db.default.user"));
		assertEquals(postgresServiceInfo.getPassword(),
				System.getProperty("db.default.password"));
		assertEquals(PlayConfigurer.POSTGRES_DRIVER_CLASS,
				System.getProperty("db.default.driver"));
		assertEquals("true", System.getProperty("applyEvolutions.default"));
		assertEquals("disabled", System.getProperty("jpaplugin"));
		assertEquals("enabled", System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configureSinglePlayDBMultipleDBServicesTwoNamedProd() {
		System.setProperty("config.file",
				"org/cloudfoundry/reconfiguration/play/play-single-db.conf");
		RdbmsServiceInfo mysqlServiceInfo = createServiceInfo(
				"myothersvc-prod", "localhost", 5678, "foo", "bar", "testdb",
				"mysql-5.1");
		RdbmsServiceInfo postgresServiceInfo = createServiceInfo(
				"myservice-prod", "localhost", 5678, "foo", "bar", "testdb",
				"postgresql-9.0");
		List<RdbmsServiceInfo> services = new ArrayList<RdbmsServiceInfo>();
		services.add(mysqlServiceInfo);
		services.add(postgresServiceInfo);
		when(cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class))
				.thenReturn(services);
		configurer.configure();
		assertNull(System.getProperty("db.default.url"));
		assertNull(System.getProperty("db.default.user"));
		assertNull(System.getProperty("db.default.password"));
		assertNull(System.getProperty("db.default.driver"));
		assertEquals("true",System.getProperty("applyEvolutions.default"));
		assertNull(System.getProperty("jpaplugin"));
		assertNull(System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configureMultiplePlayDBs() {
		System.setProperty("config.file",
				"org/cloudfoundry/reconfiguration/play/play-multi-dbs.conf");
		RdbmsServiceInfo mysqlServiceInfo = createServiceInfo(
				"myothersvc-prod", "localhost", 5678, "foo", "bar", "testdb",
				"mysql-5.1");
		List<RdbmsServiceInfo> services = new ArrayList<RdbmsServiceInfo>();
		services.add(mysqlServiceInfo);
		when(cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class))
				.thenReturn(services);
		configurer.configure();
		assertNull(System.getProperty("db.default.url"));
		assertNull(System.getProperty("db.default.user"));
		assertNull(System.getProperty("db.default.password"));
		assertNull(System.getProperty("db.default.driver"));
		assertNull(System.getProperty("applyEvolutions.default"));
		assertNull(System.getProperty("jpaplugin"));
		assertNull(System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configureNoPlayDBs() {
		System.setProperty("config.file",
				"org/cloudfoundry/reconfiguration/play/play-no-db.conf");
		RdbmsServiceInfo mysqlServiceInfo = createServiceInfo(
				"myothersvc-prod", "localhost", 5678, "foo", "bar", "testdb",
				"mysql-5.1");
		List<RdbmsServiceInfo> services = new ArrayList<RdbmsServiceInfo>();
		services.add(mysqlServiceInfo);
		when(cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class))
				.thenReturn(services);
		configurer.configure();
		assertNull(System.getProperty("jpaplugin"));
		assertNull(System.getProperty("cfjpaplugin"));
	}

	@Test
	public void configureSinglePlayDBJpaDisabled() {
		System.setProperty("config.file",
				"org/cloudfoundry/reconfiguration/play/play-single-db-jpa-disabled.conf");
		RdbmsServiceInfo serviceInfo = createServiceInfo("myservice",
				"localhost", 5678, "foo", "bar", "testdb", "mysql-5.1");
		when(cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class))
				.thenReturn(Collections.singletonList(serviceInfo));
		configurer.configure();
		assertEquals(serviceInfo.getUrl(), System.getProperty("db.default.url"));
		assertEquals(serviceInfo.getUserName(),
				System.getProperty("db.default.user"));
		assertEquals(serviceInfo.getPassword(),
				System.getProperty("db.default.password"));
		assertEquals(PlayConfigurer.MYSQL_DRIVER_CLASS,
				System.getProperty("db.default.driver"));
		assertEquals("true", System.getProperty("applyEvolutions.default"));
		assertEquals("disabled", System.getProperty("jpaplugin"));
		assertNull(System.getProperty("cfjpaplugin"));
	}

	@Test
	public void autoconfigDisabledByConfigFile() {
		assertTrue(configurer
				.autoconfigDisabled("org/cloudfoundry/reconfiguration/play/cloudfoundry-no-autostaging.properties"));
	}

	@Test
	public void autoconfigNotDisabledByConfigFile() {
		assertFalse(configurer
				.autoconfigDisabled("org/cloudfoundry/reconfiguration/play/cloudfoundry.properties"));
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
