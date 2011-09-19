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
	public CloudMongoDbFactoryBean(CloudEnvironment cloudEnvironment) {
		super(new MongoServiceCreator(),MongoServiceInfo.class,cloudEnvironment);
	}

	public CloudMongoDbFactoryBean() {
		this(new CloudEnvironment());
	}

	@Override
	public Class<?> getObjectType() {
		return MongoDbFactory.class;
	}
}
