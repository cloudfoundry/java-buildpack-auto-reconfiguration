package org.cloudfoundry.reconfiguration;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
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
	@Mock private MysqlServiceInfo mockMysqlServiceInfo1;
	@Mock private MysqlServiceInfo mockMysqlServiceInfo2;
	@Mock private CloudEnvironment mockEnvironment;
	
	private CloudAutoStagingBeanFactoryPostProcessor testBFPP;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		testBFPP = new CloudAutoStagingBeanFactoryPostProcessor();
	}
	
	@Test
	public void cloudDataSourceReplacesUserDataSourceIfOneServiceDetected() {
		String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
		List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
		serviceInfos.add(mockMysqlServiceInfo1);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mysql-5.1");
		List<Map<String,Object>> serviceList = new ArrayList<Map<String,Object>>();
		serviceList.add(service);
		when(mockMysqlServiceInfo1.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-jpa-good-context.xml");
		AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs", DataSource.class);
		Assert.assertEquals(serviceJdbcUrl, replacedDataSource.getUrl());
	}

	@Test
	public void cloudDataSourceLeavesOriginalInPlaceIfMultipleServicesDetected() {
		String serviceJdbcUrl1 = "jdbc:mysql://10.20.20.40:1234/mysql-1";
		String serviceJdbcUrl2 = "jdbc:mysql://10.20.20.40:1234/mysql-2";
		List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
		serviceInfos.add(mockMysqlServiceInfo1);
		serviceInfos.add(mockMysqlServiceInfo2);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mysql-5.1");
		List<Map<String,Object>> serviceList = new ArrayList<Map<String,Object>>();
		serviceList.add(service);
		when(mockMysqlServiceInfo1.getUrl()).thenReturn(serviceJdbcUrl1);
		when(mockMysqlServiceInfo2.getUrl()).thenReturn(serviceJdbcUrl2);
		when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).thenReturn(serviceInfos);
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
		assertApplicationContextProcessing("test-hibernate-good-context.xml");
	}
	
	@Test
	public void entityManagerFactoryDialectUpdated() {
		String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
		List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
		serviceInfos.add(mockMysqlServiceInfo1);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mysql-5.1");
		List<Map<String,Object>> serviceList = new ArrayList<Map<String,Object>>();
		serviceList.add(service);
		when(mockMysqlServiceInfo1.getUrl()).thenReturn(serviceJdbcUrl);
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
	public void grailsLikeEmbeddedPropertyApplicationContextProcessed() {
		assertApplicationContextProcessing("test-grails-embedded-props-good-context.xml");
	}

	@Test
	public void grailsLikeReferencedMapApplicationContextProcessed() {
		assertApplicationContextProcessing("test-grails-map-reference-good-context.xml");
	}

	@Test
	public void grailsLikeReferencedPropertyFactoryApplicationContextProcessed() {
		assertApplicationContextProcessing("test-grails-propertyFactory-reference-good-context.xml");
	}

	@Test
	public void typeStringValuePropertyApplicationContextProcessed() {
		assertApplicationContextProcessing("test-jpa-typedStringValue-good-context.xml");
	}

	@Test
	public void referencedNestedPropertyFactorySingleLocationApplicationContextProcessed() {
		assertApplicationContextProcessing("test-propertyFactory-nested-reference-single-location-good-context.xml");
	}

	@Test
	public void referencedNestedPropertyFactoryListLocationApplicationContextProcessed() {
		assertApplicationContextProcessing("test-propertyFactory-nested-reference-list-location-good-context.xml");
	}

	@Test
	public void autostagingOffWhenPropertyFileSpecifiedOff() {
		Assert.assertTrue(testBFPP.autoStagingOff("org/cloudfoundry/reconfiguration/test-autostaging-off.properties"));
	}

	@Test
	public void autostagingOnWhenPropertyFileSpecifiedOn() {
		Assert.assertFalse(testBFPP.autoStagingOff("org/cloudfoundry/reconfiguration/test-autostaging-on.properties"));
	}

	@Test
	public void autostagingOnWhenPropertyFileEmpty() {
		Assert.assertFalse(testBFPP.autoStagingOff("org/cloudfoundry/reconfiguration/test-autostaging-empty.properties"));
	}
	
	@Test
	public void autostagingOnWhenPropertyFileCorrupt() {
		Assert.assertFalse(testBFPP.autoStagingOff("org/cloudfoundry/reconfiguration/test-autostaging-corrupt.properties"));
	}

	@Test
	public void autostagingOnWhenPropertyFileDoesntExist() {
		Assert.assertFalse(testBFPP.autoStagingOff("file-that-doesnt-exist"));
	}

	private void assertApplicationContextProcessing(String appContextFile) {
		String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
		List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
		serviceInfos.add(mockMysqlServiceInfo1);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mysql-5.1");
		List<Map<String,Object>> serviceList = new ArrayList<Map<String,Object>>();
		serviceList.add(service);
		when(mockMysqlServiceInfo1.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext(appContextFile);
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) context.getBean("sessionFactory", SessionFactory.class);
		
		Assert.assertEquals("org.hibernate.dialect.MySQLDialect", sessionFactory.getDialect().toString());

	}
	
	private ApplicationContext getTestApplicationContext(String fileName) {
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
