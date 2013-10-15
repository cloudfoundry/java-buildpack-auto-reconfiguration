package org.cloudfoundry.reconfiguration.data.orm;

import org.cloudfoundry.reconfiguration.AbstractCloudConfigurerTest;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionFactoryImpl;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;
import org.springframework.cloud.service.common.RelationalServiceInfo;
import org.springframework.context.ApplicationContext;

/**
 * Integration test of the {@link HibernateConfigurer}
 * <p>
 * Bootstraps application contexts containing various DataSource and Hibernate
 * beans and checks if the actual beans got replaced/not replaced with the mock
 * beans.
 *
 * @author Ramnivas Laddad
 * @author Jennifer Hickey
 *
 */
public class HibernateConfigurerTest extends AbstractCloudConfigurerTest {

	@Test
	public void hibernateSessionFactoryDialectUpdated() {
		assertApplicationContextProcessingForMysql("test-hibernate-good-context.xml");
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
	public void typeStringValuePropertyApplicationContextProcessed() {
		assertApplicationContextProcessingForMysql("test-jpa-typedStringValue-good-context.xml");
		assertApplicationContextProcessingForPostgresql("test-jpa-typedStringValue-good-context.xml");
	}

	private void assertApplicationContextProcessingForMysql(String appContextFile) {
		RelationalServiceInfo serviceInfo = new MysqlServiceInfo("my-mysql", "mysql://myuser:mypass@10.20.20.40:1234/mysql-1");
		ApplicationContext context = getTestApplicationContext(appContextFile, serviceInfo);
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) context
				.getBean("sessionFactory", SessionFactory.class);

		Assert.assertEquals("org.hibernate.dialect.MySQLDialect", sessionFactory.getDialect().toString());
	}

	private void assertApplicationContextProcessingForPostgresql(String appContextFile) {
        RelationalServiceInfo serviceInfo = new PostgresqlServiceInfo("my-post", "mysql://myuser:mypass@postgresql://10.20.20.40:5432/pg-1");
		ApplicationContext context = getTestApplicationContext(appContextFile, serviceInfo);
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) context
				.getBean("sessionFactory", SessionFactory.class);

		Assert.assertEquals("org.hibernate.dialect.PostgreSQLDialect", sessionFactory.getDialect().toString());
	}
}
