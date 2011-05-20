package org.cloudfoundry.runtime.service.relational;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class CloudDataSourceFactoryTest {
	@Mock private CloudEnvironment mockRuntime;
	@Mock private MysqlServiceInfo mockServiceInfo;
	private CloudDataSourceFactory factory;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		factory = new CloudDataSourceFactory(mockRuntime);
	}
	
	@Test
	public void cloudDataSourceCreation() throws Exception {
		when(mockRuntime.getServiceInfo("mysql-1", MysqlServiceInfo.class))
			.thenReturn(mockServiceInfo);
		when(mockServiceInfo.getUrl()).thenReturn("jdbc:mysql://10.20.30.40:3306/database-123");
		when(mockServiceInfo.getUserName()).thenReturn("myuser");
		when(mockServiceInfo.getPassword()).thenReturn("mypass");
		
		factory.setServiceName("mysql-1");
		factory.afterPropertiesSet();
		
		DataSource dataSource = factory.getObject();
		assertNotNull(dataSource);
		
		assertEquals("jdbc:mysql://10.20.30.40:3306/database-123", ReflectionTestUtils.getField(dataSource, "url"));
		assertEquals("myuser", ReflectionTestUtils.invokeGetterMethod(dataSource, "username"));
		assertEquals("mypass", ReflectionTestUtils.invokeGetterMethod(dataSource, "password"));
		assertTrue((Boolean) ReflectionTestUtils.invokeGetterMethod(dataSource, "testOnBorrow"));
		assertTrue(((String) ReflectionTestUtils.invokeGetterMethod(dataSource, "validationQuery")).startsWith("/* ping */"));
	}
}
