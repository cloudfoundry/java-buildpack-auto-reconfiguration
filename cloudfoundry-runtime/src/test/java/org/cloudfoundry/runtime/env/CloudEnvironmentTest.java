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
 *
 */
public class CloudEnvironmentTest {

	private static final String VCAP_APPLICATION = "{\"instance_id\":\"1609980ef97169685a4ce7041e7d90bd\",\"instance_index\":0,\"name\":\"helloworld\",\"uris\":[\"helloworld.cloudfoundry.com\"],\"users\":[\"me@example.com\"],\"version\":\"424ee00ced1ce8faf7347353eaf36ac11d73228d-1\",\"start\":\"2011-06-16 23:31:51 +0000\",\"runtime\":\"java\",\"state_timestamp\":1308267111,\"port\":8928,\"limits\":{\"fds\":256,\"mem\":536870912,\"disk\":2147483648},\"host\":\"172.30.49.201\"}";
	private static final String VCAP_SERVICES_EMPTY = "{}";
	private static final String VCAP_SERVICES_MYSQL = "{\"mysql-5.1\":[{\"name\":\"mydb\",\"label\":\"mysql-5.1\",\"plan\":\"free\",\"credentials\":{\"node_id\":\"mysql_node_4\",\"hostname\":\"172.30.48.23\",\"port\":3306,\"password\":\"pCHzhL2btOepb\",\"name\":\"d3caf897662dc43bfb3fd9791ad6d0427\",\"user\":\"uSnCB4uAVBaYr\"}}]}";
	private static final String VCAP_SERVICES_MYSQL_x2 = "{\"mysql-5.1\":[{\"name\":\"mydb_alt\",\"label\":\"mysql-5.1\",\"plan\":\"free\",\"credentials\":{\"node_id\":\"mysql_node_5\",\"hostname\":\"172.30.48.24\",\"port\":3306,\"password\":\"pKQ4YarAb84q9\",\"name\":\"dd6f33259ca354015bb78b88e13c60228\",\"user\":\"uKXaFDUYh1mW2\"}},{\"name\":\"mydb\",\"label\":\"mysql-5.1\",\"plan\":\"free\",\"credentials\":{\"node_id\":\"mysql_node_4\",\"hostname\":\"172.30.48.23\",\"port\":3306,\"password\":\"pCHzhL2btOepb\",\"name\":\"d3caf897662dc43bfb3fd9791ad6d0427\",\"user\":\"uSnCB4uAVBaYr\"}}]}";
	private static final String VCAP_SERVICES_MYSQL_x2_VERSIONS = "{\"mysql-5.1\":[{\"name\":\"mydb_alt\",\"label\":\"mysql-5.1\",\"plan\":\"free\",\"credentials\":{\"node_id\":\"mysql_node_5\",\"hostname\":\"172.30.48.24\",\"port\":3306,\"password\":\"pKQ4YarAb84q9\",\"name\":\"dd6f33259ca354015bb78b88e13c60228\",\"user\":\"uKXaFDUYh1mW2\"}}],\"mysql-5.5\":[{\"name\":\"mydb\",\"label\":\"mysql-5.5\",\"plan\":\"free\",\"credentials\":{\"node_id\":\"mysql_node_4\",\"hostname\":\"172.30.48.23\",\"port\":3306,\"password\":\"pCHzhL2btOepb\",\"name\":\"d3caf897662dc43bfb3fd9791ad6d0427\",\"user\":\"uSnCB4uAVBaYr\"}}]}";
	private static final String VCAP_SERVICES_REDIS_MONGO = "{\"redis-2.2\":[{\"name\":\"mykey\",\"label\":\"redis-2.2\",\"plan\":\"free\",\"credentials\":{\"node_id\":\"redis_node_8\",\"hostname\":\"172.30.48.47\",\"port\":5040,\"password\":\"428045b9-530d-40f3-9e31-6225b3a71c22\",\"name\":\"redis-6601fa58-5b01-4a0c-9546-8af050d8dc62\"}}],\"mongodb-1.8\":[{\"name\":\"mydoc\",\"label\":\"mongodb-1.8\",\"plan\":\"free\",\"credentials\":{\"hostname\":\"172.30.48.61\",\"port\":25070,\"username\":\"459bd2b6-70c3-4427-96d2-7ccc6040be66\",\"password\":\"e5e31a11-593b-4d4a-ae22-85565828b7bf\",\"name\":\"mongodb-cfe09359-0963-47d2-91cb-a2c1216dbfb0\",\"db\":\"db\"}}]}";
	private static final String VCAP_SERVICES_REDIS_MONGO_NAME_IS_TYPE = "{\"redis-2.2\":[{\"name\":\"mongodb\",\"label\":\"redis-2.2\",\"plan\":\"free\",\"credentials\":{\"node_id\":\"redis_node_8\",\"hostname\":\"172.30.48.47\",\"port\":5040,\"password\":\"428045b9-530d-40f3-9e31-6225b3a71c22\",\"name\":\"redis-6601fa58-5b01-4a0c-9546-8af050d8dc62\"}}],\"mongodb-1.8\":[{\"name\":\"redis\",\"label\":\"mongodb-1.8\",\"plan\":\"free\",\"credentials\":{\"hostname\":\"172.30.48.61\",\"port\":25070,\"username\":\"459bd2b6-70c3-4427-96d2-7ccc6040be66\",\"password\":\"e5e31a11-593b-4d4a-ae22-85565828b7bf\",\"name\":\"mongodb-cfe09359-0963-47d2-91cb-a2c1216dbfb0\",\"db\":\"db\"}}]}";

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

		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(VCAP_APPLICATION);
		assertTrue(testRuntime.isCloudFoundry());
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
		String database = "mongo-db";
		
