package org.cloudfoundry.reconfiguration.play;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test of {@link PropertySetter}
 *
 * @author Jennifer Hickey
 *
 */
public class PropertySetterTest {

	@Mock
	private CloudEnvironment cloudEnvironment;

	private PropertySetter propertySetter;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		propertySetter = new PropertySetter(cloudEnvironment);
	}

	@After
	public void tearDown() {
		System.clearProperty("cloud.services.mysql.connection.name");
		System.clearProperty("cloud.services.mysql.connection.driver");
		System.clearProperty("cloud.services.mysql.connection.url");
		System.clearProperty("cloud.services.postgresql.connection.driver");
		System.clearProperty("cloud.services.postgresql.connection.url");
		System.clearProperty("cloud.services.postgresql.connection.name");
		System.clearProperty("cloud.services.service1.connection.username");
		System.clearProperty("cloud.services.service1.connection.name");
		System.clearProperty("cloud.services.service1.connection.driver");
		System.clearProperty("cloud.services.service1.connection.url");
		System.clearProperty("cloud.services.service2.connection.driver");
		System.clearProperty("cloud.services.service2.connection.url");
		System.clearProperty("cloud.services.service3.connection.driver");
		System.clearProperty("cloud.services.service3.connection.url");
		System.clearProperty("applyEvolutions.default");
		System.clearProperty("applyEvolutions.other");
	}

	@Test
	public void setCloudPropertiesMultiplePostgres() {
		Properties cloudProps = new Properties();
		cloudProps.put("cloud.services.service1.connection.username", "foo");
		cloudProps.put("cloud.services.service1.connection.name", "testdb");
		cloudProps.put("cloud.services.mysql.connection.name", "testmysqldb");
		when(cloudEnvironment.getCloudProperties()).thenReturn(cloudProps);
		RdbmsServiceInfo pgInfo1 = ServiceHelper.createServiceInfo("service1", "localhost", 5678, "foo",
				"bar", "testdb", "postgresql-9.0");
		RdbmsServiceInfo pgInfo2 = ServiceHelper.createServiceInfo("service2", "localhost", 3306, "testuser",
				"testpwd", "testdb2", "postgresql-9.0");
		RdbmsServiceInfo mysqlInfo = ServiceHelper.createServiceInfo("service3", "localhost", 1234, "bob",
				"bobspwd", "testmysqldb", "mysql-5.1");
		List<RdbmsServiceInfo> services = Arrays
				.asList(new RdbmsServiceInfo[] { pgInfo1, pgInfo2, mysqlInfo });
		when(cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(services);
		propertySetter.setCloudProperties();
		assertEquals("foo", System.getProperty("cloud.services.service1.connection.username"));
		assertEquals("testdb", System.getProperty("cloud.services.service1.connection.name"));
		assertEquals("testmysqldb", System.getProperty("cloud.services.mysql.connection.name"));
		assertEquals(PropertySetter.POSTGRES_DRIVER_CLASS,
				System.getProperty("cloud.services.service1.connection.driver"));
		assertEquals(PropertySetter.POSTGRES_DRIVER_CLASS,
				System.getProperty("cloud.services.service2.connection.driver"));
		assertNull(System.getProperty("cloud.services.postgresql.connection.driver"));
		assertEquals(PropertySetter.MYSQL_DRIVER_CLASS,
				System.getProperty("cloud.services.service3.connection.driver"));
		assertEquals(PropertySetter.MYSQL_DRIVER_CLASS,
				System.getProperty("cloud.services.mysql.connection.driver"));
		assertEquals("jdbc:postgresql://localhost:5678/testdb",
				System.getProperty("cloud.services.service1.connection.url"));
		assertEquals("jdbc:postgresql://localhost:3306/testdb2",
				System.getProperty("cloud.services.service2.connection.url"));
		assertNull(System.getProperty("cloud.services.postgresql.connection.url"));
		assertEquals("jdbc:mysql://localhost:1234/testmysqldb",
				System.getProperty("cloud.services.service3.connection.url"));
		assertEquals("jdbc:mysql://localhost:1234/testmysqldb",
				System.getProperty("cloud.services.mysql.connection.url"));
	}

	@Test
	public void setCloudPropertiesMultipleMysql() {
		Properties cloudProps = new Properties();
		cloudProps.put("cloud.services.service1.connection.username", "foo");
		cloudProps.put("cloud.services.service1.connection.name", "testdb");
		cloudProps.put("cloud.services.postgresql.connection.name", "testdb");
		when(cloudEnvironment.getCloudProperties()).thenReturn(cloudProps);
		RdbmsServiceInfo pgInfo = ServiceHelper.createServiceInfo("service1", "localhost", 5678, "foo",
				"bar", "testdb", "postgresql-9.0");
		RdbmsServiceInfo mysqlInfo1 = ServiceHelper.createServiceInfo("service2", "localhost", 1234, "bob",
				"bobspwd", "testmysqldb", "mysql-5.1");
		RdbmsServiceInfo mysqlInfo2 = ServiceHelper.createServiceInfo("service3", "localhost", 4566, "joe",
				"joespwd", "testmysqldb2", "mysql-5.1");
		List<RdbmsServiceInfo> services = Arrays.asList(new RdbmsServiceInfo[] { pgInfo, mysqlInfo1,
				mysqlInfo2 });
		when(cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(services);
		propertySetter.setCloudProperties();
		assertEquals("foo", System.getProperty("cloud.services.service1.connection.username"));
		assertEquals("testdb", System.getProperty("cloud.services.service1.connection.name"));
		assertEquals("testdb", System.getProperty("cloud.services.postgresql.connection.name"));
		assertEquals(PropertySetter.POSTGRES_DRIVER_CLASS,
				System.getProperty("cloud.services.service1.connection.driver"));
		assertEquals(PropertySetter.POSTGRES_DRIVER_CLASS,
				System.getProperty("cloud.services.postgresql.connection.driver"));
		assertEquals("jdbc:postgresql://localhost:5678/testdb",
				System.getProperty("cloud.services.postgresql.connection.url"));
		assertEquals("jdbc:postgresql://localhost:5678/testdb",
				System.getProperty("cloud.services.service1.connection.url"));
		assertEquals(PropertySetter.MYSQL_DRIVER_CLASS,
				System.getProperty("cloud.services.service2.connection.driver"));
		assertEquals(PropertySetter.MYSQL_DRIVER_CLASS,
				System.getProperty("cloud.services.service3.connection.driver"));
		assertNull(System.getProperty("cloud.services.mysql.connection.url"));
		assertEquals("jdbc:mysql://localhost:1234/testmysqldb",
				System.getProperty("cloud.services.service2.connection.url"));
		assertEquals("jdbc:mysql://localhost:4566/testmysqldb2",
				System.getProperty("cloud.services.service3.connection.url"));
	}

	@Test
	public void setDatabaseProperties() {
		Set<String> dbs = new HashSet<String>();
		dbs.add("default");
		dbs.add("other");
		propertySetter.setDatabaseProperties(dbs);
		assertEquals("true", System.getProperty("applyEvolutions.default"));
		assertEquals("true", System.getProperty("applyEvolutions.other"));
	}
}
