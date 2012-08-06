package org.cloudfoundry.runtime.service.config.xml;

import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper;
import org.cloudfoundry.runtime.service.messaging.CloudRabbitConnectionFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CloudRabbitConnectionFactoryParserTest extends AbstractCloudParserTest {

	@Before
	public void setUp() {
		appContext = new GenericApplicationContext();
		cloudParser = new CloudRabbitConnectionFactoryParser(CloudRabbitConnectionFactoryBean.class, RabbitServiceInfo.class);
	}

	@Test
	public void testRabbitConnectionFactoryDefinition() throws Exception {
		Map<String, String> envMap = new HashMap<String, String>();
		envMap.put("VCAP_SERVICES", CloudEnvironmentTestHelper.getServicesPayload(
				new String[]{},
				new String[]{},
				new String[]{},
				new String[]{CloudEnvironmentTestHelper.getRabbitServicePayload("2.4", "rabbit-test", "127.0.0.1", 5672,
						"uIKpwGOTgjfaQ", "pu0xEgiy997kQ", "d855c4a2468de494984f94e85255ce5d5", "/")}));
		envMap.put("VCAP_APPLICATION", CloudEnvironmentTestHelper.getApplicationInstanceInfo("test", "test.vcap.me"));
		replaceCloudEnvironmentAccessor(envMap);
		loadContext("CloudRabbitConnectionFactoryParserTest-context.xml", CloudRabbitConnectionFactoryParserTest.class,
				appContext, cloudParser, "rabbit-connection-factory");

		Object beanWithCache = this.appContext.getBean("rabbitConnectionFactoryWithCache");
		assertEquals("not the correct class", "org.springframework.amqp.rabbit.connection.CachingConnectionFactory", beanWithCache.getClass().getName());
		BeanWrapper connectionFactoryWithCache = new BeanWrapperImpl(beanWithCache);
		assertEquals("not the correct host", "127.0.0.1", connectionFactoryWithCache.getPropertyValue("host"));
		assertEquals("not the correct port", 5672, connectionFactoryWithCache.getPropertyValue("port"));
		BeanWrapper rabbitConnectionFactoryWithCache = new BeanWrapperImpl(ReflectionTestUtils.getField(beanWithCache, "rabbitConnectionFactory"));
		assertEquals("not the correct username", "uIKpwGOTgjfaQ", rabbitConnectionFactoryWithCache.getPropertyValue("username"));
		assertEquals("not the correct password", "pu0xEgiy997kQ", rabbitConnectionFactoryWithCache.getPropertyValue("password"));
		assertEquals("not the correct vHost", "/", rabbitConnectionFactoryWithCache.getPropertyValue("virtualHost"));
		assertEquals("not the correct channel-cache-size", 10, connectionFactoryWithCache.getPropertyValue("channelCacheSize"));

		Object beanWithDefaults = this.appContext.getBean("rabbitConnectionFactoryWithDefaults");
		assertEquals("not the correct class", "org.springframework.amqp.rabbit.connection.CachingConnectionFactory", beanWithDefaults.getClass().getName());
		BeanWrapper connectionFactoryWithDefaults = new BeanWrapperImpl(beanWithDefaults);
		assertEquals("not the correct channel-cache-size", 1, connectionFactoryWithDefaults.getPropertyValue("channelCacheSize"));
	}
}
