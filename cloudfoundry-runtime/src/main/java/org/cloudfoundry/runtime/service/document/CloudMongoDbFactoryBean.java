package org.cloudfoundry.runtime.service.document;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;
import org.cloudfoundry.runtime.service.CloudMongoConfiguration;
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
		super(new MongoServiceCreator(),MongoServiceInfo.class,cloudEnvironment);
		this.mongoServiceCreator = (MongoServiceCreator) super.serviceCreator;
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
}
