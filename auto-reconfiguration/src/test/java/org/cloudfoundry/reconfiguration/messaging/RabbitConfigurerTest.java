package org.cloudfoundry.reconfiguration.messaging;

import static org.junit.Assert.assertFalse;

import org.apache.commons.lang3.tuple.Pair;
import org.cloudfoundry.reconfiguration.AbstractCloudConfigurerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.common.RabbitServiceInfo;
import org.springframework.cloud.util.UriInfo;
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
 * @author Ramnivas Laddad
 *
 */
public class RabbitConfigurerTest extends AbstractCloudConfigurerTest {
    private RabbitServiceInfo createRabbitServiceInfo(String id, String host, int port, String user, String password, String virtualHost) {
        UriInfo uriInfo = new UriInfo("amqp", host, port, user, password, virtualHost);
        return new RabbitServiceInfo(id, uriInfo.getUri().toString());
        
    }
	@Test
	public void cloudRabbitConnFactoryReplacesUserConnFactory() {
	    RabbitServiceInfo rabbitServiceInfo 
	        = createRabbitServiceInfo("my-rabbit", "cloudhost", 1234, "clouduser", "cloudpass", "cloudvirt");

	    ApplicationContext context = getTestApplicationContext("test-rabbit-context.xml", rabbitServiceInfo);
		CachingConnectionFactory replacedConnFactory = 
		        (CachingConnectionFactory) context.getBean("rabbitConnectionFactory", ConnectionFactory.class);
		Assert.assertEquals("cloudhost", replacedConnFactory.getHost());
		Assert.assertEquals("cloudvirt", replacedConnFactory.getVirtualHost());
		Assert.assertEquals(1234, replacedConnFactory.getPort());
	}

	@Test
	public void cloudRabbitConnFactoryLeavesOriginalInPlaceIfMultipleBeansDetected() {
        RabbitServiceInfo rabbitServiceInfo 
            = createRabbitServiceInfo("my-rabbit", "cloudhost", 1234, "clouduser", "cloudpass", "cloudvirt");

		ApplicationContext context = getTestApplicationContext("test-multiple-rabbit-context.xml", rabbitServiceInfo);
		CachingConnectionFactory replacedConnFactory 
		    = (CachingConnectionFactory) context.getBean("rabbitConnectionFactory", ConnectionFactory.class);
		Assert.assertEquals("localhost", replacedConnFactory.getHost());
		Assert.assertEquals("virthost", replacedConnFactory.getVirtualHost());
		Assert.assertEquals(1238, replacedConnFactory.getPort());
	}

	@Test
	public void cloudRabbitConnFactoryLeavesOriginalInPlaceIfMultipleServicesDetected() {
        RabbitServiceInfo rabbitServiceInfo1 
            = createRabbitServiceInfo("my-rabbit1", "cloudhost", 1234, "clouduser", "cloudpass", "cloudvirt");
        RabbitServiceInfo rabbitServiceInfo2 
            = createRabbitServiceInfo("my-rabbit2", "cloudhost", 1234, "clouduser", "cloudpass", "cloudvirt");

        ApplicationContext context = getTestApplicationContext("test-rabbit-context.xml", rabbitServiceInfo1, rabbitServiceInfo2);
		CachingConnectionFactory replacedConnFactory 
		    = (CachingConnectionFactory) context.getBean("rabbitConnectionFactory", ConnectionFactory.class);
		Assert.assertEquals("localhost", replacedConnFactory.getHost());
		Assert.assertEquals("virthost", replacedConnFactory.getVirtualHost());
		Assert.assertEquals(1238, replacedConnFactory.getPort());
	}

	@Test
	public void cloudRabbitConnFactoryLeavesOriginalInPlaceIfNoServicesDetected() {
		ApplicationContext context = getTestApplicationContext("test-rabbit-context.xml");
		CachingConnectionFactory replacedConnFactory 
		    = (CachingConnectionFactory) context.getBean("rabbitConnectionFactory", ConnectionFactory.class);
		Assert.assertEquals("localhost", replacedConnFactory.getHost());
		Assert.assertEquals("virthost", replacedConnFactory.getVirtualHost());
		Assert.assertEquals(1238, replacedConnFactory.getPort());
	}

	@Test
	public void doesNothingIfRabbitConnFactoryClassNotFound() {
        // Set up such that conditions match auto-reconfig requirements (except for what subclasses configuer does)
        RabbitServiceInfo rabbitServiceInfo 
            = createRabbitServiceInfo("my-rabbit", "cloudhost", 1234, "clouduser", "cloudpass", "cloudvirt");

        Pair<Cloud, DefaultListableBeanFactory> cloudAndBeanFactory = getCloudAndBeanFactory("test-rabbit-context.xml", rabbitServiceInfo);
        
        RabbitConfigurer configurer = new CachingConnectionFactoryNotFoundRabbitConfigurer(cloudAndBeanFactory.getLeft());
        assertFalse(configurer.configure(cloudAndBeanFactory.getRight()));
	}

	private class CachingConnectionFactoryNotFoundRabbitConfigurer extends RabbitConfigurer {
		public CachingConnectionFactoryNotFoundRabbitConfigurer(Cloud cloud) {
			super(cloud);
		}

		@Override
		protected Class<?> loadClass(String name) {
			return null;
		}
	}
}
