package org.cloudfoundry.reconfiguration.data.orm;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.cloudfoundry.reconfiguration.CloudEnvironmentMockingTest;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.cloudfoundry.runtime.env.PostgresqlServiceInfo;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.impl.SessionImpl;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

/**
 * Integration test of the {@link JpaConfigurer}
 * <p>
 * Bootstraps application contexts containing various DataSource, JPA, and
 * Hibernate beans and checks if the actual beans got replaced/not replaced with
 * the mock beans.
 *
 * @author Ramnivas Laddad
 * @author Jennifer Hickey
 *
 */
public class JpaConfigurerTest extends CloudEnvironmentMockingTest {

	@Mock private MysqlServiceInfo mockMysqlServiceInfo;
	@Mock private PostgresqlServiceInfo mockPostgresqlServiceInfo;

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



}
