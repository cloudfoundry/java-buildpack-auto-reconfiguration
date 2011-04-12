package org.cloudfoundry.runtime.service.messaging;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

/**
 * Spring factory bean for Rabbit service.
 * 
 * @author Ramnivas Laddad
 *
 */
public class CloudRabbitConnectionFactoryBean extends AbstractCloudServiceFactory<ConnectionFactory, RabbitServiceInfo> {

	public CloudRabbitConnectionFactoryBean(CloudEnvironment cloudEnvironment) {
		super(new RabbitServiceCreator(cloudEnvironment));
	}
	
	public CloudRabbitConnectionFactoryBean() {
		super(new RabbitServiceCreator(new CloudEnvironment()));
	}

	@Override
	public Class<?> getObjectType() {
		return ConnectionFactory.class;
	}
}
