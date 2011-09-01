package org.cloudfoundry.reconfiguration;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.AbstractDataSourceServiceInfo;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.cloudfoundry.runtime.env.PostgresqlServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;
import org.cloudfoundry.runtime.service.CloudServicesAutoPopulator;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.impl.SessionImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.AbstractDriverBasedDataSource;

/**
 * Unit-tests for {@link CloudAutoStagingBeanFactoryPostProcessor}.
 * <p>
 * This test uses a mock application context to introduce environment with services
 * to avoid the need of actual working application and services. The assertions
 * are made to check if the actual beans got replaced/not replaced with the mock beans.
 * 
 * @author Ramnivas Laddad
 *
 */
public class CloudFactoryAutoStagingBeanFactoryPostProcessorTest {
	@Mock private MysqlServiceInfo mockMysqlServiceInfo;
	@Mock private PostgresqlServiceInfo mockPostgresqlServiceInfo;
	@Mock private CloudEnvironment mockEnvironment;
	@Mock private ConfigurableListableBeanFactory beanFactory;

	
	private CloudAutoStagingBeanFactoryPostProcessor testBFPP;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		testBFPP = new CloudAutoStagingBeanFactoryPostProcessor();
	}
	
	@Test
	public void cloudDataSourceReplacesUserDataSourceIfMySqlServiceDetected() {
		String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
		List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
		serviceInfos.add(mockMysqlServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mysql-5.1");
		List<Map<String,Object>> serviceList = new ArrayList<Map<String,Object>>();
		serviceList.add(service);
		when(mockMysqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-jpa-good-context.xml");
		AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs", DataSource.class);
		Assert.assertEquals(serviceJdbcUrl, replacedDataSource.getUrl());
	}
	
	@Test
	public void cloudDataSourceReplacesUserDataSourceIfPostgreSqlServiceDetected() {
		String serviceJdbcUrl = "jdbc:postgresql://10.20.20.40:5432/mydb-1";
		List<PostgresqlServiceInfo> serviceInfos = new ArrayList<PostgresqlServiceInfo>();
		serviceInfos.add(mockPostgresqlServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "postgresql-9.0");
		List<Map<String,Object>> serviceList = new ArrayList<Map<String,Object>>();
		serviceList.add(service);
		when(mockPostgresqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockEnvironment.getServiceInfos(PostgresqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-jpa-good-context.xml");
		AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs", DataSource.class);
		Assert.assertEquals(serviceJdbcUrl, replacedDataSource.getUrl());
	}

	@Test
	public void cloudDataSourceLeavesOriginalInPlaceIfMultipleServicesDetected() {
		String serviceJdbcUrl1 = "jdbc:mysql://10.20.20.40:1234/mysql-1";
		String serviceJdbcUrl2 = "jdbc:postgresql://10.20.20.40:5432/pg-2";
		List<AbstractDataSourceServiceInfo> serviceInfos = new ArrayList<AbstractDataSourceServiceInfo>();
		serviceInfos.add(mockMysqlServiceInfo);
		serviceInfos.add(mockPostgresqlServiceInfo);
		Map<String, Object> service1 = new HashMap<String, Object>();
		service1.put("label", "mysql-5.1");
		Map<String, Object> service2 = new HashMap<String, Object>();
		service2.put("label", "postgresql-9.0");
		List<Map<String,Object>> serviceList = new ArrayList<Map<String,Object>>();
		serviceList.add(service1);
		serviceList.add(service2);
		when(mockMysqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl1);
		when(mockPostgresqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl2);
		when(mockEnvironment.getServiceInfos(AbstractDataSourceServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-jpa-good-context.xml");
		AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs", DataSource.class);
		Assert.assertEquals("jdbc:hsql:localdb", replacedDataSource.getUrl());
	}

	@Test
	public void cloudDataSourceLeavesOriginalInPlaceIfNoServicesDetected() {
		List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mysql-5.1");
		List<Map<String,Object>> serviceList = new ArrayList<Map<String,Object>>();
		serviceList.add(service);
		when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-jpa-good-context.xml");
		AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs", DataSource.class);
		Assert.assertEquals("jdbc:hsql:localdb", replacedDataSource.getUrl());
	}
	
	@Test
	public void hibernateSessionFactoryDialectUpdated() {
		assertApplicationContextProcessingForMysql("test-hibernate-good-context.xml");
	}
	
	@Test
	public void entityManagerFactoryMysqlDialectUpdated() {
		String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
		List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
		serviceInfos.add(mockMysqlServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mysql-5.1");
		List<Map<String,Object>> serviceList = new ArrayList<Map<String,Object>>();
		serviceList.add(service);
		when(mockMysqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-jpa-good-context.xml");
		EntityManagerFactory entityManagerFactory = (EntityManagerFactory) context.getBean("entityManagerFactory", EntityManagerFactory.class);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		SessionImpl entityManagerDelegate = (SessionImpl) entityManager.getDelegate();
		SessionFactoryImpl underlyingSessionFactory = (SessionFactoryImpl) entityManagerDelegate.getSessionFactory();
		Assert.assertEquals("org.hibernate.dialect.MySQLDialect", underlyingSessionFactory.getDialect().toString());
	}
	
	@Test
	public void entityManagerFactoryPostgresqlDialectUpdated() {
		String serviceJdbcUrl = "jdbc:postgresql://10.20.20.40:5432/pg-1";
		List<PostgresqlServiceInfo> serviceInfos = new ArrayList<PostgresqlServiceInfo>();
		serviceInfos.add(mockPostgresqlServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "postgresql-9.0");
		List<Map<String,Object>> serviceList = new ArrayList<Map<String,Object>>();
		serviceList.add(service);
		when(mockPostgresqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockEnvironment.getServiceInfos(PostgresqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-jpa-good-context.xml");
		EntityManagerFactory entityManagerFactory = (EntityManagerFactory) context.getBean("entityManagerFactory", EntityManagerFactory.class);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		SessionImpl entityManagerDelegate = (SessionImpl) entityManager.getDelegate();
		SessionFactoryImpl underlyingSessionFactory = (SessionFactoryImpl) entityManagerDelegate.getSessionFactory();
		Assert.assertEquals("org.hibernate.dialect.PostgreSQLDialect", underlyingSessionFactory.getDialect().toString());
	}

	@Test
	public void grailsLikeEmbeddedPropertyApplicationContextProcessed() {
		assertApplicationContextProcessingForMysql("test-grails-embedded-props-good-context.xml");
		assertApplicationContextProcessingForPostgresql("test-grails-embedded-props-good-context.xml");
	}

	@Test
	public void grailsLikeReferencedMapApplicationContextProcessed() {
		assertApplicationContextProcessingForMysql("test-grails-map-reference-good-context.xml");
		assertApplicationContextProcessingForPostgresql("test-grails-map-reference-good-context.xml");
	}

	@Test
	public void grailsLikeReferencedPropertyFactoryApplicationContextProcessed() {
		assertApplicationContextProcessingForMysql("test-grails-propertyFactory-reference-good-context.xml");
		assertApplicationContextProcessingForPostgresql("test-grails-propertyFactory-reference-good-context.xml");
	}

	@Test
	public void typeStringValuePropertyApplicationContextProcessed() {
		assertApplicationContextProcessingForMysql("test-jpa-typedStringValue-good-context.xml");
		assertApplicationContextProcessingForPostgresql("test-jpa-typedStringValue-good-context.xml");
	}

	@Test
	public void referencedNestedPropertyFactorySingleLocationApplicationContextProcessed() {
		assertApplicationContextProcessingForMysql("test-propertyFactory-nested-reference-single-location-good-context.xml");
		assertApplicationContextProcessingForPostgresql("test-propertyFactory-nested-reference-single-location-good-context.xml");
	}

	@Test
	public void referencedNestedPropertyFactoryListLocationApplicationContextProcessed() {
		assertApplicationContextProcessingForMysql("test-propertyFactory-nested-reference-list-location-good-context.xml");
		assertApplicationContextProcessingForPostgresql("test-propertyFactory-nested-reference-list-location-good-context.xml");
	}

	@Test
	public void autoStagingOffWhenUsingCloudServiceBean() {
		String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
		List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
		serviceInfos.add(mockMysqlServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mysql-5.1");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		when(mockMysqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);
		ClassPathXmlApplicationContext context = getTestApplicationContext("test-auto-stage-off-cloud-service-context.xml");
		Assert.assertTrue(testBFPP.autoStagingOff(context.getBeanFactory()));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void autoStagingOffWhenUsingAutoPopulate() {
		// Mock out BeanFactory here because we can't instantiate
		// CloudServicesAutoPopulator in app context (postProcessBeanFactory
		// depends on Spring 3.x)
		when(beanFactory.getBeansOfType(AbstractCloudServiceFactory.class)).thenReturn(
				new HashMap<String, AbstractCloudServiceFactory>(0, 1));
		Map<String, CloudServicesAutoPopulator> autoPopulatorBeans = new HashMap<String, CloudServicesAutoPopulator>();
		autoPopulatorBeans.put("autoPopBean", new CloudServicesAutoPopulator());
		when(beanFactory.getBeansOfType(CloudServicesAutoPopulator.class)).thenReturn(autoPopulatorBeans);
		Assert.assertTrue(testBFPP.autoStagingOff(beanFactory));
	}
	
	private void assertApplicationContextProcessingForMysql(String appContextFile) {
		String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
		List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
		serviceInfos.add(mockMysqlServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mysql-5.1");
		List<Map<String,Object>> serviceList = new ArrayList<Map<String,Object>>();
		serviceList.add(service);
		when(mockMysqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext(appContextFile);
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) context.getBean("sessionFactory", SessionFactory.class);
		
		Assert.assertEquals("org.hibernate.dialect.MySQLDialect", sessionFactory.getDialect().toString());

	}
	
	private void assertApplicationContextProcessingForPostgresql(String appContextFile) {
		String serviceJdbcUrl = "jdbc:postgresql://10.20.20.40:5432/pg-1";
		List<PostgresqlServiceInfo> serviceInfos = new ArrayList<PostgresqlServiceInfo>();
		serviceInfos.add(mockPostgresqlServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "postgresql-9.0");
		List<Map<String,Object>> serviceList = new ArrayList<Map<String,Object>>();
		serviceList.add(service);
		when(mockPostgresqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockEnvironment.getServiceInfos(PostgresqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext(appContextFile);
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) context.getBean("sessionFactory", SessionFactory.class);
		
		Assert.assertEquals("org.hibernate.dialect.PostgreSQLDialect", sessionFactory.getDialect().toString());
	}
	
	private ClassPathXmlApplicationContext getTestApplicationContext(String fileName) {
		return new ClassPathXmlApplicationContext(new String[]{"org/cloudfoundry/reconfiguration/" + fileName, 
															  "META-INF/cloud/cloudfoundry-auto-reconfiguration-context.xml"}) {
			@Override
			protected void prepareBeanFactory(
					ConfigurableListableBeanFactory beanFactory) {
				getBeanFactory().registerSingleton("test-cloudEnvironment", mockEnvironment);
				super.prepareBeanFactory(beanFactory);
			}
		};
	}
}
