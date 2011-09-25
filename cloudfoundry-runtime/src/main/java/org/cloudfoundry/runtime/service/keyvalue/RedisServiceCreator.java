package org.cloudfoundry.runtime.service.keyvalue;

import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.cloudfoundry.runtime.service.CloudPoolConfiguration;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Simplified access to creating Redis service objects.
 *
 * @author Ramnivas Laddad
 * @author Jennifer Hickey
 * @author Thomas Risberg
 *
 */
public class RedisServiceCreator extends AbstractServiceCreator<RedisConnectionFactory, RedisServiceInfo> {

	private static final String REDIS_CLIENT_CLASS_NAME = "redis.clients.jedis.Jedis";

	private CloudPoolConfiguration cloudPoolConfiguration;

	public void setCloudPoolConfiguration(CloudPoolConfiguration cloudPoolConfiguration) {
		this.cloudPoolConfiguration = cloudPoolConfiguration;
	}

	public RedisConnectionFactory createService(RedisServiceInfo serviceInfo) {
		if (hasClass(REDIS_CLIENT_CLASS_NAME)) {
			JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
			connectionFactory.setHostName(serviceInfo.getHost());
			connectionFactory.setPort(serviceInfo.getPort());
			connectionFactory.setPassword(serviceInfo.getPassword());
			JedisPoolConfig poolConfig = null;
			if (cloudPoolConfiguration != null) {
				poolConfig = new JedisPoolConfig();
				BeanWrapper target = new BeanWrapperImpl(poolConfig);
				BeanWrapper source = new BeanWrapperImpl(cloudPoolConfiguration);
				setCorrespondingProperties(target, source);
			}
			if (poolConfig != null) {
				connectionFactory.setPoolConfig(poolConfig);
			}
			connectionFactory.afterPropertiesSet();
			return connectionFactory;
		} else {
			throw new CloudServiceException("Failed to created cloud Redis connection factory for "
					+ serviceInfo.getServiceName() + " service.  Jedis client implementation class ("
					+ REDIS_CLIENT_CLASS_NAME + ") not found");
		}
	}

}
