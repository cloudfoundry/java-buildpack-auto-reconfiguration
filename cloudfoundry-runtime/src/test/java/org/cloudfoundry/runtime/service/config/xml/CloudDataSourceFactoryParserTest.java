package org.cloudfoundry.runtime.service.config.xml;

import static org.junit.Assert.assertEquals;

import org.cloudfoundry.runtime.env.CloudEnvironment;
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

public class CloudDataSourceFactoryParserTest  extends AbstractCloudParserTest {

	private CloudDataSourceFactoryParser cloudParser;

	private GenericApplicationContext appContext;

	@Before
	public void setUp() {
		appContext = new GenericApplicationContext();
		cloudParser = new CloudDataSourceFactoryParser(CloudDataSourceFactory.class, RdbmsServiceInfo.class);
	}

	@Test
	public void testMySqlDataSourceDefinition() throws Exception {
		Map<String, String> envMap = new HashMap<String, String>();
		envMap.put("VCAP_SERVICES", CloudEnvironmentTestHelper.getServicesPayload(
				new String[] {CloudEnvironmentTestHelper.getMysqlServicePayload("mysql", "127.0.0.1", 3306,
						"uIKpwGOTgjfaQ", "pu0xEgiy997kQ", "d855c4a2468de494984f94e85255ce5d5")},
				new String[] {},
				new String[] {},
				new String[] {}));
		envMap.put("VCAP_APPLICATION", CloudEnvironmentTestHelper.getApplicationInstanceInfo("test", "test.vcap.me"));
		CloudEnvironment env = (CloudEnvironment) ReflectionTestUtils.getField(cloudParser, "cloudEnvironment");
		ReflectionTestUtils.setField(env, "environment", new MapEnvironmentAccessor(envMap));
		loadContext("CloudDataSourceFactoryParserTest-context.xml", CloudDataSourceFactoryParserTest.class,
				appContext, cloudParser);

		Object bean = this.appContext.getBean("dataSource");
		assertEquals("not the correct class", "org.apache.commons.dbcp.BasicDataSource", bean.getClass().getName());
		BeanWrapper dataSource = new BeanWrapperImpl(bean);
		assertEquals("not the correct driverClassName", "com.mysql.jdbc.Driver",
				dataSource.getPropertyValue("driverClassName"));
		assertEquals("not the correct URL", "jdbc:mysql://127.0.0.1:3306/d855c4a2468de494984f94e85255ce5d5",
				dataSource.getPropertyValue("url"));
		assertEquals("not the correct username", "uIKpwGOTgjfaQ", dataSource.getPropertyValue("username"));
		assertEquals("not the correct password", "pu0xEgiy997kQ", dataSource.getPropertyValue("password"));
		assertEquals("not the correct validation-query", "SELECT 0", dataSource.getPropertyValue("validationQuery"));
		assertEquals("not the correct validation-query-timeout", 2,
				dataSource.getPropertyValue("validationQueryTimeout"));
		assertEquals("not the correct test-on-borrow", true, dataSource.getPropertyValue("testOnBorrow"));
		assertEquals("not the correct test-on-return", true, dataSource.getPropertyValue("testOnReturn"));
		assertEquals("not the correct test-while-idle", true, dataSource.getPropertyValue("testWhileIdle"));
		assertEquals("not the correct max-active", 100, dataSource.getPropertyValue("maxActive"));
		assertEquals("not the correct max-idle", 20, dataSource.getPropertyValue("maxIdle"));
		assertEquals("not the correct min-idle", 5, dataSource.getPropertyValue("minIdle"));
		assertEquals("not the correct max-wait", 5000L, dataSource.getPropertyValue("maxWait"));
		assertEquals("not the correct pool-prepared-statements", true,
				dataSource.getPropertyValue("poolPreparedStatements"));
		assertEquals("not the correct max-open-prepared-statements", 25,
				dataSource.getPropertyValue("maxOpenPreparedStatements"));
	}
}
