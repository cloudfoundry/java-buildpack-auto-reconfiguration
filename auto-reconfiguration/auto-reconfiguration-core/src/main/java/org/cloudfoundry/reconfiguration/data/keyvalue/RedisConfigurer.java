package org.cloudfoundry.reconfiguration.data.keyvalue;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cloudfoundry.reconfiguration.Configurer;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.service.keyvalue.RedisServiceCreator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Implementation of {@link Configurer} that replaces a single
 * {@link RedisConnectionFactory} with one connecting to a redis cloud service
 * bound to the current application.
 *
 * @author Jennifer Hickey
 *
 */
public class RedisConfigurer implements Configurer {

	private final Logger logger = Logger.getLogger(RedisConfigurer.class.getName());

	private static final String CF_REDIS_CONN_FACTORY_NAME = "__cloudFoundryRedisConnectionFactory";

	private static final String REDIS_CONN_FACTORY_CLASS_NAME = "org.springframework.data.redis.connection.RedisConnectionFactory";

	private CloudEnvironment cloudEnvironment;

	public RedisConfigurer(CloudEnvironment cloudEnvironment) {
		this.cloudEnvironment = cloudEnvironment;
	}

	@Override
	public boolean configure(DefaultListableBeanFactory beanFactory) {
		String[] redisConnFactoryBeanNames = beanFactory.getBeanNamesForType(loadClass(REDIS_CONN_FACTORY_CLASS_NAME));
		if (redisConnFactoryBeanNames.length == 0) {
			logger.log(Level.INFO, "No RedisConnectionFactory found in application context");
			return false;
		} else if (redisConnFactoryBeanNames.length > 1) {
			logger.log(Level.INFO, "More than 1 (" + redisConnFactoryBeanNames.length
					+ ") RedisConnectionFactory beans found in application context. Skipping autostaging.");
			return false;
		}
		for (Map<String, Object> service : cloudEnvironment.getServices()) {
			String label = (String) service.get("label");
			if (label == null) {
				continue;
			}
			if (label.startsWith("redis")) {
				try {
					RedisServiceCreator redisServiceCreator = new RedisServiceCreator(cloudEnvironment);
					beanFactory.registerSingleton(CF_REDIS_CONN_FACTORY_NAME,
							redisServiceCreator.createSingletonService().service);
					beanFactory.removeBeanDefinition(redisConnFactoryBeanNames[0]);
					beanFactory.registerAlias(CF_REDIS_CONN_FACTORY_NAME, redisConnFactoryBeanNames[0]);
					return true;
				} catch (CloudServiceException ex) {
					logger.log(Level.INFO, "Error creating cloud service. Skipping autostaging", ex);
					return false;
				}
			}
		}
		logger.log(Level.INFO, "No redis service found. Skipping autostaging");
		return false;
	}

	private Class<?> loadClass(String name) {
		try {
			return Class.forName(name);
		} catch (Throwable ex) {
			return null;
		}
	}
}
