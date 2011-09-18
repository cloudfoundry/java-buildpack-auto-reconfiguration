package org.cloudfoundry.reconfiguration.messaging;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.reconfiguration.CloudEnvironmentMockingTest;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.messaging.RabbitServiceCreator;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * Test of {@link RabbitConfigurer}
 * <p>
 * Integration tests bootstrap application contexts containing various Rabbit
 * {@link ConnectionFactory} beans and check if the actual beans got
 * replaced/not replaced with the mock beans.
 * <p>
 * Unit test verifies auto-reconfig behavior when the Rabbit
 * {@link ConnectionFactory} class is not found on the classpath
 *
 *
 * @author Jennifer Hickey
 *
 */
public class RabbitConfigurerTest extends CloudEnvironmentMockingTest {
	@Mock
	private RabbitServiceInfo mockRabbitServiceInfo;
	@Mock
	private RabbitServiceInfo mockRabbitServiceInfo2;

	@Mock
	private DefaultListableBeanFactory beanFactory;

	@Mock
	private RabbitServiceCreator serviceCreator;

	@Test
	public void cloudRabbitConnFactoryReplacesUserConnFactory() {
		List<RabbitServiceInfo> serviceInfos = new ArrayList<RabbitServiceInfo>();
		serviceInfos.add(mockRabbitServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "rabbitmq-2.4.1");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		when(mockRabbitServiceInfo.getHost()).thenReturn("cloudhost");
		when(mockRabbitServiceInfo.getPassword()).thenReturn("mypass");
		when(mockRabbitServiceInfo.getPort()).thenReturn(1234);
		when(mockRabbitServiceInfo.getUserName()).thenReturn("clouduser");
		when(mockRabbitServiceInfo.getVirtualHost()).thenReturn("cloudvirt");
		when(mockEnvironment.getServiceInfos(RabbitServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-rabbit-context.xml");
		CachingConnectionFactory replacedConnFactory = (CachingConnectionFactory) context.getBean(
				"rabbitConnectionFactory", ConnectionFactory.class);
		Assert.assertEquals("cloudhost", replacedConnFactory.getHost());
		Assert.assertEquals("cloudvirt", replacedConnFactory.getVirtualHost());
		Assert.assertEquals(1234, replacedConnFactory.getPort());
	}

	@Test
	public void cloudRabbitConnFactoryLeavesOriginalInPlaceIfMultipleBeansDetected() {
		List<RabbitServiceInfo> serviceInfos = new ArrayList<RabbitServiceInfo>();
		serviceInfos.add(mockRabbitServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "rabbitmq-2.4.1");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		when(mockEnvironment.getServiceInfos(RabbitServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-multiple-rabbit-context.xml");
		CachingConnectionFactory replacedConnFactory = (CachingConnectionFactory) context.getBean(
				"rabbitConnectionFactory", ConnectionFactory.class);
		Assert.assertEquals("localhost", replacedConnFactory.getHost());
		Assert.assertEquals("virthost", replacedConnFactory.getVirtualHost());
		Assert.assertEquals(1238, replacedConnFactory.getPort());
	}

	@Test
	public void cloudRabbitConnFactoryLeavesOriginalInPlaceIfMultipleServicesDetected() {
		List<RabbitServiceInfo> serviceInfos = new ArrayList<RabbitServiceInfo>();
		serviceInfos.add(mockRabbitServiceInfo);
		serviceInfos.add(mockRabbitServiceInfo2);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "rabbitmq-2.4.1");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		when(mockEnvironment.getServiceInfos(RabbitServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);
		ApplicationContext context = getTestApplicationContext("test-rabbit-context.xml");
		CachingConnectionFactory replacedConnFactory = (CachingConnectionFactory) context.getBean(
				"rabbitConnectionFactory", ConnectionFactory.class);
		Assert.assertEquals("localhost", replacedConnFactory.getHost());
		Assert.assertEquals("virthost", replacedConnFactory.getVirtualHost());
		Assert.assertEquals(1238, replacedConnFactory.getPort());
	}

	@Test
	public void cloudRabbitConnFactoryLeavesOriginalInPlaceIfNoServicesDetected() {
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		when(mockEnvironment.getServices()).thenReturn(serviceList);
		ApplicationContext context = getTestApplicationContext("test-rabbit-context.xml");
		CachingConnectionFactory replacedConnFactory = (CachingConnectionFactory) context.getBean(
				"rabbitConnectionFactory", ConnectionFactory.class);
		Assert.assertEquals("localhost", replacedConnFactory.getHost());
		Assert.assertEquals("virthost", replacedConnFactory.getVirtualHost());
		Assert.assertEquals(1238, replacedConnFactory.getPort());
	}

	@Test
	public void doesNothingIfRabbitConnFactoryClassNotFound() {
		RabbitConfigurer configurer = new StubRabbitConfigurer(new ArrayList<Map<String, Object>>(), serviceCreator);
		assertFalse(configurer.configure(beanFactory));
	}

	private class StubRabbitConfigurer extends RabbitConfigurer {
		public StubRabbitConfigurer(List<Map<String, Object>> cloudServices, RabbitServiceCreator serviceCreator) {
			super(cloudServices, serviceCreator);
		}

		@Override
		protected Class<?> loadClass(String name) {
			return null;
		}
	}
}
