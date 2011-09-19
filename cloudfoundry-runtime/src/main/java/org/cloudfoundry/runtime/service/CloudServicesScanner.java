package org.cloudfoundry.runtime.service;

import java.util.List;
import java.util.logging.Logger;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator.ServiceNameTuple;
import org.cloudfoundry.runtime.service.document.MongoServiceCreator;
import org.cloudfoundry.runtime.service.keyvalue.RedisServiceCreator;
import org.cloudfoundry.runtime.service.messaging.RabbitServiceCreator;
import org.cloudfoundry.runtime.service.relational.RdbmsServiceCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Bean factory post processor that adds a bean for each service bound to the application.
 *
 * <p>
 * Each service populated by this bean has the same name as the service it is bound to.
 * </p>
 *
 * Usage:
 * <p>
 * An application may add a bean of this type:
 * <pre>
 * &lt;bean class="org.cloudfoundry.runtime.service.CloudServicesScanner"/&gt;
 * </pre>
 * to have an easy access to all the services.
 *
 * If there is unique bean of a type, you can inject beans using the following
 * code (shows Redis, but the same scheme works for all services):
 * <pre>
 * &#64;Autowired RedisConnectionFactory redisConnectionFactory;
 * </pre>
 *
 * If there are more than one services of a type, you can use the @Qualifier
 * as in the following code:
 * <pre>
 * &#64;Autowired &#64;Qualifier("service-name1") RedisConnectionFactory redisConnectionFactory;
 * &#64;Autowired &#64;Qualifier("service-name2") RedisConnectionFactory redisConnectionFactory;
 * </pre>
 *
 * You may, of course, use XML-based configuration.
 *
 * @author Ramnivas Laddad
 * @author Jennifer Hickey
 *
 */
public class CloudServicesScanner implements BeanFactoryPostProcessor {

	Logger logger = Logger.getLogger(CloudServicesScanner.class.getName());

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		CloudEnvironment cloudEnvironment;

		String[] envBeans = beanFactory.getBeanNamesForType(CloudEnvironment.class);
		if (envBeans.length == 0) {
			cloudEnvironment = new CloudEnvironment();
			beanFactory.registerSingleton("cloud-environment", cloudEnvironment);
		} else if (envBeans.length == 1){
			cloudEnvironment = beanFactory.getBean(CloudEnvironment.class);
		} else {
			throw new CloudServiceException("CloudServicesScanner expects 0 or 1 bean of CloudEnvironment, found "
					+ envBeans.length);
		}
		createCloudServiceBeans(beanFactory, cloudEnvironment);
	}

	/**
	 * Create and register beans for each cloud service bound to the application
	 * @param beanFactory
	 * @param cloudEnvironment
	 */
	protected void createCloudServiceBeans(ConfigurableListableBeanFactory beanFactory, CloudEnvironment cloudEnvironment) {
		logger.info("Auto-creating service beans");
		if(hasServicesOfType(cloudEnvironment, MongoServiceInfo.class)) {
			registerServiceBeans(beanFactory, cloudEnvironment, new MongoServiceCreator(), MongoServiceInfo.class);
		}
		if(hasServicesOfType(cloudEnvironment, RedisServiceInfo.class)) {
			registerServiceBeans(beanFactory, cloudEnvironment, new RedisServiceCreator(), RedisServiceInfo.class);
		}
		if(hasServicesOfType(cloudEnvironment, RabbitServiceInfo.class)) {
			registerServiceBeans(beanFactory, cloudEnvironment, new RabbitServiceCreator(), RabbitServiceInfo.class);
		}
		if(hasServicesOfType(cloudEnvironment, RdbmsServiceInfo.class)) {
			registerServiceBeans(beanFactory, cloudEnvironment, new RdbmsServiceCreator(), RdbmsServiceInfo.class);
		}
	}

	/**
	 * Verifies that there are services of a specified type bound to the application.  It is very important to
	 * check for bound services before instantiating the {@link AbstractServiceCreator}, as the service creators may have
	 * third party dependencies that are not present on the classpath
	 * @param cloudEnvironment
	 * @param serviceInfoClass
	 * @return If there are services of the specified type bound to the application
	 */
	protected boolean hasServicesOfType(CloudEnvironment cloudEnvironment, Class<? extends AbstractServiceInfo> serviceInfoClass) {
		if(cloudEnvironment.getServiceInfos(serviceInfoClass).isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Registers specified service beans
	 *
	 * @param beanFactory
	 * @param serviceNamePairs
	 */
	protected <S, T extends AbstractServiceInfo> void registerServiceBeans(ConfigurableListableBeanFactory beanFactory,
			CloudEnvironment cloudEnvironment, AbstractServiceCreator<S, T> serviceCreator, Class<T> serviceInfoType) {
		List<ServiceNameTuple<S>> serviceNamePairs = serviceCreator.createServices(cloudEnvironment
				.getServiceInfos(serviceInfoType));
		for (ServiceNameTuple<S> serviceNamePair : serviceNamePairs) {
			logger.info("Auto-creating service bean for " + serviceNamePair.name);
			beanFactory.registerSingleton(serviceNamePair.name, serviceNamePair.service);
		}
	}
}
