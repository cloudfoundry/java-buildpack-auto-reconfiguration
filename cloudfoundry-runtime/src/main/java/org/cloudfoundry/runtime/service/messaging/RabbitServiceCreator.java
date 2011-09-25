package org.cloudfoundry.runtime.service.messaging;

import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.cloudfoundry.runtime.service.CloudCacheConfiguration;
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

	private CloudCacheConfiguration cloudCacheConfiguration;

	public void setCloudCacheConfiguration(CloudCacheConfiguration cloudCacheConfiguration) {
		this.cloudCacheConfiguration = cloudCacheConfiguration;
	}

	public ConnectionFactory createService(RabbitServiceInfo serviceInfo) {
		CachingConnectionFactory connectionFactory
		    = new CachingConnectionFactory(serviceInfo.getHost());
		connectionFactory.setVirtualHost(serviceInfo.getVirtualHost());
		connectionFactory.setUsername(serviceInfo.getUserName());
		connectionFactory.setPassword(serviceInfo.getPassword());
		connectionFactory.setPort(serviceInfo.getPort());
		if (cloudCacheConfiguration != null) {
			connectionFactory.setChannelCacheSize(cloudCacheConfiguration.getChannelCacheSize());
		}
		return connectionFactory;
	}
}
