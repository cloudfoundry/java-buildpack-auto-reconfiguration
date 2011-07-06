package org.cloudfoundry.runtime.service.keyvalue;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

/**
 * Simplified access to creating Redis service objects.
 * 
 * @author Ramnivas Laddad
 *
 */
public class RedisServiceCreator 
	extends AbstractServiceCreator<RedisConnectionFactory, RedisServiceInfo> {
	
	public RedisServiceCreator(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, RedisServiceInfo.class);
	}
	
	public RedisConnectionFactory createService(RedisServiceInfo serviceInfo) {
		JedisConnectionFactory connectionFactory 
		    = new JedisConnectionFactory();
		connectionFactory.setHostName(serviceInfo.getHost());
		connectionFactory.setPort(serviceInfo.getPort());
		connectionFactory.setPassword(serviceInfo.getPassword());
		connectionFactory.afterPropertiesSet();
		return connectionFactory;
	}

}
