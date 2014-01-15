package org.cloudfoundry.reconfiguration;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.service.ServiceInfo;

/**
 * 
 * @author Ramnivas Laddad
 *
 * @param <SI>
 */
abstract public class AbstractServiceConfigurer<SI extends ServiceInfo> implements Configurer {

	private final Logger logger = Logger.getLogger(getClass().getName());

	protected Cloud cloud;

	protected Class<SI> serviceInfoClass;

	public AbstractServiceConfigurer(Cloud cloud, Class<SI> serviceInfoClass) {
		this.cloud = cloud;
		this.serviceInfoClass = serviceInfoClass;
	}

	public boolean configure(DefaultListableBeanFactory beanFactory) {
		Class<?> serviceConnectorType = loadClass(getBeanClass());
		
		if (serviceConnectorType == null) {
			logger.log(Level.INFO, "Class " + getBeanClass() + " not in classpath. Skipping auto-reconfiguration for it");
			return false;
		}
		
 		String[] beanNames = getBeanNames(beanFactory);
		if (beanNames.length == 0) {
			logger.log(Level.INFO, "No beans of type " + getBeanClass() 
					+ " found in application context. Skipping auto-reconfiguration.");
			return false;
		} else if (beanNames.length > 1) {
			logger.log(Level.INFO, "More than 1 (" + beanNames.length + ") beans of type " + getBeanClass()
					+ " found in application context. Skipping autoreconfiguration.");
			return false;
		}
		List<ServiceInfo> cloudServices = cloud.getServiceInfos(serviceConnectorType);
		if (cloudServices.isEmpty()) {
			logger.log(Level.INFO, "No services found. Skipping auto-reconfiguration");
			return false;
		}
		if (cloudServices.size() > 1) {
			logger.log(Level.INFO, "Error creating cloud service. Skipping auto-reconfiguration.  Expected 1 service of "
					+ serviceInfoClass + " type, but found " + cloudServices.size());
			return false;
		}
		try {
			Object serviceConnector = cloud.getSingletonServiceConnector(serviceConnectorType, null);
			beanFactory.registerSingleton(getServiceBeanName(), serviceConnector);
			beanFactory.removeBeanDefinition(beanNames[0]);
			beanFactory.registerAlias(getServiceBeanName(), beanNames[0]);
			return true;
		} catch (CloudException ex) {
			logger.log(Level.INFO, "Error creating cloud service. Skipping auto-reconfiguration", ex);
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
