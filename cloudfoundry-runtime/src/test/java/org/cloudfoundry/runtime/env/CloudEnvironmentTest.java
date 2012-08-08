package org.cloudfoundry.runtime.env;

import static org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper.getApplicationInstanceInfo;
import static org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper.getMongoServicePayload;
import static org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper.getMysqlServicePayload;
import static org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper.getPostgreSQLServicePayload;
import static org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper.getRabbitSRSServicePayload;
import static org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper.getRabbitServicePayload;
import static org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper.getRedisServicePayload;
import static org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper.getServicesPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.cloudfoundry.runtime.env.CloudEnvironment.EnvironmentAccessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test for CloudEnvironment
 * 
 * @author Ramnivas Laddad
 * @author Scott Andrews
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
	public void isCloudFoundry() {
		assertFalse(testRuntime.isCloudFoundry());

		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(getApplicationInstanceInfo("foobar", "foo.cloudfoundry.com"));
		assertTrue(testRuntime.isCloudFoundry());
	}

	@Test
	public void getServiceInfoRedis() {
		String serviceName = "redis-1";

		String[] versions = {"2.2", "2.4"};
		for (String version : versions) {
			when(mockEnvironment.getValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(null,
											   new String[]{getRedisServicePayload(version, serviceName, hostname, port, password, "r1")},
											   null,
											   null));
			RedisServiceInfo info = testRuntime.getServiceInfo("redis-1", RedisServiceInfo.class);
			assertEquals(serviceName, info.getServiceName());
			assertEquals(hostname, info.getHost());
			assertEquals(port, info.getPort());
		}
	}

	@Test
	public void getServiceInfoMongo() {
		String serviceName = "mongo-1";
		String database = "mongo-db";
		String name = "mongo-name";
		
		String[] versions = {"1.8", "2.0"};
		for (String version : versions) {
			when(mockEnvironment.getValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(null,
											   null,
											   new String[]{getMongoServicePayload(version, serviceName, hostname, port, username, password, database, name)},
											   null));
			MongoServiceInfo info = testRuntime.getServiceInfo(serviceName, MongoServiceInfo.class);
			assertEquals(serviceName, info.getServiceName());
			assertEquals(hostname, info.getHost());
			assertEquals(port, info.getPort());
			assertEquals(hostname, info.getHost());
			assertEquals(password, info.getPassword());
			assertEquals(database, info.getDatabase());
		}
	}

	@Test
	public void getServiceInfoMysql() {
		String[] versions = {"5.1", "5.5"};
		for (String version : versions) {
			when(mockEnvironment.getValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(new String[]{getMysqlServicePayload(version, "mysql-1", hostname, port, username, password, "database-123")},
											   null,
											   null,
											   null));
			RdbmsServiceInfo info = testRuntime.getServiceInfo("mysql-1", RdbmsServiceInfo.class);
			assertEquals("mysql-1", info.getServiceName());
			assertEquals("jdbc:mysql://"+ hostname + ":" + port + "/database-123", info.getUrl());
			assertEquals(username, info.getUserName());
			assertEquals(password, info.getPassword());
		}
	}
	
	@Test
	public void getServiceInfoPostgresql() {
		String[] versions = {"9.0", "9.1"};
		for (String version : versions) {
			when(mockEnvironment.getValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(new String[]{getPostgreSQLServicePayload(version, "postgresql-1", hostname, port, username, password, "db-123")},
						null,
						null,
						null));
			RdbmsServiceInfo info = testRuntime.getServiceInfo("postgresql-1", RdbmsServiceInfo.class);
			assertEquals("postgresql-1", info.getServiceName());
			assertEquals("jdbc:postgresql://" + hostname + ":" + port + "/db-123", info.getUrl());
			assertEquals(username, info.getUserName());
			assertEquals(password, info.getPassword());
		}
	}

	/**
	 * Verifies parsing of credentials in the format of "old"
	 * Rabbit service (still used by chef recipe deployment)
	 */
	@Test
	public void getServiceInfoRabbit() {
		String serviceName = "rabbit-1";
		String vHost = "vhost-1";
		
		String[] versions = {"2.4", "2.6"};
		for (String version : versions) {
			when(mockEnvironment.getValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(null,
											   null,
											   null,
											   new String[]{getRabbitServicePayload(version, serviceName, hostname, port, username, password, "r1", vHost)}));
			RabbitServiceInfo info = testRuntime.getServiceInfo(serviceName, RabbitServiceInfo.class);
			assertEquals(serviceName, info.getServiceName());
			assertEquals(hostname, info.getHost());
			assertEquals(vHost, info.getVirtualHost());
			assertEquals(port, info.getPort());
		}
	}

	/**
	 * Verifies parsing of credentials in the existing Rabbit service
	 */
	@Test
	public void getServiceSRSInfoRabbit() {
		String serviceName = "rabbit-1";
		String url = "amqp://username:password@127.0.0.1:12345/virtualHost";

		String[] versions = {"2.4", "2.6"};
		for (String version : versions) {
			when(mockEnvironment.getValue("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(null,
											   null,
											   null,
											   new String[]{getRabbitSRSServicePayload(version, serviceName, url)}));
			RabbitServiceInfo info = testRuntime.getServiceInfo(serviceName, RabbitServiceInfo.class);
			assertEquals(serviceName, info.getServiceName());
			assertEquals("username", info.getUserName());
			assertEquals("password", info.getPassword());
			assertEquals("127.0.0.1", info.getHost());
			assertEquals(12345, info.getPort());
			assertEquals("virtualHost", info.getVirtualHost());
		}
	}

	@Test
	public void getCloudApiUri() {
		String appInfo = getApplicationInstanceInfo("foobar", "foo.cloudfoundry.com", "bar.notcloudfoundry.com");
		when(mockEnvironment.getValue("VCAP_APPLICATION"))
			.thenReturn(appInfo);
		assertEquals("api.cloudfoundry.com", testRuntime.getCloudApiUri());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void getCloudApiUriNonVcap() {
		when(mockEnvironment.getValue("VCAP_APPLICATION"))
			.thenReturn("");
		testRuntime.getCloudApiUri();
	}

	@Test
	public void operatingInNonVcap() {
		when(mockEnvironment.getValue("VCAP_APPLICATION"))
			.thenReturn("");
		assertEquals(null, testRuntime.getInstanceInfo());
	}

	@Test
	public void getCloudProperties_core() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(getApplicationInstanceInfo("foo", "foo.cloudfoundry.com"));
		when(mockEnvironment.getValue("VCAP_SERVICES")).thenReturn(getServicesPayload(null, null, null, null));

		Properties cloudProperties = testRuntime.getCloudProperties();

		assertEquals("foo", cloudProperties.getProperty("cloud.application.name"));
		assertEquals("cloudfoundry.com", cloudProperties.getProperty("cloud.provider.url"));
	}

	@Test
	public void getCloudProperties_service() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(getApplicationInstanceInfo("foo", "foo.cloudfoundry.com"));
		when(mockEnvironment.getValue("VCAP_SERVICES")).thenReturn(getServicesPayload(
			new String[]{ getMysqlServicePayload("5.1", "mydb", hostname, port, "mydb-user", "mydb-password", "mydb-name")},
			null,
			null,
			null
		));

		Properties cloudProperties = testRuntime.getCloudProperties();

		// service properties by name
		assertEquals("free", cloudProperties.getProperty("cloud.services.mydb.plan"));
		assertEquals("mysql-5.1", cloudProperties.getProperty("cloud.services.mydb.type"));
		assertEquals(hostname, cloudProperties.getProperty("cloud.services.mydb.connection.hostname"));
		assertEquals(Integer.toString(port), cloudProperties.getProperty("cloud.services.mydb.connection.port"));
		assertEquals("mydb-password", cloudProperties.getProperty("cloud.services.mydb.connection.password"));
		assertEquals("mydb-name", cloudProperties.getProperty("cloud.services.mydb.connection.name"));
		assertEquals("mydb-user", cloudProperties.getProperty("cloud.services.mydb.connection.user"));

		// service properties by type
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.plan"), cloudProperties.getProperty("cloud.services.mysql.plan"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.type"), cloudProperties.getProperty("cloud.services.mysql.type"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.connection.hostname"), cloudProperties.getProperty("cloud.services.mysql.connection.hostname"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.connection.port"), cloudProperties.getProperty("cloud.services.mysql.connection.port"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.connection.password"), cloudProperties.getProperty("cloud.services.mysql.connection.password"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.connection.name"), cloudProperties.getProperty("cloud.services.mysql.connection.name"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.connection.user"), cloudProperties.getProperty("cloud.services.mysql.connection.user"));
	}

	@Test
	public void getCloudProperties_service_multiple() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(getApplicationInstanceInfo("foo", "foo.cloudfoundry.com"));
		when(mockEnvironment.getValue("VCAP_SERVICES")).thenReturn(getServicesPayload(
			null,
			new String[]{ getRedisServicePayload("2.2", "mykey", "2.2.0.0", 2200, "redis-password", "redis-name") },
			new String[]{ getMongoServicePayload("1.8", "mydoc", "1.8.0.0", 1800, "mongodb-username", "mongodb-password", "mongodb-db", "mongodb-name") },
			null
		));

		Properties cloudProperties = testRuntime.getCloudProperties();

		// service properties by name
		assertEquals("free", cloudProperties.getProperty("cloud.services.mykey.plan"));
		assertEquals("redis-2.2", cloudProperties.getProperty("cloud.services.mykey.type"));
		assertEquals("2.2.0.0", cloudProperties.getProperty("cloud.services.mykey.connection.hostname"));
		assertEquals("2200", cloudProperties.getProperty("cloud.services.mykey.connection.port"));
		assertEquals("redis-password", cloudProperties.getProperty("cloud.services.mykey.connection.password"));
		assertEquals("redis-name", cloudProperties.getProperty("cloud.services.mykey.connection.name"));

		assertEquals("free", cloudProperties.getProperty("cloud.services.mydoc.plan"));
		assertEquals("mongodb-1.8", cloudProperties.getProperty("cloud.services.mydoc.type"));
		assertEquals("1.8.0.0", cloudProperties.getProperty("cloud.services.mydoc.connection.hostname"));
		assertEquals("1800", cloudProperties.getProperty("cloud.services.mydoc.connection.port"));
		assertEquals("mongodb-username", cloudProperties.getProperty("cloud.services.mydoc.connection.username"));
		assertEquals("mongodb-password", cloudProperties.getProperty("cloud.services.mydoc.connection.password"));
		assertEquals("mongodb-name", cloudProperties.getProperty("cloud.services.mydoc.connection.name"));
		assertEquals("mongodb-db", cloudProperties.getProperty("cloud.services.mydoc.connection.db"));

		// service properties by type
		assertEquals(cloudProperties.getProperty("cloud.services.redis.plan"), cloudProperties.getProperty("cloud.services.mykey.plan"));
		assertEquals(cloudProperties.getProperty("cloud.services.redis.type"), cloudProperties.getProperty("cloud.services.mykey.type"));
		assertEquals(cloudProperties.getProperty("cloud.services.redis.connection.node_id"), cloudProperties.getProperty("cloud.services.mykey.connection.node_id"));
		assertEquals(cloudProperties.getProperty("cloud.services.redis.connection.hostname"), cloudProperties.getProperty("cloud.services.mykey.connection.hostname"));
		assertEquals(cloudProperties.getProperty("cloud.services.redis.connection.port"), cloudProperties.getProperty("cloud.services.mykey.connection.port"));
		assertEquals(cloudProperties.getProperty("cloud.services.redis.connection.password"), cloudProperties.getProperty("cloud.services.mykey.connection.password"));
		assertEquals(cloudProperties.getProperty("cloud.services.redis.connection.name"), cloudProperties.getProperty("cloud.services.mykey.connection.name"));

		assertEquals(cloudProperties.getProperty("cloud.services.mongodb.plan"), cloudProperties.getProperty("cloud.services.mydoc.plan"));
		assertEquals(cloudProperties.getProperty("cloud.services.mongodb.type"), cloudProperties.getProperty("cloud.services.mydoc.type"));
		assertEquals(cloudProperties.getProperty("cloud.services.mongodb.connection.hostname"), cloudProperties.getProperty("cloud.services.mydoc.connection.hostname"));
		assertEquals(cloudProperties.getProperty("cloud.services.mongodb.connection.port"), cloudProperties.getProperty("cloud.services.mydoc.connection.port"));
		assertEquals(cloudProperties.getProperty("cloud.services.mongodb.connection.username"), cloudProperties.getProperty("cloud.services.mydoc.connection.username"));
		assertEquals(cloudProperties.getProperty("cloud.services.mongodb.connection.password"), cloudProperties.getProperty("cloud.services.mydoc.connection.password"));
		assertEquals(cloudProperties.getProperty("cloud.services.mongodb.connection.name"), cloudProperties.getProperty("cloud.services.mydoc.connection.name"));
		assertEquals(cloudProperties.getProperty("cloud.services.mongodb.connection.db"), cloudProperties.getProperty("cloud.services.mydoc.connection.db"));
	}

	@Test
	public void getCloudProperties_service_multipleForType() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(getApplicationInstanceInfo("foo", "foo.cloudfoundry.com"));
		when(mockEnvironment.getValue("VCAP_SERVICES")).thenReturn(getServicesPayload(
			new String[]{
				getMysqlServicePayload("5.1", "mydb", hostname, port, "mydb-user", "mydb-password", "mydb-name"),
				getMysqlServicePayload("5.1", "mydb-alt", hostname, port, "mydbalt-user", "mydbalt-password", "mydbalt-name")
			},
			null,
			null,
			null
		));

		Properties cloudProperties = testRuntime.getCloudProperties();

		// service properties by name
		assertEquals("mysql-5.1", cloudProperties.getProperty("cloud.services.mydb.type"));
		assertEquals("mysql-5.1", cloudProperties.getProperty("cloud.services.mydb-alt.type"));

		// service properties by type
		assertNull(cloudProperties.getProperty("cloud.services.mysql.plan"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.type"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.connection.node_id"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.connection.hostname"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.connection.port"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.connection.password"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.connection.name"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.connection.user"));
	}

	@Test
	public void getCloudProperties_service_multipleForVersion() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(getApplicationInstanceInfo("foo", "foo.cloudfoundry.com"));
		when(mockEnvironment.getValue("VCAP_SERVICES")).thenReturn(getServicesPayload(
			new String[]{
				getMysqlServicePayload("5.1", "mydb", hostname, port, "mydb-user", "mydb-password", "mydb-name"),
				getMysqlServicePayload("5.5", "mydb-alt", hostname, port, "mydbalt-user", "mydbalt-password", "mydbalt-name")
			},
			null,
			null,
			null
		));

		Properties cloudProperties = testRuntime.getCloudProperties();

		// service properties by name
		assertEquals("mysql-5.1", cloudProperties.getProperty("cloud.services.mydb.type"));
		assertEquals("mysql-5.5", cloudProperties.getProperty("cloud.services.mydb-alt.type"));

		// service properties by type
		// since we have multiple service differing only in version, type-based props shouldn't exist
		assertNull(cloudProperties.getProperty("cloud.services.mysql.plan"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.type"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.connection.node_id"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.connection.hostname"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.connection.port"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.connection.password"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.connection.name"));
		assertNull(cloudProperties.getProperty("cloud.services.mysql.connection.user"));
	}

	@Test
	public void getCloudProperties_service_nameTypeCollision() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(getApplicationInstanceInfo("foo", "foo.cloudfoundry.com"));
		when(mockEnvironment.getValue("VCAP_SERVICES")).thenReturn(getServicesPayload(
				null,
				new String[]{ getRedisServicePayload("2.2", "mongodb", hostname, port, "redis-password", "redis-name") },
				new String[]{ getMongoServicePayload("1.8", "redis", hostname, port, "mongodb-username", "mongodb-password", "mongodb-db", "mongodb-name") },
				null
			));

		Properties cloudProperties = testRuntime.getCloudProperties();

		// mongodb service is named 'redis'; redis service is named 'mongodb'
		assertEquals("mongodb-1.8", cloudProperties.getProperty("cloud.services.redis.type"));
		assertEquals("redis-2.2", cloudProperties.getProperty("cloud.services.mongodb.type"));
	}

}
