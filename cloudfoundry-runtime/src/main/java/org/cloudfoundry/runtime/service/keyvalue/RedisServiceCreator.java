package org.cloudfoundry.runtime.service.keyvalue;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

/**
 * Simplified access to creating Redis service objects.
 *
 * @author Ramnivas Laddad
 * @author Jennifer Hickey
 *
 */
public class RedisServiceCreator extends AbstractServiceCreator<RedisConnectionFactory, RedisServiceInfo> {

	private static final String REDIS_CLIENT_CLASS_NAME = "redis.clients.jedis.Jedis";

	public RedisServiceCreator(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, RedisServiceInfo.class);
	}

	public RedisConnectionFactory createService(RedisServiceInfo serviceInfo) {
		if (hasClass(REDIS_CLIENT_CLASS_NAME)) {
			JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
			connectionFactory.setHostName(serviceInfo.getHost());
			connectionFactory.setPort(serviceInfo.getPort());
			connectionFactory.setPassword(serviceInfo.getPassword());
			connectionFactory.afterPropertiesSet();
			return connectionFactory;
		} else {
			throw new CloudServiceException("Failed to created cloud Redis connection factory for "
					+ serviceInfo.getServiceName() + " service.  Jedis client implementation class ("
					+ REDIS_CLIENT_CLASS_NAME + ") not found");
		}
	}

	protected boolean hasClass(String name) {
		try {
			Class.forName(name);
			return true;
		} catch (Throwable ex) {
			return false;
		}
	}

}
