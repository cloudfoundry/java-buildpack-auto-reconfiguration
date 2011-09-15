package org.cloudfoundry.runtime.service;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Abstract base factory class.
 * <p>
 * This factory uses the service creator provided through the constructor to create services.
 * If the service name is provided it creates a service object based on the service bound 
 * to that name. Otherwise, it creates a singleton service and fails if it doesn't
 * find a unique service of the expected type.
 * 
 * @author Ramnivas Laddad
 *
 * @param <S> The service type
 * @param <SI> The service info type matching the service to be created
 */
public abstract class AbstractCloudServiceFactory<S, SI extends AbstractServiceInfo> 
    extends AbstractFactoryBean<S> {
	
	protected AbstractServiceCreator<S, SI> serviceCreator;

	protected String serviceName;

	public AbstractCloudServiceFactory(AbstractServiceCreator<S, SI> creationHelper) {
		this.serviceCreator = creationHelper;
	}

	/**
	 * Optional service name property. If this property isn't set or set to null,
	 * unique service of the expected type (redis, for example) needs to be bound
	 * to the application.
	 * 
	 * @param serviceName
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	@Override
	protected S createInstance() throws Exception {
		if (serviceName != null) {
			return serviceCreator.createService(serviceName);
		} else {
			return serviceCreator.createSingletonService().service;
		}
	}
}
