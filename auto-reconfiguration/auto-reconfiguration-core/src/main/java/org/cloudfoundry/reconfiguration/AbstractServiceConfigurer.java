package org.cloudfoundry.reconfiguration;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

abstract public class AbstractServiceConfigurer<SC extends AbstractServiceCreator<?,?>> implements Configurer {

	private final Logger logger = Logger.getLogger(getClass().getName());

	protected CloudEnvironment cloudEnvironment;

	protected SC serviceCreator;

	public AbstractServiceConfigurer(CloudEnvironment cloudEnvironment, SC serviceCreator) {
		this.cloudEnvironment = cloudEnvironment;
		this.serviceCreator = serviceCreator;
	}

	public boolean configure(DefaultListableBeanFactory beanFactory) {
		Class<?> beanClass = loadClass(getBeanClass());
		if(beanClass == null) {
			logger.log(Level.INFO, "Class " + getBeanClass() + " not found.  Skipping autostaging.");
			return false;
		}
		String[] beanNames = beanFactory.getBeanNamesForType(beanClass);
		if (beanNames.length == 0) {
			logger.log(Level.INFO, "No beans of type " + getBeanClass() + " found in application context");
			return false;
		} else if (beanNames.length > 1) {
			logger.log(Level.INFO, "More than 1 (" + beanNames.length
					+ ") beans of type " + getBeanClass() + "found in application context. Skipping autostaging.");
			return false;
		}
		for (Map<String, Object> service : cloudEnvironment.getServices()) {
			String label = (String) service.get("label");
			if (label == null) {
				continue;
			}
			if (label.startsWith(getServiceLabel())) {
				try {
					beanFactory.registerSingleton(getServiceBeanName(),
							serviceCreator.createSingletonService().service);
					beanFactory.removeBeanDefinition(beanNames[0]);
					beanFactory.registerAlias(getServiceBeanName(), beanNames[0]);
					return true;
				} catch (CloudServiceException ex) {
					logger.log(Level.INFO, "Error creating cloud service. Skipping autostaging", ex);
					return false;
				}
			}
		}
		logger.log(Level.INFO, "No services found. Skipping autostaging");
		return false;
	}

	abstract public String getServiceLabel();

	abstract public String getBeanClass();

	abstract public String getServiceBeanName();

	protected Class<?> loadClass(String name) {
		try {
			return Class.forName(name);
		} catch (Throwable ex) {
			return null;
		}
	}
}
