package org.cloudfoundry.runtime.service.config.xml;

import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper;
import org.cloudfoundry.runtime.service.document.CloudMongoDbFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CloudMongoDbFactoryParserTest {

	private CloudMongoDbFactoryParser cloudParser;

	private GenericApplicationContext appContext;

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
		CloudEnvironment env = (CloudEnvironment) ReflectionTestUtils.getField(cloudParser, "cloudEnvironment");
		ReflectionTestUtils.setField(env, "environment", new MapEnvironmentAccessor(envMap));
		loadContext("CloudMongoDbFactoryParserTest-context.xml", CloudMongoDbFactoryParserTest.class,
				appContext, cloudParser);

		Object bean1 = this.appContext.getBean("mongoDbFactory1");
		assertEquals("not the correct class", "org.springframework.data.mongodb.core.SimpleMongoDbFactory", bean1.getClass().getName());
		assertEquals("not the correct databaseName", "468de494984f94e85255ce5d5", ReflectionTestUtils.getField(bean1, "databaseName"));
		assertEquals("not the correct username", "uIKpwGOTgjfaQ", ReflectionTestUtils.getField(bean1, "username"));
		assertEquals("not the correct password", "pu0xEgiy997kQ", ReflectionTestUtils.getField(bean1, "password"));
		Mongo mongo1 = (Mongo) ReflectionTestUtils.getField(bean1, "mongo");
		assertEquals("not the correct host", "127.0.0.1", mongo1.getAddress().getHost());
		assertEquals("not the correct port", 27017, mongo1.getAddress().getPort());
		MongoOptions options1 = mongo1.getMongoOptions();
		assertEquals("not the correct auto-connect-retry", true, options1.autoConnectRetry);
		assertEquals("not the correct max-auto-connect-retry-time", 12000, options1.maxAutoConnectRetryTime);
		assertEquals("not the correct socket-keep-alive", true, options1.socketKeepAlive);
		assertEquals("not the correct socket-timeout ", 10000, options1.socketTimeout);
		assertEquals("not the correct write-fsync", true, options1.fsync);
		assertEquals("not the correct connections-per-host", 20, options1.connectionsPerHost);
		assertEquals("not the correct threads-allowed-to-block-for-connection-multiplier", 8, options1.threadsAllowedToBlockForConnectionMultiplier);
		assertEquals("not the correct connect-timeout", 11000, options1.connectTimeout);
		assertEquals("not the correct max-wait-time", 110000, options1.maxWaitTime);
		Object bean2 = this.appContext.getBean("mongoDbFactory2");
		Mongo mongo2 = (Mongo) ReflectionTestUtils.getField(bean2, "mongo");
		MongoOptions options2 = mongo2.getMongoOptions();
		assertEquals("not the correct auto-connect-retry", false, options2.autoConnectRetry);
		assertEquals("not the correct max-auto-connect-retry-time", 0, options2.maxAutoConnectRetryTime);
		assertEquals("not the correct socket-keep-alive", false, options2.socketKeepAlive);
		assertEquals("not the correct socket-timeout ", 0, options2.socketTimeout);
		assertEquals("not the correct write-fsync", false, options2.fsync);
		assertEquals("not the correct connections-per-host", 10, options2.connectionsPerHost);
		assertEquals("not the correct threads-allowed-to-block-for-connection-multiplier", 5, options2.threadsAllowedToBlockForConnectionMultiplier);
		assertEquals("not the correct connect-timeout", 0, options2.connectTimeout);
		assertEquals("not the correct max-wait-time", 120000, options2.maxWaitTime);
	}

	private void loadContext(String fileName, Class relativeLocation, GenericApplicationContext context,
					final BeanDefinitionParser parser) {
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
		reader.setNamespaceHandlerResolver(new NamespaceHandlerResolver() {
			@Override
			public NamespaceHandler resolve(String namespaceUri) {
				return new NamespaceHandlerSupport() {
					@Override
					public BeanDefinition parse(Element element, ParserContext parserContext) {
						return parser.parse(element, parserContext);
					}
					@Override
					public void init() {
						registerBeanDefinitionParser("mongo-db-factory", parser);
					}
				};
			}
		});
		ClassPathResource resource = new ClassPathResource(fileName, relativeLocation);
		reader.loadBeanDefinitions(resource);
	}

	public static class MapEnvironmentAccessor extends CloudEnvironment.EnvironmentAccessor {

		Map<String, String> env;

		public MapEnvironmentAccessor(Map<String, String> env) {
			this.env = env;
		}

		public String getValue(String key) {
			return env.get(key);
		}
	}
}
