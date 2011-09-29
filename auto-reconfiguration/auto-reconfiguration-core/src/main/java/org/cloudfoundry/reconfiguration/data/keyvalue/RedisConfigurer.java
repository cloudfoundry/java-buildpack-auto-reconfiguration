package org.cloudfoundry.reconfiguration.data.keyvalue;

import org.cloudfoundry.reconfiguration.AbstractServiceConfigurer;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.cloudfoundry.runtime.service.keyvalue.RedisServiceCreator;
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

	public RedisConfigurer(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, RedisServiceInfo.class);
	}

	@Override
	public String getBeanClass() {
		return REDIS_CONN_FACTORY_CLASS_NAME;
	}

	@Override
	public String getServiceBeanName() {
		return CF_REDIS_CONN_FACTORY_NAME;
	}

	@Override
	public AbstractServiceCreator<?, RedisServiceInfo> getServiceCreator() {
		return new RedisServiceCreator();
	}
}