		when(mockEnvironment.getValue("VCAP_SERVICES"))
			.thenReturn(getServicesPayload(null,
										   null,
										   new String[]{getMongoServicePayload(serviceName, hostname, port, username, password, database)},
										   null));
		MongoServiceInfo info = testRuntime.getServiceInfo(serviceName, MongoServiceInfo.class);
		assertEquals(serviceName, info.getServiceName());
		assertEquals(hostname, info.getHost());
		assertEquals(port, info.getPort());
		assertEquals(hostname, info.getHost());
		assertEquals(password, info.getPassword());
		assertEquals(database, info.getDatabase());
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
	public void getServiceSRSInfoRabbit() {
		String serviceName = "rabbit-1";
		String url = "amqp://username:password@127.0.0.1:12345/virtualHost";

		when(mockEnvironment.getValue("VCAP_SERVICES"))
			.thenReturn(getServicesPayload(null,
										   null,
										   null,
										   new String[]{getRabbitSRSServicePayload(serviceName, url)}));
		RabbitServiceInfo info = testRuntime.getServiceInfo(serviceName, RabbitServiceInfo.class);
		assertEquals(serviceName, info.getServiceName());
		assertEquals("username", info.getUserName());
		assertEquals("password", info.getPassword());
		assertEquals("127.0.0.1", info.getHost());
		assertEquals(12345, info.getPort());
		assertEquals("virtualHost", info.getVirtualHost());
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

	@Test
	public void getCloudProperties_core() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(VCAP_APPLICATION);
		when(mockEnvironment.getValue("VCAP_SERVICES")).thenReturn(VCAP_SERVICES_EMPTY);

		Properties cloudProperties = testRuntime.getCloudProperties();

		assertEquals("helloworld", cloudProperties.getProperty("cloud.application.name"));
		assertEquals("cloudfoundry.com", cloudProperties.getProperty("cloud.provider.url"));
	}

	@Test
	public void getCloudProperties_service() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(VCAP_APPLICATION);
		when(mockEnvironment.getValue("VCAP_SERVICES")).thenReturn(VCAP_SERVICES_MYSQL);

		Properties cloudProperties = testRuntime.getCloudProperties();

		// service properties by name
		assertEquals("free", cloudProperties.getProperty("cloud.services.mydb.plan"));
		assertEquals("mysql-5.1", cloudProperties.getProperty("cloud.services.mydb.type"));
		assertEquals("mysql_node_4", cloudProperties.getProperty("cloud.services.mydb.connection.node_id"));
		assertEquals("172.30.48.23", cloudProperties.getProperty("cloud.services.mydb.connection.hostname"));
		assertEquals("3306", cloudProperties.getProperty("cloud.services.mydb.connection.port"));
		assertEquals("pCHzhL2btOepb", cloudProperties.getProperty("cloud.services.mydb.connection.password"));
		assertEquals("d3caf897662dc43bfb3fd9791ad6d0427", cloudProperties.getProperty("cloud.services.mydb.connection.name"));
		assertEquals("uSnCB4uAVBaYr", cloudProperties.getProperty("cloud.services.mydb.connection.user"));

