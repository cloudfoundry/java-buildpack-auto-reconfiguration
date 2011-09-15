package org.cloudfoundry.runtime.service;

import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;

/**
 * Base service creator
 * <p>
 * This class allows creating services based on
 * <ul>
 * <li>The singleton service of the matching type</li>
 * <li>The service matching the given name</li>
 * <li>All services matching of the matching type</li>
 * </ul>
 * 
 * @author Ramnivas Laddad
 *
 * @param <S> Service type to be create
 * @param <SI> ServiceInfo type
 */
public abstract class AbstractServiceCreator<S, SI extends AbstractServiceInfo> {
	
	private CloudEnvironment cloudEnvironment;
	private Class<SI> serviceInfoClass;
	
	public AbstractServiceCreator(CloudEnvironment cloudEnvironment, Class<SI> serviceInfoClass) {
		this.cloudEnvironment = cloudEnvironment;
		this.serviceInfoClass = serviceInfoClass;
	}
	
	/**
	 * Implementation of this method must create service based on the service info object passed.
	 * 
	 * @param serviceInfo
	 * @return service
	 */
	public abstract S createService(SI serviceInfo);
	
	/**
	 * Create service based on the unique service of the required type.
	 * 
	 * @return service object along with the name of the matching service
	 * @throws CloudServiceException if unique service of the expected type isn't bound to the application
	 */
	public ServiceNameTuple<S> createSingletonService() {
		List<SI> serviceInfos = cloudEnvironment.getServiceInfos(serviceInfoClass);
		
		if (serviceInfos.size() != 1) {
			throw new CloudServiceException("Expected 1 service of " + serviceInfoClass + " type, but found " + serviceInfos.size());
		}
		
		SI singletonServiceInfo = serviceInfos.get(0);
		return new ServiceNameTuple<S>(createService(singletonServiceInfo), singletonServiceInfo.getServiceName());
	}
	
	/**
	 * Create service object for the given name service bound to the application. 
	 * 
	 * @param serviceName
	 * @return service object
	 */
	public S createService(String serviceName) {
		SI serviceInfo = cloudEnvironment.getServiceInfo(serviceName, serviceInfoClass);
		if (serviceInfo != null) {
			return createService(serviceInfo);
		}
		return null;
	}
	
	/**
	 * Create service objects for all services of the matching type bound to the application.
	 * 
	 * @return service objects along with the name of the matching services
	 */
	public List<ServiceNameTuple<S>> createServices() {
		List<SI> serviceInfos = cloudEnvironment.getServiceInfos(serviceInfoClass);
		List<ServiceNameTuple<S>> services = new ArrayList<ServiceNameTuple<S>>();
		for (SI serviceInfo : serviceInfos) {
			services.add(new ServiceNameTuple<S>(createService(serviceInfo), serviceInfo.getServiceName()));
		}
		return services;
	}
	
	public static class ServiceNameTuple<S> {
		public ServiceNameTuple(S service, String name) {
			this.service = service;
			this.name = name;
		}
		
		public S service;
		public String name;
	}
}
