package org.cloudfoundry.reconfiguration.messaging;

import java.util.List;
import java.util.Map;

import org.cloudfoundry.reconfiguration.AbstractServiceConfigurer;
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
public class RabbitConfigurer extends AbstractServiceConfigurer<RabbitServiceCreator> {

	private static final String CF_RABBIT_CONN_FACTORY_NAME = "__cloudFoundryRabbitConnectionFactory";

	private static final String RABBIT_CONN_FACTORY_CLASS_NAME = "org.springframework.amqp.rabbit.connection.ConnectionFactory";

	public RabbitConfigurer(List<Map<String, Object>> cloudServices, RabbitServiceCreator serviceCreator) {
		super(cloudServices, serviceCreator);
	}

	@Override
	public String getServiceLabel() {
		return "rabbitmq";
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
