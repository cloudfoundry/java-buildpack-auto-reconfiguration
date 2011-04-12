package org.cloudfoundry.runtime.service.messaging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.messaging.CloudRabbitConnectionFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

public class CloudRabbitConnectionFactoryBeanTest {
	@Mock private CloudEnvironment mockRuntime;
	@Mock private RabbitServiceInfo mockServiceInfo;
	private CloudRabbitConnectionFactoryBean factory;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		factory = new CloudRabbitConnectionFactoryBean(mockRuntime);
	}
	
	@Test
	public void cloudRabbitCreation() throws Exception {
		when(mockRuntime.getServiceInfo("rabbit-1", RabbitServiceInfo.class))
			.thenReturn(mockServiceInfo);
		when(mockServiceInfo.getHost()).thenReturn("10.20.30.40");
		when(mockServiceInfo.getVirtualHost()).thenReturn("virtual-host-string");
		when(mockServiceInfo.getPort()).thenReturn(10000);
		
		when(mockServiceInfo.getUserName()).thenReturn("myuser");
		when(mockServiceInfo.getPassword()).thenReturn("mypass");
		
		factory.setServiceName("rabbit-1");
		factory.afterPropertiesSet();
		ConnectionFactory cloudConnectionFactory = factory.getObject();
		assertEquals("10.20.30.40", cloudConnectionFactory.getHost());
		assertEquals("virtual-host-string", cloudConnectionFactory.getVirtualHost());
	}

}
