package org.cloudfoundry.runtime.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.service.AbstractServiceCreator.ServiceNameTuple;
import org.cloudfoundry.runtime.service.document.MongoServiceCreator;
import org.cloudfoundry.runtime.service.keyvalue.RedisServiceCreator;
import org.cloudfoundry.runtime.service.messaging.RabbitServiceCreator;
import org.cloudfoundry.runtime.service.relational.MysqlServiceCreator;
import org.cloudfoundry.runtime.service.relational.PostgresqlServiceCreator;
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
 *
 */
public class CloudServicesScanner implements BeanFactoryPostProcessor {

	Logger logger = Logger.getLogger(CloudServicesScanner.class.getName());

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
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

		List<AbstractServiceCreator> serviceCreators = new ArrayList<AbstractServiceCreator>();
		serviceCreators.add(new MysqlServiceCreator(cloudEnvironment));
		serviceCreators.add(new PostgresqlServiceCreator(cloudEnvironment));
		serviceCreators.add(new RedisServiceCreator(cloudEnvironment));
		serviceCreators.add(new RabbitServiceCreator(cloudEnvironment));
		serviceCreators.add(new MongoServiceCreator(cloudEnvironment));

		logger.info("Auto-creating service beans");

		for (AbstractServiceCreator serviceCreator : serviceCreators) {
			List<ServiceNameTuple<Object>> serviceNamePairs = serviceCreator.createServices();
			for (ServiceNameTuple<Object> serviceNamePair : serviceNamePairs) {
				logger.info("Auto-creating service bean for " + serviceNamePair.name);
				beanFactory.registerSingleton(serviceNamePair.name, serviceNamePair.service);
			}
		}

	}

}
