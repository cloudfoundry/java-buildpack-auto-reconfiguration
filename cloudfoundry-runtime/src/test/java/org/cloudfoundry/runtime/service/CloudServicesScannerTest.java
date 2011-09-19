package org.cloudfoundry.runtime.service;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyObject;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Unit test of the {@link CloudServicesScanner}
 *
 * @author Jennifer Hickey
 *
 */
public class CloudServicesScannerTest {

	@Mock
	private CloudEnvironment mockEnvironment;

	@Mock
	private ConfigurableListableBeanFactory beanFactory;

	@Mock
	MongoServiceInfo mongoServiceInfo;

	@Mock
	RdbmsServiceInfo rdbmsServiceInfo;

	@Mock
	RedisServiceInfo redisServiceInfo;

	@Mock
	RabbitServiceInfo rabbitServiceInfo;

	private CloudServicesScanner cloudServicesScanner = new CloudServicesScanner();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createCloudServiceBeans() {
		when(beanFactory.getBeanNamesForType(CloudEnvironment.class)).thenReturn(new String[] { "cloudenv" });
		when(beanFactory.getBean(CloudEnvironment.class)).thenReturn(mockEnvironment);
		List<MongoServiceInfo> mongoSvcs = new ArrayList<MongoServiceInfo>();
		mongoSvcs.add(mongoServiceInfo);
		when(mockEnvironment.getServiceInfos(MongoServiceInfo.class)).thenReturn(mongoSvcs);
		when(mongoServiceInfo.getServiceName()).thenReturn("mongo1");
		when(mongoServiceInfo.getDatabase()).thenReturn("stocks");
		List<RedisServiceInfo> redisSvcs = new ArrayList<RedisServiceInfo>();
		redisSvcs.add(redisServiceInfo);
		when(mockEnvironment.getServiceInfos(RedisServiceInfo.class)).thenReturn(redisSvcs);
		when(redisServiceInfo.getServiceName()).thenReturn("redis1");
		List<RabbitServiceInfo> rabbitSvcs = new ArrayList<RabbitServiceInfo>();
		rabbitSvcs.add(rabbitServiceInfo);
		when(mockEnvironment.getServiceInfos(RabbitServiceInfo.class)).thenReturn(rabbitSvcs);
		when(rabbitServiceInfo.getServiceName()).thenReturn("rabbit1");
		List<RdbmsServiceInfo> rdbmsSvcs = new ArrayList<RdbmsServiceInfo>();
		rdbmsSvcs.add(rdbmsServiceInfo);
		when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(rdbmsSvcs);
		when(rdbmsServiceInfo.getServiceName()).thenReturn("data1");
		cloudServicesScanner.postProcessBeanFactory(beanFactory);
		verify(beanFactory).registerSingleton(eq("mongo1"), any(MongoDbFactory.class));
		verify(beanFactory).registerSingleton(eq("redis1"), any(RedisConnectionFactory.class));
		verify(beanFactory).registerSingleton(eq("rabbit1"), any(ConnectionFactory.class));
		verify(beanFactory).registerSingleton(eq("data1"), any(DataSource.class));
	}

	@Test(expected = CloudServiceException.class)
	public void postProcessBeanFactoryMultipleCloudEnvs() {
		when(beanFactory.getBeanNamesForType(CloudEnvironment.class)).thenReturn(
				new String[] { "cloudenv1", "cloudenv2" });
		cloudServicesScanner.postProcessBeanFactory(beanFactory);
	}

	@Test
	public void noMongoSvcs() {
		List<MongoServiceInfo> mongoSvcs = new ArrayList<MongoServiceInfo>();
		when(mockEnvironment.getServiceInfos(MongoServiceInfo.class)).thenReturn(mongoSvcs);
		List<RedisServiceInfo> redisSvcs = new ArrayList<RedisServiceInfo>();
		redisSvcs.add(redisServiceInfo);
		when(mockEnvironment.getServiceInfos(RedisServiceInfo.class)).thenReturn(redisSvcs);
		when(redisServiceInfo.getServiceName()).thenReturn("redis1");
		List<RabbitServiceInfo> rabbitSvcs = new ArrayList<RabbitServiceInfo>();
		rabbitSvcs.add(rabbitServiceInfo);
		when(mockEnvironment.getServiceInfos(RabbitServiceInfo.class)).thenReturn(rabbitSvcs);
		when(rabbitServiceInfo.getServiceName()).thenReturn("rabbit1");
		List<RdbmsServiceInfo> rdbmsSvcs = new ArrayList<RdbmsServiceInfo>();
		rdbmsSvcs.add(rdbmsServiceInfo);
		when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(rdbmsSvcs);
		when(rdbmsServiceInfo.getServiceName()).thenReturn("data1");
		cloudServicesScanner.createCloudServiceBeans(beanFactory, mockEnvironment);
		verify(beanFactory).registerSingleton(eq("redis1"), any(RedisConnectionFactory.class));
		verify(beanFactory).registerSingleton(eq("rabbit1"), any(ConnectionFactory.class));
		verify(beanFactory).registerSingleton(eq("data1"), any(DataSource.class));
		// Verify we registered only the above 3 beans
		verify(beanFactory, times(3)).registerSingleton(anyString(), anyObject());
	}