		// service properties by type
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.plan"), cloudProperties.getProperty("cloud.services.mysql.plan"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.type"), cloudProperties.getProperty("cloud.services.mysql.type"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.connection.node_id"), cloudProperties.getProperty("cloud.services.mysql.connection.node_id"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.connection.hostname"), cloudProperties.getProperty("cloud.services.mysql.connection.hostname"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.connection.port"), cloudProperties.getProperty("cloud.services.mysql.connection.port"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.connection.password"), cloudProperties.getProperty("cloud.services.mysql.connection.password"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.connection.name"), cloudProperties.getProperty("cloud.services.mysql.connection.name"));
		assertEquals(cloudProperties.getProperty("cloud.services.mydb.connection.user"), cloudProperties.getProperty("cloud.services.mysql.connection.user"));
	}

	@Test
	public void getCloudProperties_service_multiple() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(VCAP_APPLICATION);
		when(mockEnvironment.getValue("VCAP_SERVICES")).thenReturn(VCAP_SERVICES_REDIS_MONGO);

		Properties cloudProperties = testRuntime.getCloudProperties();

		// service properties by name
		assertEquals("free", cloudProperties.getProperty("cloud.services.mykey.plan"));
		assertEquals("redis-2.2", cloudProperties.getProperty("cloud.services.mykey.type"));
		assertEquals("redis_node_8", cloudProperties.getProperty("cloud.services.mykey.connection.node_id"));
		assertEquals("172.30.48.47", cloudProperties.getProperty("cloud.services.mykey.connection.hostname"));
		assertEquals("5040", cloudProperties.getProperty("cloud.services.mykey.connection.port"));
		assertEquals("428045b9-530d-40f3-9e31-6225b3a71c22", cloudProperties.getProperty("cloud.services.mykey.connection.password"));
		assertEquals("redis-6601fa58-5b01-4a0c-9546-8af050d8dc62", cloudProperties.getProperty("cloud.services.mykey.connection.name"));

		assertEquals("free", cloudProperties.getProperty("cloud.services.mydoc.plan"));
		assertEquals("mongodb-1.8", cloudProperties.getProperty("cloud.services.mydoc.type"));
		assertEquals("172.30.48.61", cloudProperties.getProperty("cloud.services.mydoc.connection.hostname"));
		assertEquals("25070", cloudProperties.getProperty("cloud.services.mydoc.connection.port"));
		assertEquals("459bd2b6-70c3-4427-96d2-7ccc6040be66", cloudProperties.getProperty("cloud.services.mydoc.connection.username"));
		assertEquals("e5e31a11-593b-4d4a-ae22-85565828b7bf", cloudProperties.getProperty("cloud.services.mydoc.connection.password"));
		assertEquals("mongodb-cfe09359-0963-47d2-91cb-a2c1216dbfb0", cloudProperties.getProperty("cloud.services.mydoc.connection.name"));
		assertEquals("db", cloudProperties.getProperty("cloud.services.mydoc.connection.db"));

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
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(VCAP_APPLICATION);
		when(mockEnvironment.getValue("VCAP_SERVICES")).thenReturn(VCAP_SERVICES_MYSQL_x2);

		Properties cloudProperties = testRuntime.getCloudProperties();

		// service properties by name
		assertEquals("free", cloudProperties.getProperty("cloud.services.mydb.plan"));
		assertEquals("mysql-5.1", cloudProperties.getProperty("cloud.services.mydb.type"));
		assertEquals("mysql_node_4", cloudProperties.getProperty("cloud.services.mydb.connection.node_id"));
		assertEquals("172.30.48.23", cloudProperties.getProperty("cloud.services.mydb.connection.hostname"));
		assertEquals("3306", cloudProperties.getProperty("cloud.services.mydb.connection.port"));
		assertEquals("pCHzhL2btOepb", cloudProperties.getProperty("cloud.services.mydb.connection.password"));
		assertEquals("d3caf897662dc43bfb3fd9791ad6d0427", cloudProperties.getProperty("cloud.services.mydb.connection.name"));
		assertEquals("uSnCB4uAVBaYr", cloudProperties.getProperty("cloud.services.mydb.connection.user"));

		assertEquals("free", cloudProperties.getProperty("cloud.services.mydb_alt.plan"));
		assertEquals("mysql-5.1", cloudProperties.getProperty("cloud.services.mydb_alt.type"));
		assertEquals("mysql_node_5", cloudProperties.getProperty("cloud.services.mydb_alt.connection.node_id"));
		assertEquals("172.30.48.24", cloudProperties.getProperty("cloud.services.mydb_alt.connection.hostname"));
		assertEquals("3306", cloudProperties.getProperty("cloud.services.mydb_alt.connection.port"));
		assertEquals("pKQ4YarAb84q9", cloudProperties.getProperty("cloud.services.mydb_alt.connection.password"));
		assertEquals("dd6f33259ca354015bb78b88e13c60228", cloudProperties.getProperty("cloud.services.mydb_alt.connection.name"));
		assertEquals("uKXaFDUYh1mW2", cloudProperties.getProperty("cloud.services.mydb_alt.connection.user"));

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
	public void getCloudProperties_service_multipleDifferentVersion() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(VCAP_APPLICATION);
		when(mockEnvironment.getValue("VCAP_SERVICES")).thenReturn(VCAP_SERVICES_MYSQL_x2_VERSIONS);

		Properties cloudProperties = testRuntime.getCloudProperties();

		// service properties by name
		assertEquals("free", cloudProperties.getProperty("cloud.services.mydb.plan"));
		assertEquals("mysql-5.5", cloudProperties.getProperty("cloud.services.mydb.type"));
		assertEquals("mysql_node_4", cloudProperties.getProperty("cloud.services.mydb.connection.node_id"));
		assertEquals("172.30.48.23", cloudProperties.getProperty("cloud.services.mydb.connection.hostname"));
		assertEquals("3306", cloudProperties.getProperty("cloud.services.mydb.connection.port"));
		assertEquals("pCHzhL2btOepb", cloudProperties.getProperty("cloud.services.mydb.connection.password"));
		assertEquals("d3caf897662dc43bfb3fd9791ad6d0427", cloudProperties.getProperty("cloud.services.mydb.connection.name"));
		assertEquals("uSnCB4uAVBaYr", cloudProperties.getProperty("cloud.services.mydb.connection.user"));

		assertEquals("free", cloudProperties.getProperty("cloud.services.mydb_alt.plan"));
		assertEquals("mysql-5.1", cloudProperties.getProperty("cloud.services.mydb_alt.type"));
		assertEquals("mysql_node_5", cloudProperties.getProperty("cloud.services.mydb_alt.connection.node_id"));
		assertEquals("172.30.48.24", cloudProperties.getProperty("cloud.services.mydb_alt.connection.hostname"));
		assertEquals("3306", cloudProperties.getProperty("cloud.services.mydb_alt.connection.port"));
		assertEquals("pKQ4YarAb84q9", cloudProperties.getProperty("cloud.services.mydb_alt.connection.password"));
		assertEquals("dd6f33259ca354015bb78b88e13c60228", cloudProperties.getProperty("cloud.services.mydb_alt.connection.name"));
		assertEquals("uKXaFDUYh1mW2", cloudProperties.getProperty("cloud.services.mydb_alt.connection.user"));

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
	public void getCloudProperties_service_nameTypeCollision() {
		when(mockEnvironment.getValue("VCAP_APPLICATION")).thenReturn(VCAP_APPLICATION);
		when(mockEnvironment.getValue("VCAP_SERVICES")).thenReturn(VCAP_SERVICES_REDIS_MONGO_NAME_IS_TYPE);

		Properties cloudProperties = testRuntime.getCloudProperties();

		// mongodb service is named 'redis'; redis service is named 'mongodb'
		assertEquals("mongodb-1.8", cloudProperties.getProperty("cloud.services.redis.type"));
		assertEquals("redis-2.2", cloudProperties.getProperty("cloud.services.mongodb.type"));
	}

}
