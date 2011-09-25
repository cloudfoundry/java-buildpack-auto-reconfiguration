package org.cloudfoundry.runtime.service.relational;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.PostgresqlServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class PostgresqlServiceCreatorTest {
	@Mock private PostgresqlServiceInfo mockPostgresqlServiceInfo;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void cloudPostgreSQLDataSourceCreation() throws Exception {
		when(mockPostgresqlServiceInfo.getUrl()).thenReturn("jdbc:postgresql://10.20.30.40:5432/database-123");
		when(mockPostgresqlServiceInfo.getUserName()).thenReturn("pguser");
		when(mockPostgresqlServiceInfo.getPassword()).thenReturn("pgpass");

		PostgresqlServiceCreator postgresqlCreationHelper = new PostgresqlServiceCreator();
		DataSource dataSource = postgresqlCreationHelper.createSingletonService(mockPostgresqlServiceInfo).service;

		assertNotNull(dataSource);

		assertEquals("org.postgresql.Driver", ReflectionTestUtils.getField(dataSource, "driverClassName"));
		assertEquals("jdbc:postgresql://10.20.30.40:5432/database-123", ReflectionTestUtils.getField(dataSource, "url"));
		assertEquals("pguser", ReflectionTestUtils.invokeGetterMethod(dataSource, "username"));
		assertEquals("pgpass", ReflectionTestUtils.invokeGetterMethod(dataSource, "password"));
		assertNotNull(ReflectionTestUtils.invokeGetterMethod(dataSource, "validationQuery"));
		assertTrue(((String) ReflectionTestUtils.invokeGetterMethod(dataSource, "validationQuery")).startsWith("SELECT 1"));
	}
}
