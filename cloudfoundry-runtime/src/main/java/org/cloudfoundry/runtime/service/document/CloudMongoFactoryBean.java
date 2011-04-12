package org.cloudfoundry.runtime.service.document;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;

import com.mongodb.Mongo;

/**
 * Spring factory bean for Mongo service.
 * 
 * @author Ramnivas Laddad
 *
 */
public class CloudMongoFactoryBean extends AbstractCloudServiceFactory<Mongo, MongoServiceInfo> {
	public CloudMongoFactoryBean(CloudEnvironment cloudEnvironment) {
		super(new MongoServiceCreator(cloudEnvironment));
	}

	public CloudMongoFactoryBean() {
		super(new MongoServiceCreator(new CloudEnvironment()));
	}

	@Override
	public Class<?> getObjectType() {
		return Mongo.class;
	}
}
