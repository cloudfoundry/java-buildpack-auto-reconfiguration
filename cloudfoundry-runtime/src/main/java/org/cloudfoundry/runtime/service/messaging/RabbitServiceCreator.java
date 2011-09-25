package org.cloudfoundry.runtime.service.messaging;

import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

/**
 * Simplified access to creating RabbitMQ service objects.
 *
 * @author Ramnivas Laddad
 * @author Dave Syer
 * @author Thomas Risberg
 *
 */
public class RabbitServiceCreator
	extends AbstractServiceCreator<ConnectionFactory, RabbitServiceInfo> {

	private CloudRabbitConfiguration cloudRabbitConfiguration;

	public void setCloudRabbitConfiguration(CloudRabbitConfiguration cloudRabbitConfiguration) {
		this.cloudRabbitConfiguration = cloudRabbitConfiguration;
	}

	public ConnectionFactory createService(RabbitServiceInfo serviceInfo) {
		CachingConnectionFactory connectionFactory
		    = new CachingConnectionFactory(serviceInfo.getHost());
		connectionFactory.setVirtualHost(serviceInfo.getVirtualHost());
		connectionFactory.setUsername(serviceInfo.getUserName());
		connectionFactory.setPassword(serviceInfo.getPassword());
		connectionFactory.setPort(serviceInfo.getPort());
		if (cloudRabbitConfiguration != null) {
			connectionFactory.setChannelCacheSize(cloudRabbitConfiguration.getChannelCacheSize());
		}
		return connectionFactory;
	}
}
