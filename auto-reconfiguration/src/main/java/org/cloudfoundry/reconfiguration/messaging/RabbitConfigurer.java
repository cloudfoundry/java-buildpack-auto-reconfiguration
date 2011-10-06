package org.cloudfoundry.reconfiguration.messaging;

import org.cloudfoundry.reconfiguration.AbstractServiceConfigurer;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.cloudfoundry.runtime.service.messaging.RabbitServiceCreator;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

/**
 * Implementation of {@link AbstractServiceConfigurer} that replaces a single
 * Rabbit {@link ConnectionFactory} with one connecting to a rabbit cloud
 * service bound to the current application.
 *
 * @author Jennifer Hickey
 *
 */
public class RabbitConfigurer extends AbstractServiceConfigurer<RabbitServiceInfo> {

	private static final String CF_RABBIT_CONN_FACTORY_NAME = "__cloudFoundryRabbitConnectionFactory";

	private static final String RABBIT_CONN_FACTORY_CLASS_NAME = "org.springframework.amqp.rabbit.connection.ConnectionFactory";

	public RabbitConfigurer(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, RabbitServiceInfo.class);
	}

	@Override
	public String getBeanClass() {
		return RABBIT_CONN_FACTORY_CLASS_NAME;
	}

	@Override
	public String getServiceBeanName() {
		return CF_RABBIT_CONN_FACTORY_NAME;
	}

	@Override
	public AbstractServiceCreator<?, RabbitServiceInfo> getServiceCreator() {
		return new RabbitServiceCreator();
	}
}
