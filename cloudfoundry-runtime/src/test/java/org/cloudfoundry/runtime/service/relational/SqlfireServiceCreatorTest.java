package org.cloudfoundry.runtime.service.relational;

import java.util.Map;
import java.util.HashMap;
import org.cloudfoundry.runtime.env.SqlfireServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;


import static org.junit.Assert.*;

public class SqlfireServiceCreatorTest {

    private Map<String, Object> creds = new HashMap<String, Object>() {{
        put("name", "database-123");
        put("hostname", "10.20.30.40");
        put("port", 5432);
        put("locator_port", 5433);
        put("user", "sqlfuser");
        put("password", "sqlfpass");
    }};

    SqlfireServiceInfo mockSqlfireServiceInfo =
            new SqlfireServiceInfo(new HashMap() {{put("credentials", creds);}});

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void cloudSqlfireDataSourceCreation() throws Exception {

		SqlfireServiceCreator sqlfireCreationHelper = new SqlfireServiceCreator();
		DataSource dataSource = sqlfireCreationHelper.createSingletonService(mockSqlfireServiceInfo).service;

		assertNotNull(dataSource);

		assertEquals("com.vmware.sqlfire.jdbc.ClientDriver", ReflectionTestUtils.getField(dataSource, "driverClassName"));
		assertEquals("jdbc:sqlfire://10.20.30.40:5432/;user=sqlfuser;password=sqlfpass", ReflectionTestUtils.getField(dataSource, "url"));
		assertEquals("sqlfuser", ReflectionTestUtils.invokeGetterMethod(dataSource, "username"));
		assertEquals("sqlfpass", ReflectionTestUtils.invokeGetterMethod(dataSource, "password"));
		assertNotNull(ReflectionTestUtils.invokeGetterMethod(dataSource, "validationQuery"));
		assertTrue(((String) ReflectionTestUtils.invokeGetterMethod(dataSource, "validationQuery")).startsWith("SELECT current_timestamp"));
	}
}
