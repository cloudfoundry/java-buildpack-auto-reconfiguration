package org.cloudfoundry.runtime.service.config.xml;

import static org.junit.Assert.assertEquals;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.service.relational.CloudDataSourceFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

public class CloudDataSourceFactoryParserTest {

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
		envMap.put("VCAP_SERVICES", "{\"mysql-5.1\":[{\"name\":\"mysql-1164\",\"label\":\"mysql-5.1\",\"plan\":\"free\",\"tags\":[\"mysql\",\"mysql-5.1\",\"relational\"],\"credentials\":{\"name\":\"d855c4a2468de494984f94e85255ce5d5\",\"hostname\":\"127.0.0.1\",\"host\":\"127.0.0.1\",\"port\":3306,\"user\":\"uIKpwGOTgjfaQ\",\"username\":\"uIKpwGOTgjfaQ\",\"password\":\"pu0xEgiy997kQ\"}}]}");
		envMap.put("VCAP_APPLICATION", "{\"instance_id\":\"6d500776f5c28c8b024b0fe0ed658ce9\",\"instance_index\":0,\"name\":\"test\",\"uris\":[\"test.vcap.me\"],\"users\":[\"trisberg@vmware.com\"],\"version\":\"b7e8d1011b2545f38571c0486a95c8160b4f8f1f-1\",\"start\":\"2011-09-14 20:49:42 -0700\",\"runtime\":\"java\",\"state_timestamp\":1316058582,\"port\":40776,\"limits\":{\"fds\":256,\"mem\":536870912,\"disk\":2147483648},\"host\":\"127.0.0.1\"}");
		CloudEnvironment env = (CloudEnvironment) ReflectionTestUtils.getField(cloudParser, "cloudEnvironment");
		ReflectionTestUtils.setField(env, "environment", new MapEnvironmentAccessor(envMap));
		loadContext("CloudDataSourceFactoryParserTest-context.xml", CloudDataSourceFactoryParserTest.class, appContext, cloudParser);

		Object bean = this.appContext.getBean("dataSource");
		assertEquals("not the correct class", "org.apache.commons.dbcp.BasicDataSource", bean.getClass().getName());
		BeanWrapper dataSource = new BeanWrapperImpl(bean);
		assertEquals("not the correct driverClassName", "com.mysql.jdbc.Driver", dataSource.getPropertyValue("driverClassName"));
		assertEquals("not the correct URL", "jdbc:mysql://127.0.0.1:3306/d855c4a2468de494984f94e85255ce5d5", dataSource.getPropertyValue("url"));
		assertEquals("not the correct username", "uIKpwGOTgjfaQ", dataSource.getPropertyValue("username"));
		assertEquals("not the correct password", "pu0xEgiy997kQ", dataSource.getPropertyValue("password"));
		assertEquals("not the correct validation-query", "SELECT 0", dataSource.getPropertyValue("validationQuery"));
		assertEquals("not the correct validation-query-timeout", 2, dataSource.getPropertyValue("validationQueryTimeout"));
		assertEquals("not the correct test-on-borrow", true, dataSource.getPropertyValue("testOnBorrow"));
		assertEquals("not the correct test-on-return", true, dataSource.getPropertyValue("testOnReturn"));
		assertEquals("not the correct test-while-idle", true, dataSource.getPropertyValue("testWhileIdle"));
		assertEquals("not the correct max-active", 100, dataSource.getPropertyValue("maxActive"));
		assertEquals("not the correct max-idle", 20, dataSource.getPropertyValue("maxIdle"));
		assertEquals("not the correct min-idle", 5, dataSource.getPropertyValue("minIdle"));
		assertEquals("not the correct max-wait", 5000L, dataSource.getPropertyValue("maxWait"));
		assertEquals("not the correct pool-prepared-statements", true, dataSource.getPropertyValue("poolPreparedStatements"));
		assertEquals("not the correct max-open-prepared-statements", 25, dataSource.getPropertyValue("maxOpenPreparedStatements"));
	}

	private void loadContext(String fileName, Class relativeLocation, GenericApplicationContext context, final BeanDefinitionParser parser) {
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
						registerBeanDefinitionParser("data-source", parser);
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
