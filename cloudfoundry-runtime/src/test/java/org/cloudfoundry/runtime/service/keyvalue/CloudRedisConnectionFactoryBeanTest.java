package org.cloudfoundry.runtime.service.keyvalue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.keyvalue.CloudRedisConnectionFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.jredis.JredisConnectionFactory;

public class CloudRedisConnectionFactoryBeanTest {
	@Mock private CloudEnvironment mockRuntime;
	@Mock private RedisServiceInfo mockServiceInfo;
	private CloudRedisConnectionFactoryBean factory;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		factory = new CloudRedisConnectionFactoryBean(mockRuntime);
	}
	
	@Test
	public void cloudRedisConnectionFactoryCreation() throws Exception {
		when(mockRuntime.getServiceInfo("redis-1", RedisServiceInfo.class))
			.thenReturn(mockServiceInfo);
		when(mockServiceInfo.getHost()).thenReturn("10.20.30.40");
		when(mockServiceInfo.getPort()).thenReturn(10000);
		when(mockServiceInfo.getPassword()).thenReturn("mypass");
		
		factory.setServiceName("redis-1");
		factory.afterPropertiesSet();
		RedisConnectionFactory cloudConnectionFactory = factory.getObject();
		
		if (cloudConnectionFactory instanceof JredisConnectionFactory) {
			assertEquals("10.20.30.40", ((JredisConnectionFactory)cloudConnectionFactory).getHostName());
			assertEquals(10000, ((JredisConnectionFactory)cloudConnectionFactory).getPort());
			assertEquals("mypass", ((JredisConnectionFactory)cloudConnectionFactory).getPassword());
		} else if (cloudConnectionFactory instanceof JedisConnectionFactory) {
			assertEquals("10.20.30.40", ((JedisConnectionFactory)cloudConnectionFactory).getHostName());
			assertEquals(10000, ((JedisConnectionFactory)cloudConnectionFactory).getPort());
			assertEquals("mypass", ((JedisConnectionFactory)cloudConnectionFactory).getPassword());
		} else {
			fail();
		}
	}

}
