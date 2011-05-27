package org.cloudfoundry.runtime.service.relational;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.cloudfoundry.runtime.env.PostgresqlServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class CloudDataSourceFactoryTest {
	@Mock private CloudEnvironment mockRuntime;
	@Mock private MysqlServiceInfo mockMysqlServiceInfo;
	@Mock private PostgresqlServiceInfo mockPostgresqlServiceInfo;
	private CloudMySqlDataSourceFactory mysqlFactory;
	private CloudPostgresqlDataSourceFactory postgresqlFactory;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mysqlFactory = new CloudMySqlDataSourceFactory(mockRuntime);
		postgresqlFactory = new CloudPostgresqlDataSourceFactory(mockRuntime);
	}
	
	@Test
	public void cloudMySQLDataSourceCreation() throws Exception {
		when(mockRuntime.getServiceInfo("mysql-1", MysqlServiceInfo.class))
			.thenReturn(mockMysqlServiceInfo);
		when(mockMysqlServiceInfo.getUrl()).thenReturn("jdbc:mysql://10.20.30.40:3306/database-123");
		when(mockMysqlServiceInfo.getUserName()).thenReturn("myuser");
		when(mockMysqlServiceInfo.getPassword()).thenReturn("mypass");
		
		mysqlFactory.setServiceName("mysql-1");
		mysqlFactory.afterPropertiesSet();
		
		DataSource dataSource = mysqlFactory.getObject();
		assertNotNull(dataSource);
		
		assertEquals("jdbc:mysql://10.20.30.40:3306/database-123", ReflectionTestUtils.getField(dataSource, "url"));
		assertEquals("myuser", ReflectionTestUtils.invokeGetterMethod(dataSource, "username"));
		assertEquals("mypass", ReflectionTestUtils.invokeGetterMethod(dataSource, "password"));
		assertTrue((Boolean) ReflectionTestUtils.invokeGetterMethod(dataSource, "testOnBorrow"));
		assertTrue(((String) ReflectionTestUtils.invokeGetterMethod(dataSource, "validationQuery")).startsWith("/* ping */"));
	}
	
	@Test
	public void cloudPostgreSQLDataSourceCreation() throws Exception {
		when(mockRuntime.getServiceInfo("postgres-1", PostgresqlServiceInfo.class))
			.thenReturn(mockPostgresqlServiceInfo);
		when(mockPostgresqlServiceInfo.getUrl()).thenReturn("jdbc:postgresql://10.20.30.40:5432/database-123");
		when(mockPostgresqlServiceInfo.getUserName()).thenReturn("pguser");
		when(mockPostgresqlServiceInfo.getPassword()).thenReturn("pgpass");
	
		postgresqlFactory.setServiceName("postgres-1");
		postgresqlFactory.afterPropertiesSet();
		
		DataSource dataSource = postgresqlFactory.getObject();
		assertNotNull(dataSource);
		
		assertEquals("jdbc:postgresql://10.20.30.40:5432/database-123", ReflectionTestUtils.getField(dataSource, "url"));
		assertEquals("pguser", ReflectionTestUtils.invokeGetterMethod(dataSource, "username"));
		assertEquals("pgpass", ReflectionTestUtils.invokeGetterMethod(dataSource, "password"));
		assertNull(((String) ReflectionTestUtils.invokeGetterMethod(dataSource, "validationQuery")));
	}
}
