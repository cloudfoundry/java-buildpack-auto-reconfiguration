package org.cloudfoundry.runtime.service.document;

import static org.mockito.Mockito.when;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CloudMongoFactoryTest {
	@Mock private CloudEnvironment mockRuntime;
	@Mock private MongoServiceInfo mockServiceInfo;
	private CloudMongoFactoryBean factory;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		factory = new CloudMongoFactoryBean(mockRuntime);
	}
	
	@Test
	public void cloudMongoCreation() throws Exception {
		when(mockRuntime.getServiceInfo("mongo-1", MongoServiceInfo.class))
			.thenReturn(mockServiceInfo);
		when(mockServiceInfo.getHost()).thenReturn("10.20.30.40");
		when(mockServiceInfo.getPort()).thenReturn(10000);
		when(mockServiceInfo.getPassword()).thenReturn("mypass");
		
		factory.setServiceName("mongo-1");
		
		// TODO: Need a better way to avoid Mongo trying to connect
		
//		factory.afterPropertiesSet();
//		Mongo cloudMongo = factory.getObject();
		
//		assertEquals("10.20.30.40", cloudMongo.getHostName());
//		assertEquals(10000, cloudMongo.getPort());
//		assertEquals("mypass", cloudMongo.getPassword());
	}

}
