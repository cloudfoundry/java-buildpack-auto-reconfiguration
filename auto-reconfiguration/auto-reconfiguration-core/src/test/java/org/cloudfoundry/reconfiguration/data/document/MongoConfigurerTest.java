package org.cloudfoundry.reconfiguration.data.document;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.cloudfoundry.runtime.service.AbstractServiceCreator.ServiceNameTuple;
import org.cloudfoundry.runtime.service.document.MongoServiceCreator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Unit test of {@link MongoConfigurer}.
 * <p>
 * It is not currently possible to integration test the auto-reconfig of Mongo,
 * as there are no property accessors available in {@link SimpleMongoDbFactory},
 * and calling the getDb method will actually attempt to connect to a Mongo
 * instance.
 *
 * @author Jennifer Hickey
 *
 */
public class MongoConfigurerTest {

	@Mock
	private DefaultListableBeanFactory beanFactory;

	@Mock
	private MongoServiceCreator serviceCreator;

	@Mock
	private CloudEnvironment cloudEnvironment;

	@Mock
	private MongoServiceInfo mongoServiceInfo;

	@Mock
	private MongoServiceInfo mongoServiceInfo2;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void replacesUserDbFactory() throws UnknownHostException, MongoException {
		SimpleMongoDbFactory expectedFactory = new SimpleMongoDbFactory(new Mongo("localhost", 1234), "clouddb",
				new UserCredentials("cloudy", "mypass"));
		ServiceNameTuple<MongoDbFactory> expectedService = new ServiceNameTuple<MongoDbFactory>(expectedFactory,
				"mongo-svc");
		String mongoBeanName = "testMongoDb";
		List<MongoServiceInfo> serviceInfos = new ArrayList<MongoServiceInfo>();
		serviceInfos.add(mongoServiceInfo);
		when(cloudEnvironment.getServiceInfos(MongoServiceInfo.class)).thenReturn(serviceInfos);
		MongoConfigurer mongoConfigurer = new StubMongoConfigurer(cloudEnvironment);
		when(beanFactory.getBeanNamesForType(MongoDbFactory.class,true,false)).thenReturn(new String[] { mongoBeanName });
		when(serviceCreator.createSingletonService(mongoServiceInfo)).thenReturn(expectedService);
		assertTrue(mongoConfigurer.configure(beanFactory));
		verify(beanFactory).registerSingleton(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME, expectedFactory);
		verify(beanFactory).removeBeanDefinition(mongoBeanName);
		verify(beanFactory).registerAlias(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME, mongoBeanName);
	}

	@Test
	public void leavesOriginalInPlaceIfErrorCreatingCloudService() throws UnknownHostException, MongoException {
		String mongoBeanName = "testMongoDb";
		List<MongoServiceInfo> serviceInfos = new ArrayList<MongoServiceInfo>();
		serviceInfos.add(mongoServiceInfo);
		when(cloudEnvironment.getServiceInfos(MongoServiceInfo.class)).thenReturn(serviceInfos);
		MongoConfigurer mongoConfigurer = new StubMongoConfigurer(cloudEnvironment);
		when(beanFactory.getBeanNamesForType(MongoDbFactory.class,true,false)).thenReturn(new String[] { mongoBeanName });
		when(serviceCreator.createSingletonService(mongoServiceInfo)).thenThrow(
				new CloudServiceException("Something went wrong"));
		assertFalse(mongoConfigurer.configure(beanFactory));
	}

