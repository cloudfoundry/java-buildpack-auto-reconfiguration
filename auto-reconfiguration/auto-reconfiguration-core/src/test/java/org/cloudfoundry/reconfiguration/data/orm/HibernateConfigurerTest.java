package org.cloudfoundry.reconfiguration.data.orm;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.reconfiguration.CloudEnvironmentMockingTest;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionFactoryImpl;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
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
public class HibernateConfigurerTest extends CloudEnvironmentMockingTest {

	@Mock
	private RdbmsServiceInfo mockRdbmsServiceInfo;

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
		String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
		List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();
		serviceInfos.add(mockRdbmsServiceInfo);
		when(mockRdbmsServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockRdbmsServiceInfo.getLabel()).thenReturn("mysql-5.1");
		when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(serviceInfos);

		ApplicationContext context = getTestApplicationContext(appContextFile);
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) context
				.getBean("sessionFactory", SessionFactory.class);

		Assert.assertEquals("org.hibernate.dialect.MySQLDialect", sessionFactory.getDialect().toString());
	}

	private void assertApplicationContextProcessingForPostgresql(String appContextFile) {
		String serviceJdbcUrl = "jdbc:postgresql://10.20.20.40:5432/pg-1";
		List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();
		serviceInfos.add(mockRdbmsServiceInfo);
		when(mockRdbmsServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockRdbmsServiceInfo.getLabel()).thenReturn("postgresql-9.0");
		when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(serviceInfos);

		ApplicationContext context = getTestApplicationContext(appContextFile);
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) context
				.getBean("sessionFactory", SessionFactory.class);

		Assert.assertEquals("org.hibernate.dialect.PostgreSQLDialect", sessionFactory.getDialect().toString());
	}
}
