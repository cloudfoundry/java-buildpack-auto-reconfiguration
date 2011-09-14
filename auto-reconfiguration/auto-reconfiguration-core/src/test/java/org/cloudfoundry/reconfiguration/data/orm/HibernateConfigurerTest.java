package org.cloudfoundry.reconfiguration.data.orm;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.reconfiguration.CloudEnvironmentMockingTest;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.cloudfoundry.runtime.env.PostgresqlServiceInfo;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionFactoryImpl;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

/**
 * Integration test of the {@link HibernateConfigurer}
 * <p>
 * Bootstraps application contexts containing various DataSource and
 * Hibernate beans and checks if the actual beans got replaced/not replaced with
 * the mock beans.
 *
 * @author Ramnivas Laddad
 * @author Jennifer Hickey
 *
 */
public class HibernateConfigurerTest extends CloudEnvironmentMockingTest {

	@Mock
	private MysqlServiceInfo mockMysqlServiceInfo;
	@Mock
	private PostgresqlServiceInfo mockPostgresqlServiceInfo;

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
		List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
		serviceInfos.add(mockMysqlServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mysql-5.1");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		when(mockMysqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext(appContextFile);
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) context
				.getBean("sessionFactory", SessionFactory.class);

		Assert.assertEquals("org.hibernate.dialect.MySQLDialect", sessionFactory.getDialect().toString());
	}

	private void assertApplicationContextProcessingForPostgresql(String appContextFile) {
		String serviceJdbcUrl = "jdbc:postgresql://10.20.20.40:5432/pg-1";
		List<PostgresqlServiceInfo> serviceInfos = new ArrayList<PostgresqlServiceInfo>();
		serviceInfos.add(mockPostgresqlServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "postgresql-9.0");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		when(mockPostgresqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockEnvironment.getServiceInfos(PostgresqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext(appContextFile);
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) context
				.getBean("sessionFactory", SessionFactory.class);

		Assert.assertEquals("org.hibernate.dialect.PostgreSQLDialect", sessionFactory.getDialect().toString());
	}
}
