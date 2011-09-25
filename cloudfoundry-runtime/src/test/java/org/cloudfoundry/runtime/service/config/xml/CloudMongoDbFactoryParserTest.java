package org.cloudfoundry.runtime.service.config.xml;

import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper;
import org.cloudfoundry.runtime.service.document.CloudMongoDbFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CloudMongoDbFactoryParserTest extends AbstractCloudParserTest {

	@Before
	public void setUp() {
		appContext = new GenericApplicationContext();
		cloudParser = new CloudMongoDbFactoryParser(CloudMongoDbFactoryBean.class, MongoServiceInfo.class);
	}

	@Test
	public void testMySqlDataSourceDefinition() throws Exception {
		Map<String, String> envMap = new HashMap<String, String>();
		envMap.put("VCAP_SERVICES", CloudEnvironmentTestHelper.getServicesPayload(
				new String[] {},
				new String[] {},
				new String[] {CloudEnvironmentTestHelper.getMongoServicePayload("mongodb", "127.0.0.1", 27017,
						"uIKpwGOTgjfaQ", "pu0xEgiy997kQ", "468de494984f94e85255ce5d5", "468de494984f94e85255ce5d5")},
				new String[] {}));
		envMap.put("VCAP_APPLICATION", CloudEnvironmentTestHelper.getApplicationInstanceInfo("test", "test.vcap.me"));
		replaceCloudEnvironmentAccessor(envMap);
		loadContext("CloudMongoDbFactoryParserTest-context.xml", CloudMongoDbFactoryParserTest.class,
				appContext, cloudParser, "mongo-db-factory");

		Object mongoDbFactoryWithOptions = this.appContext.getBean("mongoDbFactoryWithOptions");
		assertEquals("not the correct class", "org.springframework.data.mongodb.core.SimpleMongoDbFactory", mongoDbFactoryWithOptions.getClass().getName());
		assertEquals("not the correct databaseName", "468de494984f94e85255ce5d5", ReflectionTestUtils.getField(mongoDbFactoryWithOptions, "databaseName"));
		assertEquals("not the correct username", "uIKpwGOTgjfaQ", ReflectionTestUtils.getField(mongoDbFactoryWithOptions, "username"));
		assertEquals("not the correct password", "pu0xEgiy997kQ", ReflectionTestUtils.getField(mongoDbFactoryWithOptions, "password"));
		Mongo mongoWithOptions = (Mongo) ReflectionTestUtils.getField(mongoDbFactoryWithOptions, "mongo");
		assertEquals("not the correct host", "127.0.0.1", mongoWithOptions.getAddress().getHost());
		assertEquals("not the correct port", 27017, mongoWithOptions.getAddress().getPort());
		MongoOptions theOptions = mongoWithOptions.getMongoOptions();
		assertEquals("not the correct write-fsync", true, theOptions.fsync);
		assertEquals("not the correct connections-per-host", 20, theOptions.connectionsPerHost);
		assertEquals("not the correct max-wait-time", 110000, theOptions.maxWaitTime);

		Object mongoDbFactoryWithDefaults = this.appContext.getBean("mongoDbFactoryWithDefaults");
		Mongo mongoWithDefaults = (Mongo) ReflectionTestUtils.getField(mongoDbFactoryWithDefaults, "mongo");
		MongoOptions defaultOptions = mongoWithDefaults.getMongoOptions();
		assertEquals("not the correct write-fsync", false, defaultOptions.fsync);
		assertEquals("not the correct connections-per-host", 10, defaultOptions.connectionsPerHost);
		assertEquals("not the correct max-wait-time", 120000, defaultOptions.maxWaitTime);
	}
}
