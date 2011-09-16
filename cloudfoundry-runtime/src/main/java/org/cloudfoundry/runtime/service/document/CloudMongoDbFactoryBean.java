package org.cloudfoundry.runtime.service.document;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;
import org.springframework.data.mongodb.MongoDbFactory;

/**
 * Spring factory bean for Mongo service.
 *
 * @author Ramnivas Laddad
 *
 */
public class CloudMongoDbFactoryBean extends AbstractCloudServiceFactory<MongoDbFactory, MongoServiceInfo> {

	private MongoServiceCreator mongoServiceCreator;

	public CloudMongoDbFactoryBean(CloudEnvironment cloudEnvironment) {
		super(new MongoServiceCreator(cloudEnvironment));
		this.mongoServiceCreator = (MongoServiceCreator) serviceCreator;
	}

	public CloudMongoDbFactoryBean() {
		this(new CloudEnvironment());
	}

	/**
	 * Optional autoConnectRetry property.
	 *
	 * @param autoConnectRetry
	 */
	public void setAutoConnectRetry(Boolean autoConnectRetry) {
		this.mongoServiceCreator.getMongoConfiguration().setAutoConnectRetry(autoConnectRetry);
	}

	/**
	 * Optional maxAutoConnectRetryTime property.
	 *
	 * @param maxAutoConnectRetryTime
	 */
	public void setMaxAutoConnectRetryTime(Long maxAutoConnectRetryTime) {
		this.mongoServiceCreator.getMongoConfiguration().setMaxAutoConnectRetryTime(maxAutoConnectRetryTime);
	}

	/**
	 * Optional socketKeepAlive property.
	 *
	 * @param socketKeepAlive
	 */
	public void setSocketKeepAlive(Boolean socketKeepAlive) {
		this.mongoServiceCreator.getMongoConfiguration().setSocketKeepAlive(socketKeepAlive);
	}

	/**
	 * Optional socketTimeout property.
	 *
	 * @param socketTimeout
	 */
	public void setSocketTimeout(Integer socketTimeout) {
		this.mongoServiceCreator.getMongoConfiguration().setSocketTimeout(socketTimeout);
	}

	/**
	 * Optional fsync property.
	 *
	 * @param fsync
	 */
	public void setFsync(Boolean fsync) {
		this.mongoServiceCreator.getMongoConfiguration().setFsync(fsync);
	}

	/**
	 * Optional connectionsPerHost  property.
	 *
	 * @param connectionsPerHost
	 */
	public void setConnectionsPerHost(Integer connectionsPerHost ) {
		this.mongoServiceCreator.getMongoConfiguration().setConnectionsPerHost(connectionsPerHost);
	}

	/**
	 * Optional threadsAllowedToBlockForConnectionMultiplier property.
	 *
	 * @param threadsAllowedToBlockForConnectionMultiplier
	 */
	public void setThreadsAllowedToBlockForConnectionMultiplier(Integer threadsAllowedToBlockForConnectionMultiplier) {
		this.mongoServiceCreator.getMongoConfiguration().setThreadsAllowedToBlockForConnectionMultiplier(
				threadsAllowedToBlockForConnectionMultiplier);
	}

	/**
	 * Optional connectTimeout property.
	 *
	 * @param connectTimeout
	 */
	public void setConnectTimeout(Integer connectTimeout) {
		this.mongoServiceCreator.getMongoConfiguration().setConnectTimeout(connectTimeout);
	}

	/**
	 * Optional maxWaitTime property.
	 *
	 * @param maxWaitTime
	 */
	public void setMaxWaitTime(Integer maxWaitTime) {
		this.mongoServiceCreator.getMongoConfiguration().setMaxWaitTime(maxWaitTime);
	}

	@Override
	public Class<?> getObjectType() {
		return MongoDbFactory.class;
	}
}
