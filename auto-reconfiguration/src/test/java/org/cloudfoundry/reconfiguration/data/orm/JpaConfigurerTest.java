package org.cloudfoundry.reconfiguration.data.orm;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.cloudfoundry.reconfiguration.CloudEnvironmentMockingTest;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
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

	@Mock
	private RdbmsServiceInfo mockRdbmsServiceInfo;

	@Test
	public void entityManagerFactoryMysqlDialectUpdated() {
		String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
		List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();
		serviceInfos.add(mockRdbmsServiceInfo);
		when(mockRdbmsServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockRdbmsServiceInfo.getLabel()).thenReturn("mysql-5.1");
		when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(serviceInfos);

		ApplicationContext context = getTestApplicationContext("test-jpa-good-context.xml");
		EntityManagerFactory entityManagerFactory = (EntityManagerFactory) context.getBean("entityManagerFactory",
				EntityManagerFactory.class);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		SessionImpl entityManagerDelegate = (SessionImpl) entityManager.getDelegate();
		SessionFactoryImpl underlyingSessionFactory = (SessionFactoryImpl) entityManagerDelegate.getSessionFactory();
		Assert.assertEquals("org.hibernate.dialect.MySQLDialect", underlyingSessionFactory.getDialect().toString());
	}

	@Test
	public void entityManagerFactoryPostgresqlDialectUpdated() {
		String serviceJdbcUrl = "jdbc:postgresql://10.20.20.40:5432/pg-1";
		List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();
		serviceInfos.add(mockRdbmsServiceInfo);
		when(mockRdbmsServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockRdbmsServiceInfo.getLabel()).thenReturn("postgresql-9.0");
		when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(serviceInfos);

		ApplicationContext context = getTestApplicationContext("test-jpa-good-context.xml");
		EntityManagerFactory entityManagerFactory = (EntityManagerFactory) context.getBean("entityManagerFactory",
				EntityManagerFactory.class);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		SessionImpl entityManagerDelegate = (SessionImpl) entityManager.getDelegate();
		SessionFactoryImpl underlyingSessionFactory = (SessionFactoryImpl) entityManagerDelegate.getSessionFactory();
		Assert.assertEquals("org.hibernate.dialect.PostgreSQLDialect", underlyingSessionFactory.getDialect().toString());
	}

	@Test
	public void entityManagerFactorySqlFireDialectUpdated() {
		String serviceJdbcUrl = "jdbc:sqlfire://10.20.20.40:5432/sqlf-1";
		List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();
		serviceInfos.add(mockRdbmsServiceInfo);
		when(mockRdbmsServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockRdbmsServiceInfo.getLabel()).thenReturn("sqlfire-1.0");
		when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(serviceInfos);

		ApplicationContext context = getTestApplicationContext("test-jpa-good-context.xml");
		EntityManagerFactory entityManagerFactory = (EntityManagerFactory) context.getBean("entityManagerFactory",
				EntityManagerFactory.class);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		SessionImpl entityManagerDelegate = (SessionImpl) entityManager.getDelegate();
		SessionFactoryImpl underlyingSessionFactory = (SessionFactoryImpl) entityManagerDelegate.getSessionFactory();
		Assert.assertEquals("org.hibernate.dialect.DerbyDialect", underlyingSessionFactory.getDialect().toString());
	}

}
