package org.cloudfoundry.runtime.service.document;

import static org.mockito.Mockito.when;
import static org.junit.Assert.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.MongoDbFactory;

public class CloudMongoDbFactoryTest {
	@Mock private CloudEnvironment mockRuntime;
	@Mock private MongoServiceInfo mockServiceInfo;
	private CloudMongoDbFactoryBean factory;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		factory = new CloudMongoDbFactoryBean(mockRuntime);
	}

	@Test
	public void cloudMongoCreation() throws Exception {
		when(mockRuntime.getServiceInfo("mongo-1", MongoServiceInfo.class))
			.thenReturn(mockServiceInfo);
		when(mockServiceInfo.getHost()).thenReturn("10.20.30.40");
		when(mockServiceInfo.getPort()).thenReturn(10000);

		when(mockServiceInfo.getServiceName()).thenReturn("mongo-1");
		when(mockServiceInfo.getDatabase()).thenReturn("mongo-database-1");

		when(mockServiceInfo.getUserName()).thenReturn("myuser");
		when(mockServiceInfo.getPassword()).thenReturn("mypass");

		factory.setServiceName("mongo-1");

		factory.afterPropertiesSet();
		MongoDbFactory cloudMongoDbFactory = factory.getObject();
		assertNotNull(cloudMongoDbFactory);
		assertEquals("mongo-database-1", getField(cloudMongoDbFactory, "databaseName"));
		assertEquals("myuser", getField(cloudMongoDbFactory, "username"));
		assertEquals("mypass", getField(cloudMongoDbFactory, "password"));
		// TODO: Need a better way to avoid Mongo trying to connect
	}

}
