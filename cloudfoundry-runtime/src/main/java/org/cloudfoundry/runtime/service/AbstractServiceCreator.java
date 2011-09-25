package org.cloudfoundry.runtime.service;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.springframework.beans.BeanWrapper;

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
 * @author Jennifer Hickey
 * @author Thomas Risberg
 *
 * @param <S> Service type to be create
 * @param <SI> ServiceInfo type
 */
public abstract class AbstractServiceCreator<S, SI extends AbstractServiceInfo> {

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
	 * @param singletonServiceInfo
	 * @return service object along with the name of the matching service
	 */
	public ServiceNameTuple<S> createSingletonService(SI singletonServiceInfo) {
		return new ServiceNameTuple<S>(createService(singletonServiceInfo), singletonServiceInfo.getServiceName());
	}

	/**
	 * Create service objects for all specified services
	 *
	 * @param serviceInfos
	 * @return service objects along with the name of the matching services
	 */
	public List<ServiceNameTuple<S>> createServices(List<SI> serviceInfos) {
		List<ServiceNameTuple<S>> services = new ArrayList<ServiceNameTuple<S>>();
		for (SI serviceInfo : serviceInfos) {
			services.add(new ServiceNameTuple<S>(createService(serviceInfo), serviceInfo.getServiceName()));
		}
		return services;
	}

	protected void setCorrespondingProperties(BeanWrapper target, BeanWrapper source) {
		for (PropertyDescriptor pd : source.getPropertyDescriptors()) {
			String property = pd.getName();
			if (!"class".equals(property) && source.isReadableProperty(property) &&
						source.getPropertyValue(property) != null) {
				if (target.isWritableProperty(property)) {
					target.setPropertyValue(property, source.getPropertyValue(property));
				}
			}
		}
	}

	protected boolean hasClass(String name) {
		try {
			Class.forName(name);
			return true;
		} catch (Throwable ex) {
			return false;
		}
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
