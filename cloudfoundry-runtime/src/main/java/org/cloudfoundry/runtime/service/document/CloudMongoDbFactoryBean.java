package org.cloudfoundry.runtime.service.document;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.springframework.data.mongodb.MongoDbFactory;

/**
 * Spring factory bean for Mongo service.
 *
 * @author Ramnivas Laddad
 * @author Thomas Risberg
 *
 */
public class CloudMongoDbFactoryBean extends AbstractCloudServiceFactory<MongoDbFactory, MongoServiceInfo> {

	private MongoServiceCreator mongoServiceCreator;

	public CloudMongoDbFactoryBean(CloudEnvironment cloudEnvironment) {
		super(MongoServiceInfo.class,cloudEnvironment);
		this.mongoServiceCreator = new MongoServiceCreator();
	}

	public CloudMongoDbFactoryBean() {
		this(new CloudEnvironment());
	}

	public void setCloudMongoConfiguration(CloudMongoConfiguration cloudMongoConfiguration) {
		this.mongoServiceCreator.setCloudMongoConfiguration(cloudMongoConfiguration);
	}

	@Override
	public Class<?> getObjectType() {
		return MongoDbFactory.class;
	}

	@Override
	protected AbstractServiceCreator<MongoDbFactory, MongoServiceInfo> getServiceCreator() {
		return this.mongoServiceCreator;
	}
}
