package org.cloudfoundry.reconfiguration.data.keyvalue;

import static org.junit.Assert.assertFalse;

import org.apache.commons.lang3.tuple.Pair;
import org.cloudfoundry.reconfiguration.AbstractCloudConfigurerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.common.RedisServiceInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

/**
 * Test of {@link RedisConfigurer}
 * <p>
 * Integration tests bootstrap application contexts containing various
 * {@link RedisConnectionFactory} beans and check if the actual beans got
 * replaced/not replaced with the mock beans.
 * <p>
 * Unit test verifies auto-reconfig behavior when the
 * {@link RedisConnectionFactory} class is not found on the classpath
 *
 *
 * @author Jennifer Hickey
 * @author Ramnivas Laddad
 *
 */
public class RedisConfigurerTest extends AbstractCloudConfigurerTest {
	@Test
	public void cloudRedisConnFactoryReplacesUserConnFactory() {
        RedisServiceInfo redisServiceInfo = new RedisServiceInfo("my-redis", "cloudhost", 1234, "mypass");
		ApplicationContext context = getTestApplicationContext("test-redis-context.xml", redisServiceInfo);
		JedisConnectionFactory replacedConnFactory = 
		        (JedisConnectionFactory) context.getBean("redisConnection",RedisConnectionFactory.class);

		Assert.assertEquals("cloudhost", replacedConnFactory.getHostName());
		Assert.assertEquals("mypass", replacedConnFactory.getPassword());
		Assert.assertEquals(1234, replacedConnFactory.getPort());
	}

	@Test
	public void cloudRedisConnFactoryLeavesOriginalInPlaceIfMultipleBeansDetected() {
        RedisServiceInfo redisServiceInfo = new RedisServiceInfo("my-redis", "cloudhost", 1234, "mypass");

		ApplicationContext context = getTestApplicationContext("test-multiple-redis-context.xml", redisServiceInfo);
		
		JedisConnectionFactory replacedConnFactory = (JedisConnectionFactory) context.getBean("redisConnection",
				RedisConnectionFactory.class);
		Assert.assertEquals("localhost", replacedConnFactory.getHostName());
		Assert.assertEquals("localpass", replacedConnFactory.getPassword());
		Assert.assertEquals(6789, replacedConnFactory.getPort());
	}

	@Test
	public void cloudRedisConnFactoryLeavesOriginalInPlaceIfMultipleServicesDetected() {
        RedisServiceInfo redisServiceInfo1 = new RedisServiceInfo("my-redis1", "cloudhost", 1234, "mypass");
        RedisServiceInfo redisServiceInfo2 = new RedisServiceInfo("my-redis2", "cloudhost", 1234, "mypass");

        ApplicationContext context = getTestApplicationContext("test-redis-context.xml", redisServiceInfo1, redisServiceInfo2);
		JedisConnectionFactory replacedConnFactory = 
		        (JedisConnectionFactory) context.getBean("redisConnection", RedisConnectionFactory.class);
		Assert.assertEquals("localhost", replacedConnFactory.getHostName());
		Assert.assertEquals("localpass", replacedConnFactory.getPassword());
		Assert.assertEquals(6789, replacedConnFactory.getPort());
	}

	@Test
	public void cloudRedisConnFactoryLeavesOriginalInPlaceIfNoServicesDetected() {
		ApplicationContext context = getTestApplicationContext("test-redis-context.xml");
		JedisConnectionFactory replacedConnFactory = 
		        (JedisConnectionFactory) context.getBean("redisConnection", RedisConnectionFactory.class);
		Assert.assertEquals("localhost", replacedConnFactory.getHostName());
		Assert.assertEquals("localpass", replacedConnFactory.getPassword());
		Assert.assertEquals(6789, replacedConnFactory.getPort());
	}

	@Test
	public void doesNothingIfRedisConnectionFactoryClassNotFound() {
	    // Set up such that conditions match auto-reconfig requirements (except for what subclasses configuer does)
        RedisServiceInfo redisServiceInfo = new RedisServiceInfo("my-redis", "cloudhost", 1234, "mypass");
        Pair<Cloud, DefaultListableBeanFactory> cloudAndBeanFactory = getCloudAndBeanFactory("test-redis-context.xml", redisServiceInfo);
	    
		RedisConfigurer configurer = new RedisConnectionFactoryNotFoundRedisConfigurer(cloudAndBeanFactory.getLeft());
		assertFalse(configurer.configure(cloudAndBeanFactory.getRight()));
	}

	private class RedisConnectionFactoryNotFoundRedisConfigurer extends RedisConfigurer {
		public RedisConnectionFactoryNotFoundRedisConfigurer(Cloud cloud) {
			super(cloud);
		}

		@Override
		protected Class<?> loadClass(String name) {
			return null;
		}
	}
}
