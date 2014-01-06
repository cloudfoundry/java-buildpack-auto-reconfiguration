package org.cloudfoundry.reconfiguration.data.document;

import org.cloudfoundry.reconfiguration.AbstractServiceConfigurer;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.common.MongoServiceInfo;
import org.springframework.data.mongodb.MongoDbFactory;

/**
 * Implementation of {@link AbstractServiceConfigurer} that replaces a single
 * {@link MongoDbFactory} with one connecting to a mongo cloud service bound to
 * the current application.
 *
 * @author Jennifer Hickey
 *
 */
public class MongoConfigurer extends AbstractServiceConfigurer<MongoServiceInfo> {

	static final String CF_MONGO_DB_FACTORY_NAME = "__cloudFoundryMongoDbFactory";

	private static final String MONGO_DB_FACTORY_CLASS_NAME = "org.springframework.data.mongodb.MongoDbFactory";

	public MongoConfigurer(Cloud cloud) {
		super(cloud, MongoServiceInfo.class);
	}

	@Override
	public String getBeanClass() {
		return MONGO_DB_FACTORY_CLASS_NAME;
	}

	@Override
	public String getServiceBeanName() {
		return CF_MONGO_DB_FACTORY_NAME;
	}
}
