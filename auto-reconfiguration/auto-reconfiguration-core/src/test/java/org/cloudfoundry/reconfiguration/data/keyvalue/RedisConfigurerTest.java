package org.cloudfoundry.reconfiguration.data.keyvalue;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.reconfiguration.CloudEnvironmentMockingTest;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.keyvalue.RedisServiceCreator;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
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
 * Unit test verifies auto-reconfig behavior when the {@link RedisConnectionFactory}
 * class is not found on the classpath
 *
 *
 * @author Jennifer Hickey
 *
 */
public class RedisConfigurerTest extends CloudEnvironmentMockingTest {
	@Mock
	private RedisServiceInfo mockRedisServiceInfo;
	@Mock
	private RedisServiceInfo mockRedisServiceInfo2;

	@Mock
	private DefaultListableBeanFactory beanFactory;

	@Mock
	private RedisServiceCreator serviceCreator;

	@Test
	public void cloudRedisConnFactoryReplacesUserConnFactory() {
		List<RedisServiceInfo> serviceInfos = new ArrayList<RedisServiceInfo>();
		serviceInfos.add(mockRedisServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "redis-2.2");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		when(mockRedisServiceInfo.getHost()).thenReturn("cloudhost");
		when(mockRedisServiceInfo.getPassword()).thenReturn("mypass");
		when(mockRedisServiceInfo.getPort()).thenReturn(1234);
		when(mockEnvironment.getServiceInfos(RedisServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-redis-context.xml");
		JedisConnectionFactory replacedConnFactory = (JedisConnectionFactory) context.getBean("redisConnection",
				RedisConnectionFactory.class);
		Assert.assertEquals("cloudhost", replacedConnFactory.getHostName());
		Assert.assertEquals("mypass", replacedConnFactory.getPassword());
		Assert.assertEquals(1234, replacedConnFactory.getPort());
	}

	@Test
	public void cloudRedisConnFactoryLeavesOriginalInPlaceIfMultipleBeansDetected() {
		List<RedisServiceInfo> serviceInfos = new ArrayList<RedisServiceInfo>();
		serviceInfos.add(mockRedisServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "redis-2.2");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		when(mockEnvironment.getServiceInfos(RedisServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-multiple-redis-context.xml");
		JedisConnectionFactory replacedConnFactory = (JedisConnectionFactory) context.getBean("redisConnection",
				RedisConnectionFactory.class);
		Assert.assertEquals("localhost", replacedConnFactory.getHostName());
		Assert.assertEquals("localpass", replacedConnFactory.getPassword());
		Assert.assertEquals(6789, replacedConnFactory.getPort());
	}

	@Test
	public void cloudRedisConnFactoryLeavesOriginalInPlaceIfMultipleServicesDetected() {
		List<RedisServiceInfo> serviceInfos = new ArrayList<RedisServiceInfo>();
		serviceInfos.add(mockRedisServiceInfo);
		serviceInfos.add(mockRedisServiceInfo2);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "redis-2.2");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		when(mockEnvironment.getServiceInfos(RedisServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);
		ApplicationContext context = getTestApplicationContext("test-redis-context.xml");
		JedisConnectionFactory replacedConnFactory = (JedisConnectionFactory) context.getBean("redisConnection",
				RedisConnectionFactory.class);
		Assert.assertEquals("localhost", replacedConnFactory.getHostName());
		Assert.assertEquals("localpass", replacedConnFactory.getPassword());
		Assert.assertEquals(6789, replacedConnFactory.getPort());
	}

	@Test
	public void cloudRedisConnFactoryLeavesOriginalInPlaceIfNoServicesDetected() {
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		when(mockEnvironment.getServices()).thenReturn(serviceList);
		ApplicationContext context = getTestApplicationContext("test-redis-context.xml");
		JedisConnectionFactory replacedConnFactory = (JedisConnectionFactory) context.getBean("redisConnection",
				RedisConnectionFactory.class);
		Assert.assertEquals("localhost", replacedConnFactory.getHostName());
		Assert.assertEquals("localpass", replacedConnFactory.getPassword());
		Assert.assertEquals(6789, replacedConnFactory.getPort());
	}

	@Test
	public void doesNothingIfRedisConnFactoryClassNotFound() {
		RedisConfigurer configurer = new StubRedisConfigurer(new ArrayList<Map<String,Object>>(), serviceCreator);
		assertFalse(configurer.configure(beanFactory));
	}

	private class StubRedisConfigurer extends RedisConfigurer {
		public StubRedisConfigurer(List<Map<String, Object>> cloudServices, RedisServiceCreator serviceCreator) {
			super(cloudServices, serviceCreator);
		}

		@Override
		protected Class<?> loadClass(String name) {
			return null;
		}
	}

}
