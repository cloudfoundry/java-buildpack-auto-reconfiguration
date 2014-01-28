package org.cloudfoundry.reconfiguration.play;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.util.UriInfo;

/**
 * Unit test of @{link {@link AppConfiguration}
 *
 * @author Jennifer Hickey
 *
 */
public class AppConfigurationTest {

	private AppConfiguration appConfiguration;

	@Mock
	private Cloud cloud;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		appConfiguration = new AppConfiguration(cloud);
	}

	@After
	public void tearDown() {
		System.clearProperty("config.file");
	}

	@Test
	public void getPlayConfigurationDefaultConfigFile() throws IOException {
		Properties expected = new Properties();
		expected.put("application.name", "Foo");
		expected.put("db.default.driver", "org.h2.Driver");
		expected.put("db.default.url", "\"jdbc:h2:mem:play\"");
		assertEquals(expected, appConfiguration.getPlayConfiguration());
	}

	@Test
	public void getPlayConfigurationAlternateConfigFile() throws IOException {
		System.setProperty("config.file", "src/test/resources/org/cloudfoundry/reconfiguration/play/play-single-db.conf");
		Properties expected = new Properties();
		expected.put("db.default.user", "sa");
		expected.put("db.default.password", "admin");
		expected.put("db.default.driver", "org.h2.Driver");
		expected.put("db.default.url", "\"jdbc:h2:mem:play\"");
		assertEquals(expected, appConfiguration.getPlayConfiguration());
	}

	@Test
	public void getPlayConfigurationWithIncludes() throws IOException {
		System.setProperty("config.file", "src/test/resources/org/cloudfoundry/reconfiguration/play/play-include-file.conf");
		Properties expected = new Properties();
		expected.put("db.default.user", "sa");
		expected.put("db.default.password", "foo");
		expected.put("db.default.driver", "org.mariadb.jdbc.Driver");
		expected.put("db.default.url", "\"jdbc:mysql://localhost:3306/testdb\"");
		expected.put("db.other.driver", "org.h2.Driver");
		expected.put("db.other.url", "\"jdbc:h2:mem:play\"");
		assertEquals(expected, appConfiguration.getPlayConfiguration());
	}

	@Test
	public void getPlayConfigurationWithMultipleIncludes() throws IOException {
		System.setProperty("config.file", "src/test/resources/org/cloudfoundry/reconfiguration/play/play-include-files.conf");
		Properties expected = new Properties();
		expected.put("db.default.user", "sa");
		expected.put("db.default.password", "foo");
		expected.put("db.default.driver", "org.mariadb.jdbc.Driver");
		expected.put("db.default.url", "\"jdbc:mysql://localhost:3306/testdb\"");
		expected.put("db.other.driver", "org.mariadb.jdbc.Driver");
		expected.put("db.other.url", "\"jdbc:h2:mem:play\"");
		expected.put("foo", "bar");
		expected.put("application.name", "Test");
		assertEquals(expected, appConfiguration.getPlayConfiguration());
	}

	@Test(expected = FileNotFoundException.class)
	public void getPlayConfigurationWithNonExistentInclude() throws IOException {
		System.setProperty("config.file", "src/test/resources/org/cloudfoundry/reconfiguration/play/play-include-notfound.conf");
		appConfiguration.getPlayConfiguration();
	}

	@Test(expected = FileNotFoundException.class)
	public void getPlayConfigurationFileNotFound() throws IOException {
		System.setProperty("config.file", "nonexistent.conf");
		appConfiguration.getPlayConfiguration();
	}

	@Test
	public void getCFConfiguration() throws IOException {
		Properties expected = new Properties();
		expected.put("some", "thing");
		assertEquals(expected, appConfiguration.getCFConfiguration());
	}

	@Test
	public void getPlayDatabaseName() {
		Set<String> dbnames = new HashSet<String>();
		dbnames.add("default");
		assertEquals(dbnames, appConfiguration.getPlayDatabaseNames());
	}

	@Test
	public void getPlayDatabaseNameMultiple() {
		System.setProperty("config.file", "src/test/resources/org/cloudfoundry/reconfiguration/play/play-multi-dbs.conf");
		Set<String> dbnames = new HashSet<String>();
		dbnames.add("default");
		dbnames.add("other");
		assertEquals(dbnames,appConfiguration.getPlayDatabaseNames());
	}

	@Test
	public void getPlayDatabaseNameNone() {
		System.setProperty("config.file", "src/test/resources/org/cloudfoundry/reconfiguration/play/play-app.conf");
		assertTrue(appConfiguration.getPlayDatabaseNames().isEmpty());
	}

	@Test
	public void getPlayDatabaseNameErrorReadingConfig() {
		System.setProperty("config.file", "nonexistent.conf");
		assertTrue(appConfiguration.getPlayDatabaseNames().isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getDatabaseBindingNone() {
		when(cloud.getServiceInfos(DataSource.class)).thenReturn(Collections.EMPTY_LIST);
		assertNull(appConfiguration.getDatabaseBinding());
	}

	@Test
	public void getDatabaseBinding() {
		ServiceInfo serviceInfo = new MysqlServiceInfo("myservice", 
		        new UriInfo("mysql", "localhost", 5678, "foo", "bar", "testdb").toString());
		when(cloud.getServiceInfos(DataSource.class)).thenReturn(	Collections.singletonList(serviceInfo));
		assertEquals(serviceInfo, appConfiguration.getDatabaseBinding());
	}

	@Test
	public void getDatabaseBindingMultiple() {
        ServiceInfo serviceInfo = new MysqlServiceInfo("myservice", 
                new UriInfo("mysql", "localhost", 5678, "foo", "bar", "testdb").toString());
        ServiceInfo serviceInfo2 = new MysqlServiceInfo("myservice2", 
                new UriInfo("mysql", "localhost", 1234, "joe", "pwd", "testdb2").toString());
		List<ServiceInfo> services = new ArrayList<ServiceInfo>();
		services.add(serviceInfo);
		services.add(serviceInfo2);
		when(cloud.getServiceInfos(DataSource.class)).thenReturn(services);
		assertNull(appConfiguration.getDatabaseBinding());
	}

	@Test
	public void getDatabaseBindingMultipleOneNamedProduction() {
        ServiceInfo serviceInfo = new MysqlServiceInfo("myservice", 
                new UriInfo("mysql", "localhost", 5678, "foo", "bar", "testdb").toString());
        ServiceInfo serviceInfo2 = new MysqlServiceInfo("production", 
                new UriInfo("mysql", "localhost", 1234, "joe", "pwd", "testdb2").toString());
		List<ServiceInfo> services = new ArrayList<ServiceInfo>();
		services.add(serviceInfo);
		services.add(serviceInfo2);
		when(cloud.getServiceInfos(DataSource.class)).thenReturn(services);
		assertEquals(serviceInfo2, appConfiguration.getDatabaseBinding());
	}

	@Test
	public void getDatabaseBindingMultipleOneNamedProd() {
        ServiceInfo serviceInfo = new MysqlServiceInfo("myservice", 
                new UriInfo("mysql", "localhost", 5678, "foo", "bar", "testdb").toString());
        ServiceInfo serviceInfo2 = new MysqlServiceInfo("myprod", 
                new UriInfo("mysql", "localhost", 1234, "joe", "pwd", "testdb2").toString());
		List<ServiceInfo> services = new ArrayList<ServiceInfo>();
		services.add(serviceInfo);
		services.add(serviceInfo2);
		when(cloud.getServiceInfos(DataSource.class)).thenReturn(services);
		assertEquals(serviceInfo2, appConfiguration.getDatabaseBinding());
	}

	@Test
	public void getDatabaseBindingMultipleTwoNamedProduction() {
        ServiceInfo serviceInfo = new MysqlServiceInfo("myprod", 
                new UriInfo("mysql", "localhost", 5678, "foo", "bar", "testdb").toString());
        ServiceInfo serviceInfo2 = new MysqlServiceInfo("myprod", 
                new UriInfo("mysql", "localhost", 1234, "joe", "pwd", "testdb2").toString());
		List<ServiceInfo> services = new ArrayList<ServiceInfo>();
		services.add(serviceInfo);
		services.add(serviceInfo2);
		when(cloud.getServiceInfos(DataSource.class)).thenReturn(services);
		assertNull(appConfiguration.getDatabaseBinding());
	}

}
