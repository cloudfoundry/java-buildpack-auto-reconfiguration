package org.cloudfoundry.runtime.service.messaging;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;
import org.cloudfoundry.runtime.service.CloudCacheConfiguration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

/**
 * Spring factory bean for Rabbit service.
 *
 * @author Ramnivas Laddad
 * @author Thomas Risberg
 *
 */
public class CloudRabbitConnectionFactoryBean extends AbstractCloudServiceFactory<ConnectionFactory, RabbitServiceInfo> {

	private RabbitServiceCreator rabbitServiceCreator;

	public CloudRabbitConnectionFactoryBean(CloudEnvironment cloudEnvironment) {
		super(new RabbitServiceCreator(), RabbitServiceInfo.class,cloudEnvironment);
		this.rabbitServiceCreator = (RabbitServiceCreator) super.serviceCreator;
	}

	public CloudRabbitConnectionFactoryBean() {
		this(new CloudEnvironment());
	}

	public void setCloudCacheConfiguration(CloudCacheConfiguration cloudCacheConfiguration) {
		this.rabbitServiceCreator.setCloudCacheConfiguration(cloudCacheConfiguration);
	}

	@Override
	public Class<?> getObjectType() {
		return ConnectionFactory.class;
	}
}
