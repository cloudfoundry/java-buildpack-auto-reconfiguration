package org.cloudfoundry.reconfiguration.data.document;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cloudfoundry.reconfiguration.Configurer;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.service.document.MongoServiceCreator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.data.mongodb.MongoDbFactory;

/**
 * Implementation of {@link Configurer} that replaces a single
 * {@link MongoDbFactory} with one connecting to a mongo cloud service bound to
 * the current application.
 *
 * @author Jennifer Hickey
 *
 */
public class MongoConfigurer implements Configurer {

	private final Logger logger = Logger.getLogger(MongoConfigurer.class.getName());

	static final String CF_MONGO_DB_FACTORY_NAME = "__cloudFoundryMongoDbFactory";

	private CloudEnvironment cloudEnvironment;

	private MongoServiceCreator serviceCreator;

	public MongoConfigurer(CloudEnvironment cloudEnvironment) {
		this.cloudEnvironment = cloudEnvironment;
		this.serviceCreator = new MongoServiceCreator(cloudEnvironment);
	}

	@Override
	public boolean configure(DefaultListableBeanFactory beanFactory) {
		String[] mongoDbFactoryBeanNames = beanFactory.getBeanNamesForType(MongoDbFactory.class);
		if (mongoDbFactoryBeanNames.length == 0) {
			logger.log(Level.INFO, "No MongoDbFactory found in application context");
			return false;
		} else if (mongoDbFactoryBeanNames.length > 1) {
			logger.log(Level.INFO, "More than 1 (" + mongoDbFactoryBeanNames.length
					+ ") MongoDbFactory beans found in application context. Skipping autostaging.");
			return false;
		}
		for (Map<String, Object> service : cloudEnvironment.getServices()) {
			String label = (String) service.get("label");
			if (label == null) {
				continue;
			}
			if (label.startsWith("mongodb")) {
				try {
					beanFactory.registerSingleton(CF_MONGO_DB_FACTORY_NAME,
							serviceCreator.createSingletonService().service);
					beanFactory.removeBeanDefinition(mongoDbFactoryBeanNames[0]);
					beanFactory.registerAlias(CF_MONGO_DB_FACTORY_NAME, mongoDbFactoryBeanNames[0]);
					return true;
				} catch (CloudServiceException ex) {
					logger.log(Level.INFO, "Multiple mongo services found. Skipping autostaging", ex);
					return false;
				}
			}
		}
		logger.log(Level.INFO, "No mongo service found. Skipping autostaging");
		return false;
	}

	public void setServiceCreator(MongoServiceCreator serviceCreator) {
		this.serviceCreator = serviceCreator;
	}
}
