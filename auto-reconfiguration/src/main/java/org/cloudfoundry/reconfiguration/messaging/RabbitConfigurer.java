package org.cloudfoundry.reconfiguration.messaging;

import org.cloudfoundry.reconfiguration.AbstractServiceConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.common.RabbitServiceInfo;

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

	public RabbitConfigurer(Cloud cloud) {
		super(cloud, RabbitServiceInfo.class);
	}

	@Override
	public String getBeanClass() {
		return RABBIT_CONN_FACTORY_CLASS_NAME;
	}

	@Override
	public String getServiceBeanName() {
		return CF_RABBIT_CONN_FACTORY_NAME;
	}
}
