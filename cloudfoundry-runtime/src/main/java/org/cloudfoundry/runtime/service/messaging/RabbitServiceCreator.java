package org.cloudfoundry.runtime.service.messaging;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SingleConnectionFactory;

/**
 * Simplified access to creating RabbitMQ service objects.
 * 
 * @author Ramnivas Laddad
 *
 */
public class RabbitServiceCreator 
	extends AbstractServiceCreator<ConnectionFactory, RabbitServiceInfo> {

	public RabbitServiceCreator(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, RabbitServiceInfo.class);
	}
	
	public ConnectionFactory createService(RabbitServiceInfo serviceInfo) {
		SingleConnectionFactory connectionFactory 
		    = new SingleConnectionFactory(serviceInfo.getHost());
		connectionFactory.setVirtualHost(serviceInfo.getVirtualHost());
		connectionFactory.setUsername(serviceInfo.getUserName());
		connectionFactory.setPassword(serviceInfo.getPassword());
		connectionFactory.setPort(serviceInfo.getPort());
		return connectionFactory;
	}

}
