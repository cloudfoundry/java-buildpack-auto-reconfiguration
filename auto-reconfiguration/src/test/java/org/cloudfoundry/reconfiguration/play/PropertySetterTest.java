package org.cloudfoundry.reconfiguration.play;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
import org.springframework.cloud.service.common.PostgresqlServiceInfo;
import org.springframework.cloud.service.common.RelationalServiceInfo;
import org.springframework.cloud.util.UriInfo;

/**
 * Unit test of {@link PropertySetter}
 *
 * @author Jennifer Hickey
 * @author Ramnivas Laddad
 *
 */
public class PropertySetterTest {

	@Mock
	private Cloud cloud;

	private PropertySetter propertySetter;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		propertySetter = new PropertySetter(cloud);
	}

	@After
	public void tearDown() {
		System.clearProperty("cloud.services.mysql.connection.name");
		System.clearProperty("cloud.services.mysql.connection.driver");
		System.clearProperty("cloud.services.mysql.connection.jdbcUrl");
		System.clearProperty("cloud.services.postgresql.connection.driver");
		System.clearProperty("cloud.services.postgresql.connection.jdbcUrl");
		System.clearProperty("cloud.services.postgresql.connection.name");
		System.clearProperty("cloud.services.service1.connection.username");
		System.clearProperty("cloud.services.service1.connection.name");
		System.clearProperty("cloud.services.service1.connection.driver");
		System.clearProperty("cloud.services.service1.connection.jdbcUrl");
		System.clearProperty("cloud.services.service2.connection.driver");
		System.clearProperty("cloud.services.service2.connection.jdbcUrl");
		System.clearProperty("cloud.services.service3.connection.driver");
		System.clearProperty("cloud.services.service3.connection.jdbcUrl");
		System.clearProperty("applyEvolutions.default");
		System.clearProperty("applyEvolutions.other");
	}

	@Test
	public void setCloudPropertiesMultiplePostgres() {
		Properties cloudProps = new Properties();
		cloudProps.put("cloud.services.service1.connection.username", "foo");
		cloudProps.put("cloud.services.service1.connection.name", "testdb");
		cloudProps.put("cloud.services.mysql.connection.name", "testmysqldb");
		when(cloud.getCloudProperties()).thenReturn(cloudProps);
        RelationalServiceInfo pgInfo1 = new PostgresqlServiceInfo("service1", 
                new UriInfo("mysql", "localhost", 5678, "foo", "bar", "testdb").toString());
		RelationalServiceInfo pgInfo2 = new PostgresqlServiceInfo("service2", 
                new UriInfo("mysql", "localhost", 3306, "testuser", "testpwd", "testdb2").toString());
        RelationalServiceInfo mysqlInfo = new MysqlServiceInfo("service3", 
                new UriInfo("mysql", "localhost", 1234, "bob", "bobspwd", "testmysqldb").toString());
		List<ServiceInfo> services = Arrays
				.asList(new ServiceInfo[] { pgInfo1, pgInfo2, mysqlInfo });
		when(cloud.getServiceInfos(DataSource.class)).thenReturn(services);
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
		assertEquals("jdbc:postgresql://localhost:5678/testdb?user=foo&password=bar",
				System.getProperty("cloud.services.service1.connection.jdbcUrl"));
		assertEquals("jdbc:postgresql://localhost:3306/testdb2?user=testuser&password=testpwd",
				System.getProperty("cloud.services.service2.connection.jdbcUrl"));
		assertNull(System.getProperty("cloud.services.postgresql.connection.jdbcUrl"));
		assertEquals("jdbc:mysql://localhost:1234/testmysqldb?user=bob&password=bobspwd",
				System.getProperty("cloud.services.service3.connection.jdbcUrl"));
		assertEquals("jdbc:mysql://localhost:1234/testmysqldb?user=bob&password=bobspwd",
				System.getProperty("cloud.services.mysql.connection.jdbcUrl"));
	}

	@Test
	public void setCloudPropertiesMultipleMysql() {
		Properties cloudProps = new Properties();
		cloudProps.put("cloud.services.service1.connection.username", "foo");
		cloudProps.put("cloud.services.service1.connection.name", "testdb");
		cloudProps.put("cloud.services.postgresql.connection.name", "testdb");
		when(cloud.getCloudProperties()).thenReturn(cloudProps);
        RelationalServiceInfo pgInfo = new PostgresqlServiceInfo("service1", 
                new UriInfo("postgres", "localhost", 5678, "foo", "bar", "testdb").toString());
        RelationalServiceInfo mysqlInfo1 = new MysqlServiceInfo("service2", 
                new UriInfo("mysql", "localhost", 1234, "bob", "bobspwd", "testmysqldb").toString());
        RelationalServiceInfo mysqlInfo2 = new MysqlServiceInfo("service3", 
                new UriInfo("mysql", "localhost", 4566, "joe", "joespwd", "testmysqldb2").toString());
		List<ServiceInfo> services = Arrays.asList(new ServiceInfo[] { pgInfo, mysqlInfo1,
				mysqlInfo2 });
		when(cloud.getServiceInfos(DataSource.class)).thenReturn(services);
		propertySetter.setCloudProperties();
		assertEquals("foo", System.getProperty("cloud.services.service1.connection.username"));
		assertEquals("testdb", System.getProperty("cloud.services.service1.connection.name"));
		assertEquals("testdb", System.getProperty("cloud.services.postgresql.connection.name"));
		assertEquals(PropertySetter.POSTGRES_DRIVER_CLASS,
				System.getProperty("cloud.services.service1.connection.driver"));
		assertEquals(PropertySetter.POSTGRES_DRIVER_CLASS,
				System.getProperty("cloud.services.postgresql.connection.driver"));
		assertEquals("jdbc:postgresql://localhost:5678/testdb?user=foo&password=bar",
				System.getProperty("cloud.services.postgresql.connection.jdbcUrl"));
		assertEquals("jdbc:postgresql://localhost:5678/testdb?user=foo&password=bar",
				System.getProperty("cloud.services.service1.connection.jdbcUrl"));
		assertEquals(PropertySetter.MYSQL_DRIVER_CLASS,
				System.getProperty("cloud.services.service2.connection.driver"));
		assertEquals(PropertySetter.MYSQL_DRIVER_CLASS,
				System.getProperty("cloud.services.service3.connection.driver"));
		assertNull(System.getProperty("cloud.services.mysql.connection.jdbcUrl"));
		assertEquals("jdbc:mysql://localhost:1234/testmysqldb?user=bob&password=bobspwd",
				System.getProperty("cloud.services.service2.connection.jdbcUrl"));
		assertEquals("jdbc:mysql://localhost:4566/testmysqldb2?user=joe&password=joespwd",
				System.getProperty("cloud.services.service3.connection.jdbcUrl"));
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
