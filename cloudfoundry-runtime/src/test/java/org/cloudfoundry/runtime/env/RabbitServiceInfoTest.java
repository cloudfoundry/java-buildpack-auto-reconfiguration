package org.cloudfoundry.runtime.env;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class RabbitServiceInfoTest {
	private static void checkRabbitServiceInfo(String uri, String host, int port, String vhost, String user, String pass)
		throws Exception
	{
		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("url", uri);

		Map<String, Object> serviceData = new HashMap<String, Object>();
		serviceData.put("credentials", credentials);

		RabbitServiceInfo serviceInfo = new RabbitServiceInfo(serviceData);
		Assert.assertEquals(host, serviceInfo.getHost());
		Assert.assertEquals(port, serviceInfo.getPort());
		Assert.assertEquals(vhost, serviceInfo.getVirtualHost());
		Assert.assertEquals(user, serviceInfo.getUserName());
		Assert.assertEquals(pass, serviceInfo.getPassword());
	}

	@Test
	public void parseSimpleAmqpUri() throws Exception {
		checkRabbitServiceInfo("amqp://dev.rabbitmq.com", "dev.rabbitmq.com", 5672, "/", "guest", "guest");
	}

	@Test
	public void parseAmqpUriWithPort() throws Exception {
		checkRabbitServiceInfo("amqp://dev.rabbitmq.com:1234", "dev.rabbitmq.com", 1234, "/", "guest", "guest");
	}

	@Test
	public void parseAmqpUriWithUser() throws Exception {
		checkRabbitServiceInfo("amqp://user:pass@dev.rabbitmq.com", "dev.rabbitmq.com", 5672, "/", "user", "pass");
	}

	@Test
	public void parseAmqpUriWithEmptyVhost() throws Exception {
		checkRabbitServiceInfo("amqp://dev.rabbitmq.com/", "dev.rabbitmq.com", 5672, "", "guest", "guest");
	}

	@Test
	public void parseAmqpUriWithSlashedVhost() throws Exception {
		checkRabbitServiceInfo("amqp://dev.rabbitmq.com/%2Fvault", "dev.rabbitmq.com", 5672, "/vault", "guest", "guest");
	}

	@Test
	public void parseAmqpUriWithBadPath() throws Exception {
		try {
			checkRabbitServiceInfo("amqp://dev.rabbitmq.com/a/path/with/slashes", "dev.rabbitmq.com", 5672, "/", "guest", "guest");
			Assert.fail();
		}
		catch (IllegalArgumentException ex) {
		}
	}
}