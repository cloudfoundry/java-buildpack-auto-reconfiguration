package org.cloudfoundry.runtime.service.keyvalue;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.cloudfoundry.runtime.service.CloudPoolConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Spring factory bean for Redis service.
 *
 * @author Ramnivas Laddad
 * @author Thomas Risberg
 *
 */
public class CloudRedisConnectionFactoryBean extends AbstractCloudServiceFactory<RedisConnectionFactory, RedisServiceInfo> {

	private RedisServiceCreator redisServiceCreator;

	public CloudRedisConnectionFactoryBean(CloudEnvironment cloudEnvironment) {
		super(RedisServiceInfo.class, cloudEnvironment);
		this.redisServiceCreator = new RedisServiceCreator();
	}

	public CloudRedisConnectionFactoryBean() {
		this(new CloudEnvironment());
	}

	public void setCloudPoolConfiguration(CloudPoolConfiguration cloudPoolConfiguration) {
		this.redisServiceCreator.setCloudPoolConfiguration(cloudPoolConfiguration);
	}

	@Override
	public Class<?> getObjectType() {
		return RedisConnectionFactory.class;
	}

	@Override
	protected AbstractServiceCreator<RedisConnectionFactory, RedisServiceInfo> getServiceCreator() {
		return this.redisServiceCreator;
	}
}