	@Test
	public void leavesOriginalInPlaceIfMultipleBeansDetected() {
		List<MongoServiceInfo> serviceInfos = new ArrayList<MongoServiceInfo>();
		when(cloudEnvironment.getServiceInfos(MongoServiceInfo.class)).thenReturn(serviceInfos);
		MongoConfigurer mongoConfigurer = new StubMongoConfigurer(cloudEnvironment);
		when(beanFactory.getBeanNamesForType(MongoDbFactory.class,true,false)).thenReturn(new String[] { "bean1", "bean2" });
		assertFalse(mongoConfigurer.configure(beanFactory));
		verify(beanFactory, never()).registerSingleton(eq(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME),
				isA(MongoDbFactory.class));
		verify(beanFactory, never()).removeBeanDefinition(isA(String.class));
		verify(beanFactory, never()).registerAlias(eq(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME), isA(String.class));
	}

	@Test
	public void leavesOriginalInPlaceIfMultipleServicesDetected() {
		String mongoBeanName = "testMongoDb";
		List<MongoServiceInfo> serviceInfos = new ArrayList<MongoServiceInfo>();
		serviceInfos.add(mongoServiceInfo);
		serviceInfos.add(mongoServiceInfo2);
		when(cloudEnvironment.getServiceInfos(MongoServiceInfo.class)).thenReturn(serviceInfos);
		MongoConfigurer mongoConfigurer = new StubMongoConfigurer(cloudEnvironment);
		when(beanFactory.getBeanNamesForType(MongoDbFactory.class,true,false)).thenReturn(new String[] { mongoBeanName });
		assertFalse(mongoConfigurer.configure(beanFactory));
		verify(beanFactory, never()).registerSingleton(eq(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME),
				isA(MongoDbFactory.class));
		verify(beanFactory, never()).removeBeanDefinition(mongoBeanName);
		verify(beanFactory, never()).registerAlias(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME, mongoBeanName);
	}

	@Test
	public void leavesOriginalInPlaceIfNoServicesDetected() {
		String mongoBeanName = "testMongoDb";
		when(beanFactory.getBeanNamesForType(MongoDbFactory.class,true,false)).thenReturn(new String[] { mongoBeanName });
		when(cloudEnvironment.getServiceInfos(MongoServiceInfo.class)).thenReturn(new ArrayList<MongoServiceInfo>());
		MongoConfigurer mongoConfigurer = new StubMongoConfigurer(cloudEnvironment);
		assertFalse(mongoConfigurer.configure(beanFactory));
		verify(beanFactory, never()).registerSingleton(eq(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME),
				isA(MongoDbFactory.class));
		verify(beanFactory, never()).removeBeanDefinition(mongoBeanName);
		verify(beanFactory, never()).registerAlias(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME, mongoBeanName);
	}

	@Test
	public void doesNothingIfNoMongoBeansDetected() {
		MongoConfigurer mongoConfigurer = new StubMongoConfigurer(cloudEnvironment);
		when(beanFactory.getBeanNamesForType(MongoDbFactory.class,true,false)).thenReturn(new String[0]);
		assertFalse(mongoConfigurer.configure(beanFactory));
		verify(beanFactory, never()).registerSingleton(eq(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME),
				isA(MongoDbFactory.class));
		verify(beanFactory, never()).removeBeanDefinition(isA(String.class));
		verify(beanFactory, never()).registerAlias(eq(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME), isA(String.class));
	}

	@Test
	public void doesNothingIfMongoDbFactoryClassNotFound() {
		MongoConfigurer configurer = new StubClassNotFoundMongoConfigurer(cloudEnvironment);
		assertFalse(configurer.configure(beanFactory));
	}

	private class StubMongoConfigurer extends MongoConfigurer {
		public StubMongoConfigurer(CloudEnvironment cloudEnvironment) {
			super(cloudEnvironment);
		}

		@Override
		public AbstractServiceCreator<?, MongoServiceInfo> getServiceCreator() {
			return serviceCreator;
		}
	}

	private class StubClassNotFoundMongoConfigurer extends StubMongoConfigurer {
		public StubClassNotFoundMongoConfigurer(CloudEnvironment cloudEnvironment) {
			super(cloudEnvironment);
		}

		@Override
		protected Class<?> loadClass(String name) {
			return null;
		}
	}

}
