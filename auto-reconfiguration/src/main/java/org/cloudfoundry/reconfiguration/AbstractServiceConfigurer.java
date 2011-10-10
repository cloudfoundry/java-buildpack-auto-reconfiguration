package org.cloudfoundry.reconfiguration;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

abstract public class AbstractServiceConfigurer<SI extends AbstractServiceInfo>
		implements Configurer {

	private final Logger logger = Logger.getLogger(getClass().getName());

	protected CloudEnvironment cloudEnvironment;

	protected Class<SI> serviceInfoClass;

	public AbstractServiceConfigurer(CloudEnvironment cloudEnvironment, Class<SI> serviceInfoClass) {
		this.cloudEnvironment = cloudEnvironment;
		this.serviceInfoClass = serviceInfoClass;
	}

	public boolean configure(DefaultListableBeanFactory beanFactory) {
		String[] beanNames = getBeanNames(beanFactory);
		if (beanNames.length == 0) {
			logger.log(Level.INFO, "No beans of type " + getBeanClass() + " found in application context");
			return false;
		} else if (beanNames.length > 1) {
			logger.log(Level.INFO, "More than 1 (" + beanNames.length + ") beans of type " + getBeanClass()
					+ "found in application context. Skipping autostaging.");
			return false;
		}
		List<SI> cloudServices = cloudEnvironment.getServiceInfos(serviceInfoClass);
		if (cloudServices.isEmpty()) {
			logger.log(Level.INFO, "No services found. Skipping autostaging");
			return false;
		}
		if (cloudServices.size() > 1) {
			logger.log(Level.INFO, "Error creating cloud service. Skipping autostaging.  Expected 1 service of "
					+ serviceInfoClass + " type, but found " + cloudServices.size());
			return false;
		}
		try {
			beanFactory.registerSingleton(getServiceBeanName(),
					getServiceCreator().createSingletonService(cloudServices.get(0)).service);
			beanFactory.removeBeanDefinition(beanNames[0]);
			beanFactory.registerAlias(getServiceBeanName(), beanNames[0]);
			return true;
		} catch (CloudServiceException ex) {
			logger.log(Level.INFO, "Error creating cloud service. Skipping autostaging", ex);
			return false;
		}
	}

	protected String[] getBeanNames(DefaultListableBeanFactory beanFactory) {
		Class<?> beanClass = loadClass(getBeanClass());
		if (beanClass == null) {
			logger.log(Level.INFO, "Class " + getBeanClass() + " not found.  Skipping autostaging.");
			return new String[0];
		}
		return beanFactory.getBeanNamesForType(beanClass,true,false);
	}

	/**
	 * Avoid creating an {@link AbstractServiceCreator} until we need to create a bean, as the creators may have
	 * dependencies on third party software (such as Mongo, Spring Data, etc)
	 * @return The {@link AbstractServiceCreator} to use
	 */
	abstract public AbstractServiceCreator<?, SI> getServiceCreator();

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
