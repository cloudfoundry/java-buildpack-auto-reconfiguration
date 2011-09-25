package org.cloudfoundry.runtime.service.messaging;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
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
		super(RabbitServiceInfo.class,cloudEnvironment);
		this.rabbitServiceCreator = new RabbitServiceCreator();
	}

	public CloudRabbitConnectionFactoryBean() {
		this(new CloudEnvironment());
	}

	public void setCloudRabbitConfiguration(CloudRabbitConfiguration cloudRabbitConfiguration) {
		this.rabbitServiceCreator.setCloudRabbitConfiguration(cloudRabbitConfiguration);
	}

	@Override
	public Class<?> getObjectType() {
		return ConnectionFactory.class;
	}

	@Override
	protected AbstractServiceCreator<ConnectionFactory, RabbitServiceInfo> getServiceCreator() {
		return this.rabbitServiceCreator;
	}
}
