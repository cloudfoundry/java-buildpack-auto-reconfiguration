package org.cloudfoundry.runtime.service.relational;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.cloudfoundry.runtime.env.PostgresqlServiceInfo;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class CloudDataSourceFactoryTest {
	@Mock private CloudEnvironment mockRuntime;
	@Mock private RdbmsServiceInfo mockRdbmsServiceInfo;
	private CloudDataSourceFactory rdbmsFactory;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		rdbmsFactory = new CloudDataSourceFactory(mockRuntime);
	}
	
	@Test
	public void cloudMySQLDataSourceCreation() throws Exception {
		when(mockRuntime.getServiceInfo("mysql-1", RdbmsServiceInfo.class))
			.thenReturn(mockRdbmsServiceInfo);
		when(mockRdbmsServiceInfo.getUrl()).thenReturn("jdbc:mysql://10.20.30.40:3306/database-123");
		when(mockRdbmsServiceInfo.getUserName()).thenReturn("myuser");
		when(mockRdbmsServiceInfo.getPassword()).thenReturn("mypass");
		
		rdbmsFactory.setServiceName("mysql-1");
		rdbmsFactory.afterPropertiesSet();
		
		DataSource dataSource = rdbmsFactory.getObject();
		assertNotNull(dataSource);
		
		assertEquals("jdbc:mysql://10.20.30.40:3306/database-123", ReflectionTestUtils.getField(dataSource, "url"));
		assertEquals("myuser", ReflectionTestUtils.invokeGetterMethod(dataSource, "username"));
		assertEquals("mypass", ReflectionTestUtils.invokeGetterMethod(dataSource, "password"));
		assertTrue((Boolean) ReflectionTestUtils.invokeGetterMethod(dataSource, "testOnBorrow"));
		assertTrue(((String) ReflectionTestUtils.invokeGetterMethod(dataSource, "validationQuery")).startsWith("/* ping */"));
	}
	
	@Test
	public void cloudPostgreSQLDataSourceCreation() throws Exception {
		when(mockRuntime.getServiceInfo("postgres-1", RdbmsServiceInfo.class))
			.thenReturn(mockRdbmsServiceInfo);
		when(mockRdbmsServiceInfo.getUrl()).thenReturn("jdbc:postgresql://10.20.30.40:5432/database-123");
		when(mockRdbmsServiceInfo.getUserName()).thenReturn("pguser");
		when(mockRdbmsServiceInfo.getPassword()).thenReturn("pgpass");
	
		rdbmsFactory.setServiceName("postgres-1");
		rdbmsFactory.afterPropertiesSet();
		
		DataSource dataSource = rdbmsFactory.getObject();
		assertNotNull(dataSource);
		
		assertEquals("jdbc:postgresql://10.20.30.40:5432/database-123", ReflectionTestUtils.getField(dataSource, "url"));
		assertEquals("pguser", ReflectionTestUtils.invokeGetterMethod(dataSource, "username"));
		assertEquals("pgpass", ReflectionTestUtils.invokeGetterMethod(dataSource, "password"));
		assertNotNull(((String) ReflectionTestUtils.invokeGetterMethod(dataSource, "validationQuery")));
	}
}
