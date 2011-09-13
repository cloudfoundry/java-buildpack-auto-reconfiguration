package org.cloudfoundry.reconfiguration.data.relational;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.cloudfoundry.reconfiguration.CloudEnvironmentMockingTest;
import org.cloudfoundry.runtime.env.AbstractDataSourceServiceInfo;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.cloudfoundry.runtime.env.PostgresqlServiceInfo;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.AbstractDriverBasedDataSource;

/**
 * Integration test of the {@link DataSourceConfigurer}
 * <p>
 * Bootstraps application contexts containing various DataSource beans and
 * checks if the actual beans got replaced/not replaced with the mock beans.
 *
 * @author Ramnivas Laddad
 * @author Jennifer Hickey
 *
 */
public class DataSourceConfigurerTest extends CloudEnvironmentMockingTest {

	@Mock
	private MysqlServiceInfo mockMysqlServiceInfo;
	@Mock
	private PostgresqlServiceInfo mockPostgresqlServiceInfo;

	@Test
	public void cloudDataSourceReplacesUserDataSourceIfMySqlServiceDetected() {
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

		ApplicationContext context = getTestApplicationContext("test-ds-good-context.xml");
		AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs",
				DataSource.class);
		Assert.assertEquals(serviceJdbcUrl, replacedDataSource.getUrl());
	}

	@Test
	public void cloudDataSourceReplacesUserDataSourceIfPostgreSqlServiceDetected() {
		String serviceJdbcUrl = "jdbc:postgresql://10.20.20.40:5432/mydb-1";
		List<PostgresqlServiceInfo> serviceInfos = new ArrayList<PostgresqlServiceInfo>();
		serviceInfos.add(mockPostgresqlServiceInfo);
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "postgresql-9.0");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		when(mockPostgresqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockEnvironment.getServiceInfos(PostgresqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-ds-good-context.xml");
		AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs",
				DataSource.class);
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
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service1);
		serviceList.add(service2);
		when(mockMysqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl1);
		when(mockPostgresqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl2);
		when(mockEnvironment.getServiceInfos(AbstractDataSourceServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-ds-good-context.xml");
		AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs",
				DataSource.class);
		Assert.assertEquals("jdbc:hsql:localdb", replacedDataSource.getUrl());
	}

	@Test
	public void cloudDataSourceLeavesOriginalInPlaceIfNoServicesDetected() {
		List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
		Map<String, Object> service = new HashMap<String, Object>();
		service.put("label", "mysql-5.1");
		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		serviceList.add(service);
		when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).thenReturn(serviceInfos);
		when(mockEnvironment.getServices()).thenReturn(serviceList);

		ApplicationContext context = getTestApplicationContext("test-ds-good-context.xml");
		AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs",
				DataSource.class);
		Assert.assertEquals("jdbc:hsql:localdb", replacedDataSource.getUrl());
	}
}
