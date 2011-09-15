package org.cloudfoundry.runtime.service.relational;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class MysqlServiceCreatorTest {
	@Mock private CloudEnvironment mockRuntime;
	@Mock private MysqlServiceInfo mockMysqlServiceInfo;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void cloudMySQLDataSourceCreation() throws Exception {
		when(mockRuntime.getServiceInfos(MysqlServiceInfo.class))
			.thenReturn(Collections.singletonList(mockMysqlServiceInfo));
		when(mockMysqlServiceInfo.getUrl()).thenReturn("jdbc:mysql://10.20.30.40:3306/database-123");
		when(mockMysqlServiceInfo.getUserName()).thenReturn("myuser");
		when(mockMysqlServiceInfo.getPassword()).thenReturn("mypass");

		MysqlServiceCreator mysqlCreationHelper = new MysqlServiceCreator(mockRuntime);
		DataSource dataSource = mysqlCreationHelper.createSingletonService().service;

		assertNotNull(dataSource);

		assertEquals(MysqlServiceCreator.MYSQL_DRIVER_CLASS_NAME, ReflectionTestUtils.getField(dataSource, "driverClassName"));
		assertEquals("jdbc:mysql://10.20.30.40:3306/database-123", ReflectionTestUtils.getField(dataSource, "url"));
		assertEquals("myuser", ReflectionTestUtils.invokeGetterMethod(dataSource, "username"));
		assertEquals("mypass", ReflectionTestUtils.invokeGetterMethod(dataSource, "password"));
		assertTrue((Boolean) ReflectionTestUtils.invokeGetterMethod(dataSource, "testOnBorrow"));
		assertNotNull(ReflectionTestUtils.invokeGetterMethod(dataSource, "validationQuery"));
		assertTrue(((String) ReflectionTestUtils.invokeGetterMethod(dataSource, "validationQuery")).startsWith("/* ping */ SELECT 1"));
	}
}
