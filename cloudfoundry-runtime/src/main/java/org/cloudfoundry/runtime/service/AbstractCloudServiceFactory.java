package org.cloudfoundry.runtime.service;

import java.util.List;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.service.AbstractServiceCreator.ServiceNameTuple;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Abstract base factory class.
 * <p>
 * This factory uses the service creator provided through the constructor to
 * create services. If the service name is provided it creates a service object
 * based on the service bound to that name. Otherwise, it creates a singleton
 * service and fails if it doesn't find a unique service of the expected type.
 *
 * @author Ramnivas Laddad
 * @author Thomas Risberg
 *
 * @param <S>
 *            The service type
 * @param <SI>
 *            The service info type matching the service to be created
 */
public abstract class AbstractCloudServiceFactory<S, SI extends AbstractServiceInfo> extends AbstractFactoryBean<S> {

	private Class<SI> serviceInfoClass;

	protected String serviceName;

	private CloudEnvironment cloudEnvironment;

	public AbstractCloudServiceFactory(Class<SI> serviceInfoClass, CloudEnvironment cloudEnvironment) {
		this.serviceInfoClass = serviceInfoClass;
		this.cloudEnvironment = cloudEnvironment;
	}

	/**
	 * Optional service name property. If this property isn't set or set to
	 * null, unique service of the expected type (redis, for example) needs to
	 * be bound to the application.
	 *
	 * @param serviceName
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	protected S createInstance() throws Exception {
		if (serviceName != null) {
			SI serviceInfo = cloudEnvironment.getServiceInfo(serviceName, serviceInfoClass);
			if (serviceInfo == null) {
				return null;
			}
			return getServiceCreator().createSingletonService(serviceInfo).service;
		} else {
			List<SI> serviceInfos = cloudEnvironment.getServiceInfos(serviceInfoClass);
			if (serviceInfos.size() != 1) {
				throw new CloudServiceException("Expected 1 service of " + serviceInfoClass + " type, but found"
						+ serviceInfos.size());
			}
			return getServiceCreator().createSingletonService(serviceInfos.get(0)).service;
		}
	}

	/**
	 *
	 * @return A list of {@link ServiceNameTuple}s containing beans for each
	 *         service of the expected type (redis, for example) bound to the
	 *         application
	 */
	public List<ServiceNameTuple<S>> createInstances() {
		return getServiceCreator().createServices(cloudEnvironment.getServiceInfos(serviceInfoClass));
	}

	protected abstract AbstractServiceCreator<S, SI> getServiceCreator();
}
