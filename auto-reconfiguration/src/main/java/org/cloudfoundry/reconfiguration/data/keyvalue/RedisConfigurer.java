package org.cloudfoundry.reconfiguration.data.keyvalue;

import org.cloudfoundry.reconfiguration.AbstractServiceConfigurer;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.common.RedisServiceInfo;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Implementation of {@link AbstractServiceConfigurer} that replaces a single
 * {@link RedisConnectionFactory} with one connecting to a redis cloud service
 * bound to the current application.
 *
 * @author Jennifer Hickey
 *
 */
public class RedisConfigurer extends AbstractServiceConfigurer<RedisServiceInfo> {

	private static final String CF_REDIS_CONN_FACTORY_NAME = "__cloudFoundryRedisConnectionFactory";

	private static final String REDIS_CONN_FACTORY_CLASS_NAME = "org.springframework.data.redis.connection.RedisConnectionFactory";

	public RedisConfigurer(Cloud cloud) {
		super(cloud, RedisServiceInfo.class);
	}

	@Override
	public String getBeanClass() {
		return REDIS_CONN_FACTORY_CLASS_NAME;
	}

	@Override
	public String getServiceBeanName() {
		return CF_REDIS_CONN_FACTORY_NAME;
	}
}
