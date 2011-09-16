package org.cloudfoundry.runtime.service.config.xml;

import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import org.cloudfoundry.runtime.env.CloudEnvironment;
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
	public void testMongoDbFactoryDefinition() throws Exception {
		Map<String, String> envMap = new HashMap<String, String>();
		envMap.put("VCAP_SERVICES", CloudEnvironmentTestHelper.getServicesPayload(
				new String[] {},
				new String[] {},
				new String[] {CloudEnvironmentTestHelper.getMongoServicePayload("mongodb", "127.0.0.1", 27017,
						"uIKpwGOTgjfaQ", "pu0xEgiy997kQ", "468de494984f94e85255ce5d5", "468de494984f94e85255ce5d5")},
				new String[] {}));
		envMap.put("VCAP_APPLICATION", CloudEnvironmentTestHelper.getApplicationInstanceInfo("test", "test.vcap.me"));
		CloudEnvironment env = (CloudEnvironment) ReflectionTestUtils.getField(cloudParser, "cloudEnvironment");
		ReflectionTestUtils.setField(env, "environment", new MapEnvironmentAccessor(envMap));
		loadContext("CloudMongoDbFactoryParserTest-context.xml", CloudMongoDbFactoryParserTest.class,
				appContext, cloudParser);

		Object beanWithMongoOptions = this.appContext.getBean("mongoDbFactoryWithMongoOptions");
		assertEquals("not the correct class", "org.springframework.data.mongodb.core.SimpleMongoDbFactory", beanWithMongoOptions.getClass().getName());
		assertEquals("not the correct databaseName", "468de494984f94e85255ce5d5", ReflectionTestUtils.getField(beanWithMongoOptions, "databaseName"));
		assertEquals("not the correct username", "uIKpwGOTgjfaQ", ReflectionTestUtils.getField(beanWithMongoOptions, "username"));
		assertEquals("not the correct password", "pu0xEgiy997kQ", ReflectionTestUtils.getField(beanWithMongoOptions, "password"));
		Mongo mongoWithMongoOptions = (Mongo) ReflectionTestUtils.getField(beanWithMongoOptions, "mongo");
		assertEquals("not the correct host", "127.0.0.1", mongoWithMongoOptions.getAddress().getHost());
		assertEquals("not the correct port", 27017, mongoWithMongoOptions.getAddress().getPort());
		MongoOptions mongoOptions = mongoWithMongoOptions.getMongoOptions();
		assertEquals("not the correct auto-connect-retry", true, mongoOptions.autoConnectRetry);
		assertEquals("not the correct max-auto-connect-retry-time", 12000, mongoOptions.maxAutoConnectRetryTime);
		assertEquals("not the correct socket-keep-alive", true, mongoOptions.socketKeepAlive);
		assertEquals("not the correct socket-timeout ", 10000, mongoOptions.socketTimeout);
		assertEquals("not the correct write-fsync", true, mongoOptions.fsync);
		assertEquals("not the correct connections-per-host", 20, mongoOptions.connectionsPerHost);
		assertEquals("not the correct threads-allowed-to-block-for-connection-multiplier", 8, mongoOptions.threadsAllowedToBlockForConnectionMultiplier);
		assertEquals("not the correct connect-timeout", 11000, mongoOptions.connectTimeout);
		assertEquals("not the correct max-wait-time", 110000, mongoOptions.maxWaitTime);

		Object beanWithDefaults = this.appContext.getBean("mongoDbFactoryWithDefaults");
		Mongo mongoWithDefaults = (Mongo) ReflectionTestUtils.getField(beanWithDefaults, "mongo");
		MongoOptions defaultOptions = mongoWithDefaults.getMongoOptions();
		assertEquals("not the correct auto-connect-retry", false, defaultOptions.autoConnectRetry);
		assertEquals("not the correct max-auto-connect-retry-time", 0, defaultOptions.maxAutoConnectRetryTime);
		assertEquals("not the correct socket-keep-alive", false, defaultOptions.socketKeepAlive);
		assertEquals("not the correct socket-timeout ", 0, defaultOptions.socketTimeout);
		assertEquals("not the correct write-fsync", false, defaultOptions.fsync);
		assertEquals("not the correct connections-per-host", 10, defaultOptions.connectionsPerHost);
		assertEquals("not the correct threads-allowed-to-block-for-connection-multiplier", 5, defaultOptions.threadsAllowedToBlockForConnectionMultiplier);
		assertEquals("not the correct connect-timeout", 0, defaultOptions.connectTimeout);
		assertEquals("not the correct max-wait-time", 120000, defaultOptions.maxWaitTime);
	}

}
