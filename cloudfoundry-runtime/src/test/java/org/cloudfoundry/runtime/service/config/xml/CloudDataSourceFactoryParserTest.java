package org.cloudfoundry.runtime.service.config.xml;

import static org.junit.Assert.assertEquals;

import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper;
import org.cloudfoundry.runtime.service.relational.CloudDataSourceFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CloudDataSourceFactoryParserTest extends AbstractCloudParserTest {

	@Before
	public void setUp() {
		appContext = new GenericApplicationContext();
		cloudParser = new CloudDataSourceFactoryParser(CloudDataSourceFactory.class, RdbmsServiceInfo.class);
	}

	@Test
	public void testMySqlDataSourceDefinition() throws Exception {
		Map<String, String> envMap = new HashMap<String, String>();
		envMap.put("VCAP_SERVICES", CloudEnvironmentTestHelper.getServicesPayload(
				new String[] {CloudEnvironmentTestHelper.getMysqlServicePayload("5.1", "mysql", "127.0.0.1", 3306,
						"uIKpwGOTgjfaQ", "pu0xEgiy997kQ", "d855c4a2468de494984f94e85255ce5d5")},
				new String[] {},
				new String[] {},
				new String[] {}));
		envMap.put("VCAP_APPLICATION", CloudEnvironmentTestHelper.getApplicationInstanceInfo("test", "test.vcap.me"));
		replaceCloudEnvironmentAccessor(envMap);
		loadContext("CloudDataSourceFactoryParserTest-context.xml", CloudDataSourceFactoryParserTest.class,
				appContext, cloudParser, "data-source");

		Object beanWithPoolAndConnProps = this.appContext.getBean("dataSourceWithPoolAndConnProps");
		assertEquals("not the correct class", "org.apache.commons.dbcp.BasicDataSource", beanWithPoolAndConnProps.getClass().getName());
		BeanWrapper dataSourceWithPoolAndConnProps = new BeanWrapperImpl(beanWithPoolAndConnProps);
		assertEquals("not the correct driverClassName", "com.mysql.jdbc.Driver",
				dataSourceWithPoolAndConnProps.getPropertyValue("driverClassName"));
		assertEquals("not the correct URL", "jdbc:mysql://127.0.0.1:3306/d855c4a2468de494984f94e85255ce5d5",
				dataSourceWithPoolAndConnProps.getPropertyValue("url"));
		assertEquals("not the correct username", "uIKpwGOTgjfaQ", dataSourceWithPoolAndConnProps.getPropertyValue("username"));
		assertEquals("not the correct password", "pu0xEgiy997kQ", dataSourceWithPoolAndConnProps.getPropertyValue("password"));
		assertEquals("not the correct validation-query", "/* ping */ SELECT 1", dataSourceWithPoolAndConnProps.getPropertyValue("validationQuery"));
		assertEquals("not the correct test-on-borrow", true, dataSourceWithPoolAndConnProps.getPropertyValue("testOnBorrow"));
		assertEquals("not the correct upper limit for pool-size", 20, dataSourceWithPoolAndConnProps.getPropertyValue("maxActive"));
		assertEquals("not the correct lower limit for pool-size", 5, dataSourceWithPoolAndConnProps.getPropertyValue("initialSize"));
		assertEquals("not the correct max-wait", 5000L, dataSourceWithPoolAndConnProps.getPropertyValue("maxWait"));
		Properties theConnectionProperties = (Properties) ReflectionTestUtils.getField(beanWithPoolAndConnProps, "connectionProperties");
		assertEquals("not the correct sessionVariables connection property", "sql_mode='ANSI'", theConnectionProperties.get("sessionVariables"));
		assertEquals("not the correct characterEncoding connection property", "'UTF8'", theConnectionProperties.get("characterEncoding"));

		Object beanWithDefaults = this.appContext.getBean("dataSourceWithDefaults");
		assertEquals("not the correct class", "org.apache.commons.dbcp.BasicDataSource", beanWithDefaults.getClass().getName());
		BeanWrapper dataSourceWithDefaults = new BeanWrapperImpl(beanWithDefaults);
		assertEquals("not the correct maxActive", 8, dataSourceWithDefaults.getPropertyValue("maxActive"));
		assertEquals("not the correct initialSize", 0, dataSourceWithDefaults.getPropertyValue("initialSize"));
		assertEquals("not the correct maxWait", -1L, dataSourceWithDefaults.getPropertyValue("maxWait"));
		Properties emptyConnectionProperties = (Properties) ReflectionTestUtils.getField(beanWithDefaults, "connectionProperties");
		assertEquals("not empty connection properties", 0, emptyConnectionProperties.size());
	}
}