	@Test
	public void noRedisSvcs() {
		List<MongoServiceInfo> mongoSvcs = new ArrayList<MongoServiceInfo>();
		mongoSvcs.add(mongoServiceInfo);
		when(mockEnvironment.getServiceInfos(MongoServiceInfo.class)).thenReturn(mongoSvcs);
		when(mongoServiceInfo.getServiceName()).thenReturn("mongo1");
		when(mongoServiceInfo.getDatabase()).thenReturn("stocks");
		List<RedisServiceInfo> redisSvcs = new ArrayList<RedisServiceInfo>();
		when(mockEnvironment.getServiceInfos(RedisServiceInfo.class)).thenReturn(redisSvcs);
		List<RabbitServiceInfo> rabbitSvcs = new ArrayList<RabbitServiceInfo>();
		rabbitSvcs.add(rabbitServiceInfo);
		when(mockEnvironment.getServiceInfos(RabbitServiceInfo.class)).thenReturn(rabbitSvcs);
		when(rabbitServiceInfo.getServiceName()).thenReturn("rabbit1");
		List<RdbmsServiceInfo> rdbmsSvcs = new ArrayList<RdbmsServiceInfo>();
		rdbmsSvcs.add(rdbmsServiceInfo);
		when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(rdbmsSvcs);
		when(rdbmsServiceInfo.getServiceName()).thenReturn("data1");
		cloudServicesScanner.createCloudServiceBeans(beanFactory, mockEnvironment);
		verify(beanFactory).registerSingleton(eq("mongo1"), any(MongoDbFactory.class));
		verify(beanFactory).registerSingleton(eq("rabbit1"), any(ConnectionFactory.class));
		verify(beanFactory).registerSingleton(eq("data1"), any(DataSource.class));
		// Verify we registered only the above 3 beans
		verify(beanFactory, times(3)).registerSingleton(anyString(), anyObject());
	}

	@Test
	public void noRabbitSvcs() {
		List<MongoServiceInfo> mongoSvcs = new ArrayList<MongoServiceInfo>();
		mongoSvcs.add(mongoServiceInfo);
		when(mockEnvironment.getServiceInfos(MongoServiceInfo.class)).thenReturn(mongoSvcs);
		when(mongoServiceInfo.getServiceName()).thenReturn("mongo1");
		when(mongoServiceInfo.getDatabase()).thenReturn("stocks");
		List<RedisServiceInfo> redisSvcs = new ArrayList<RedisServiceInfo>();
		redisSvcs.add(redisServiceInfo);
		when(mockEnvironment.getServiceInfos(RedisServiceInfo.class)).thenReturn(redisSvcs);
		when(redisServiceInfo.getServiceName()).thenReturn("redis1");
		List<RabbitServiceInfo> rabbitSvcs = new ArrayList<RabbitServiceInfo>();
		when(mockEnvironment.getServiceInfos(RabbitServiceInfo.class)).thenReturn(rabbitSvcs);
		List<RdbmsServiceInfo> rdbmsSvcs = new ArrayList<RdbmsServiceInfo>();
		rdbmsSvcs.add(rdbmsServiceInfo);
		when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(rdbmsSvcs);
		when(rdbmsServiceInfo.getServiceName()).thenReturn("data1");
		cloudServicesScanner.createCloudServiceBeans(beanFactory, mockEnvironment);
		verify(beanFactory).registerSingleton(eq("mongo1"), any(MongoDbFactory.class));
		verify(beanFactory).registerSingleton(eq("redis1"), any(RedisConnectionFactory.class));
		verify(beanFactory).registerSingleton(eq("data1"), any(DataSource.class));
		// Verify we registered only the above 3 beans
		verify(beanFactory, times(3)).registerSingleton(anyString(), anyObject());
	}

	@Test
	public void noRdbmsSvcs() {
		List<MongoServiceInfo> mongoSvcs = new ArrayList<MongoServiceInfo>();
		mongoSvcs.add(mongoServiceInfo);
		when(mockEnvironment.getServiceInfos(MongoServiceInfo.class)).thenReturn(mongoSvcs);
		when(mongoServiceInfo.getServiceName()).thenReturn("mongo1");
		when(mongoServiceInfo.getDatabase()).thenReturn("stocks");
		List<RedisServiceInfo> redisSvcs = new ArrayList<RedisServiceInfo>();
		redisSvcs.add(redisServiceInfo);
		when(mockEnvironment.getServiceInfos(RedisServiceInfo.class)).thenReturn(redisSvcs);
		when(redisServiceInfo.getServiceName()).thenReturn("redis1");
		List<RabbitServiceInfo> rabbitSvcs = new ArrayList<RabbitServiceInfo>();
		rabbitSvcs.add(rabbitServiceInfo);
		when(mockEnvironment.getServiceInfos(RabbitServiceInfo.class)).thenReturn(rabbitSvcs);
		when(rabbitServiceInfo.getServiceName()).thenReturn("rabbit1");
		List<RdbmsServiceInfo> rdbmsSvcs = new ArrayList<RdbmsServiceInfo>();
		when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(rdbmsSvcs);
		cloudServicesScanner.createCloudServiceBeans(beanFactory, mockEnvironment);
		verify(beanFactory).registerSingleton(eq("mongo1"), any(MongoDbFactory.class));
		verify(beanFactory).registerSingleton(eq("redis1"), any(RedisConnectionFactory.class));
		verify(beanFactory).registerSingleton(eq("rabbit1"), any(ConnectionFactory.class));
		// Verify we registered only the above 3 beans
		verify(beanFactory, times(3)).registerSingleton(anyString(), anyObject());
	}
}
