package org.cloudfoundry.reconfiguration.data.orm;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.cloudfoundry.reconfiguration.AbstractCloudConfigurerTest;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.impl.SessionImpl;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;
import org.springframework.cloud.service.common.RelationalServiceInfo;
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
public class JpaConfigurerTest extends AbstractCloudConfigurerTest {

	@Test
	public void entityManagerFactoryMysqlDialectUpdated() {
		RelationalServiceInfo serviceInfo = new MysqlServiceInfo("my-mysql", "mysql://10.20.20.40:1234/mysql-1");
		ApplicationContext context = getTestApplicationContext("test-jpa-good-context.xml", serviceInfo);
		EntityManagerFactory entityManagerFactory = (EntityManagerFactory) context.getBean("entityManagerFactory",
				EntityManagerFactory.class);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		SessionImpl entityManagerDelegate = (SessionImpl) entityManager.getDelegate();
		SessionFactoryImpl underlyingSessionFactory = (SessionFactoryImpl) entityManagerDelegate.getSessionFactory();
		Assert.assertEquals("org.hibernate.dialect.MySQLDialect", underlyingSessionFactory.getDialect().toString());
	}

	@Test
	public void entityManagerFactoryPostgresqlDialectUpdated() {
        RelationalServiceInfo serviceInfo = new PostgresqlServiceInfo("my-mysql", "postgresql://10.20.20.40:5432/pg-1");
		ApplicationContext context = getTestApplicationContext("test-jpa-good-context.xml", serviceInfo);
		EntityManagerFactory entityManagerFactory = (EntityManagerFactory) context.getBean("entityManagerFactory",
				EntityManagerFactory.class);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		SessionImpl entityManagerDelegate = (SessionImpl) entityManager.getDelegate();
		SessionFactoryImpl underlyingSessionFactory = (SessionFactoryImpl) entityManagerDelegate.getSessionFactory();
		Assert.assertEquals("org.hibernate.dialect.PostgreSQLDialect", underlyingSessionFactory.getDialect().toString());
	}

}
