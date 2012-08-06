package org.cloudfoundry.runtime.service.config.xml;

import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper;
import org.cloudfoundry.runtime.service.keyvalue.CloudRedisConnectionFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.support.GenericApplicationContext;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CloudRedisConnectionFactoryParserTest extends AbstractCloudParserTest {

	@Before
	public void setUp() {
		appContext = new GenericApplicationContext();
		cloudParser = new CloudRedisConnectionFactoryParser(CloudRedisConnectionFactoryBean.class, RedisServiceInfo.class);
	}

	@Test
	public void testRedisConnectionFactoryDefinition() throws Exception {
		Map<String, String> envMap = new HashMap<String, String>();
		envMap.put("VCAP_SERVICES", CloudEnvironmentTestHelper.getServicesPayload(
				new String[]{},
				new String[]{CloudEnvironmentTestHelper.getRedisServicePayload("2.2", "redis-test", "127.0.0.1", 6379,
						"pu0xEgiy997kQ", "d855c4a2468de494984f94e85255ce5d5")},
				new String[]{},
				new String[]{}));
		envMap.put("VCAP_APPLICATION", CloudEnvironmentTestHelper.getApplicationInstanceInfo("test", "test.vcap.me"));
		replaceCloudEnvironmentAccessor(envMap);
		loadContext("CloudRedisConnectionFactoryParserTest-context.xml", CloudRedisConnectionFactoryParserTest.class,
				appContext, cloudParser, "redis-connection-factory");

		Object beanWithPool = this.appContext.getBean("redisConnectionFactoryWithPool");
		assertEquals("not the correct class", "org.springframework.data.redis.connection.jedis.JedisConnectionFactory", beanWithPool.getClass().getName());
		BeanWrapper connectionFactoryWithPool = new BeanWrapperImpl(beanWithPool);
		assertEquals("not the correct host", "127.0.0.1", connectionFactoryWithPool.getPropertyValue("hostName"));
		assertEquals("not the correct password", "pu0xEgiy997kQ", connectionFactoryWithPool.getPropertyValue("password"));
		assertEquals("not the correct upper limit for pool-size", 20, ((JedisPoolConfig)connectionFactoryWithPool.getPropertyValue("poolConfig")).getMaxActive());
		assertEquals("not the correct lower limit for pool-size", 5, ((JedisPoolConfig)connectionFactoryWithPool.getPropertyValue("poolConfig")).minIdle);
		assertEquals("not the correct max-wait", 5000L, ((JedisPoolConfig)connectionFactoryWithPool.getPropertyValue("poolConfig")).getMaxWait());

		Object beanWithDefaults = this.appContext.getBean("redisConnectionFactoryWithDefaults");
		assertEquals("not the correct class", "org.springframework.data.redis.connection.jedis.JedisConnectionFactory", beanWithDefaults.getClass().getName());
		BeanWrapper connectionFactoryWithDefaults = new BeanWrapperImpl(beanWithDefaults);
		assertEquals("not the correct maxActive", 8, ((JedisPoolConfig)connectionFactoryWithDefaults.getPropertyValue("poolConfig")).getMaxActive());
		assertEquals("not the correct minIdle", 0, ((JedisPoolConfig)connectionFactoryWithDefaults.getPropertyValue("poolConfig")).minIdle);
		assertEquals("not the correct maxWait", -1L, ((JedisPoolConfig)connectionFactoryWithDefaults.getPropertyValue("poolConfig")).getMaxWait());
	}
}
