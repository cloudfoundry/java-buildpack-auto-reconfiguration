package org.cloudfoundry.runtime.env;

import static org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudEnvironment.EnvironmentAccessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test for CloudEnvironment
 * 
 * @author Ramnivas Laddad
 *
 */
public class CloudEnvironmentTest {
	@Mock EnvironmentAccessor mockEnvironment;
	private CloudEnvironment testRuntime = new CloudEnvironment();

	private static final String hostname = "10.20.30.40";
	private static final int port = 1234;
	private static String username = "myuser";
	private static final String password = "mypass";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		testRuntime.setCloudEnvironment(mockEnvironment);
	}
	
	@Test
	public void getServiceInfoRedis() {
		String serviceName = "redis-1";
		
		when(mockEnvironment.getValue("VCAP_SERVICES"))
			.thenReturn(getServicesPayload(null,
										   new String[]{getRedisServicePayload(serviceName, hostname, port, password, "r1")},
										   null,
										   null));
		RedisServiceInfo info = testRuntime.getServiceInfo("redis-1", RedisServiceInfo.class);
		assertEquals(serviceName, info.getServiceName());
		assertEquals(hostname, info.getHost());
		assertEquals(port, info.getPort());
	}

	@Test
	public void getServiceInfoMongo() {
		String serviceName = "mongo-1";
		
		when(mockEnvironment.getValue("VCAP_SERVICES"))
			.thenReturn(getServicesPayload(null,
										   null,
										   new String[]{getMongoServicePayload(serviceName, hostname, port, username, password, "m1")},
										   null));
		MongoServiceInfo info = testRuntime.getServiceInfo(serviceName, MongoServiceInfo.class);
		assertEquals(serviceName, info.getServiceName());
		assertEquals(hostname, info.getHost());
		assertEquals(port, info.getPort());
	}

	@Test
	public void getServiceInfoMysql() {
		when(mockEnvironment.getValue("VCAP_SERVICES"))
			.thenReturn(getServicesPayload(new String[]{getMysqlServicePayload("mysql-1", hostname, port, username, password, "database-123")},
										   null,
										   null,
										   null));
		MysqlServiceInfo info = testRuntime.getServiceInfo("mysql-1", MysqlServiceInfo.class);
		assertEquals("mysql-1", info.getServiceName());
		assertEquals("jdbc:mysql://"+ hostname + ":" + port + "/database-123", info.getUrl());
		assertEquals(username, info.getUserName());
		assertEquals(password, info.getPassword());
	}
	
	@Test
	public void getServiceInfoPostgresql() {
		when(mockEnvironment.getValue("VCAP_SERVICES"))
		    .thenReturn(getServicesPayload(new String[]{getPostgreSQLServicePayload("postgresql-1", hostname, port, username, password, "db-123")},
		    		null,
		    		null,
		    		null));
		PostgresqlServiceInfo info = testRuntime.getServiceInfo("postgresql-1", PostgresqlServiceInfo.class);
		assertEquals("postgresql-1", info.getServiceName());
		assertEquals("jdbc:postgresql://" + hostname + ":" + port + "/db-123", info.getUrl());
		assertEquals(username, info.getUserName());
		assertEquals(password, info.getPassword());
	}

	@Test
	public void getServiceInfoRabbit() {
		String serviceName = "rabbit-1";
		String vHost = "vhost-1";
		
		when(mockEnvironment.getValue("VCAP_SERVICES"))
			.thenReturn(getServicesPayload(null,
										   null,
										   null,
										   new String[]{getRabbitServicePayload(serviceName, hostname, port, username, password, "r1", vHost)}));
		RabbitServiceInfo info = testRuntime.getServiceInfo(serviceName, RabbitServiceInfo.class);
		assertEquals(serviceName, info.getServiceName());
		assertEquals(hostname, info.getHost());
		assertEquals(vHost, info.getVirtualHost());
		assertEquals(port, info.getPort());
	}

	@Test
	public void getCloudApiUri() {
		String appInfo = getApplicationInstanceInfo("\"dashboard.vcloudlabs.com\",\"foo.vcloudlabs.com\"");
		when(mockEnvironment.getValue("VCAP_APPLICATION"))
			.thenReturn(appInfo);
		assertEquals("api.vcloudlabs.com", testRuntime.getCloudApiUri());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void getCloudApiUriNonVcap() {
		when(mockEnvironment.getValue("VCAP_APPLICATION"))
			.thenReturn("");
		assertEquals("api.vcloudlabs.com", testRuntime.getCloudApiUri());
	}

	@Test
	public void operatingInNonVcap() {
		when(mockEnvironment.getValue("VCAP_APPLICATION"))
			.thenReturn("");
		assertEquals(null, testRuntime.getInstanceInfo());
	}
	
}
