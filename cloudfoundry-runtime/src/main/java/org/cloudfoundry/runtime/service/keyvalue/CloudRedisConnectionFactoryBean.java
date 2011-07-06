package org.cloudfoundry.runtime.service.keyvalue;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Spring factory bean for Redis service.
 * 
 * @author Ramnivas Laddad
 *
 */
public class CloudRedisConnectionFactoryBean extends AbstractCloudServiceFactory<RedisConnectionFactory, RedisServiceInfo> {
	public CloudRedisConnectionFactoryBean(CloudEnvironment cloudEnvironment) {
		super(new RedisServiceCreator(cloudEnvironment));
	}

	public CloudRedisConnectionFactoryBean() {
		super(new RedisServiceCreator(new CloudEnvironment()));
	}

	@Override
	public Class<?> getObjectType() {
		return RedisConnectionFactory.class;
	}
}
