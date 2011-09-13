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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.runtime.env.CloudServiceException;
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
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mongodb-1.8");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		MongoConfigurer mongoConfigurer = new MongoConfigurer(serviceList, serviceCreator);
		when(beanFactory.getBeanNamesForType(MongoDbFactory.class)).thenReturn(new String[] { mongoBeanName });
		when(serviceCreator.createSingletonService()).thenReturn(expectedService);
		assertTrue(mongoConfigurer.configure(beanFactory));
		verify(beanFactory).registerSingleton(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME, expectedFactory);
		verify(beanFactory).removeBeanDefinition(mongoBeanName);
		verify(beanFactory).registerAlias(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME, mongoBeanName);
	}

	@Test
	public void leavesOriginalInPlaceIfMultipleBeansDetected() {
		MongoConfigurer mongoConfigurer = new MongoConfigurer(new ArrayList<Map<String, Object>>(), serviceCreator);
		when(beanFactory.getBeanNamesForType(MongoDbFactory.class)).thenReturn(new String[] { "bean1", "bean2" });
		assertFalse(mongoConfigurer.configure(beanFactory));
		verify(beanFactory, never()).registerSingleton(eq(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME),
				isA(MongoDbFactory.class));
		verify(beanFactory, never()).removeBeanDefinition(isA(String.class));
		verify(beanFactory, never()).registerAlias(eq(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME), isA(String.class));
	}

	@Test
	public void leavesOriginalInPlaceIfMultipleServicesDetected() {
		String mongoBeanName = "testMongoDb";
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mongodb-1.8");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		MongoConfigurer mongoConfigurer = new MongoConfigurer(serviceList, serviceCreator);
		when(beanFactory.getBeanNamesForType(MongoDbFactory.class)).thenReturn(new String[] { mongoBeanName });
		when(serviceCreator.createSingletonService()).thenThrow(new CloudServiceException("Multiple services"));
		assertFalse(mongoConfigurer.configure(beanFactory));
		verify(beanFactory, never()).registerSingleton(eq(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME),
				isA(MongoDbFactory.class));
		verify(beanFactory, never()).removeBeanDefinition(mongoBeanName);
		verify(beanFactory, never()).registerAlias(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME, mongoBeanName);
	}

	@Test
	public void leavesOriginalInPlaceIfNoServicesDetected() {
		String mongoBeanName = "testMongoDb";
		when(beanFactory.getBeanNamesForType(MongoDbFactory.class)).thenReturn(new String[] { mongoBeanName });
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		MongoConfigurer mongoConfigurer = new MongoConfigurer(serviceList, serviceCreator);
		assertFalse(mongoConfigurer.configure(beanFactory));
		verify(beanFactory, never()).registerSingleton(eq(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME),
				isA(MongoDbFactory.class));
		verify(beanFactory, never()).removeBeanDefinition(mongoBeanName);
		verify(beanFactory, never()).registerAlias(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME, mongoBeanName);
	}

	@Test
	public void doesNothingIfNoMongoBeansDetected() {
		MongoConfigurer mongoConfigurer = new MongoConfigurer(new ArrayList<Map<String, Object>>(), serviceCreator);
		when(beanFactory.getBeanNamesForType(MongoDbFactory.class)).thenReturn(new String[0]);
		assertFalse(mongoConfigurer.configure(beanFactory));
		verify(beanFactory, never()).registerSingleton(eq(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME),
				isA(MongoDbFactory.class));
		verify(beanFactory, never()).removeBeanDefinition(isA(String.class));
		verify(beanFactory, never()).registerAlias(eq(MongoConfigurer.CF_MONGO_DB_FACTORY_NAME), isA(String.class));
	}

	@Test
	public void doesNothingIfMongoDbFactoryClassNotFound() {
		MongoConfigurer configurer = new StubMongoConfigurer(new ArrayList<Map<String, Object>>(), serviceCreator);
		assertFalse(configurer.configure(beanFactory));
	}

	private class StubMongoConfigurer extends MongoConfigurer {
		public StubMongoConfigurer(List<Map<String, Object>> cloudServices, MongoServiceCreator serviceCreator) {
			super(cloudServices, serviceCreator);
		}

		@Override
		protected Class<?> loadClass(String name) {
			return null;
		}
	}

}
