package org.cloudfoundry.reconfiguration.data.relational;

import javax.sql.DataSource;

import org.cloudfoundry.reconfiguration.AbstractCloudConfigurerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;
import org.springframework.cloud.service.common.RelationalServiceInfo;
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
public class DataSourceConfigurerTest extends AbstractCloudConfigurerTest {

	@Test
	public void cloudDataSourceReplacesUserDataSourceIfMySqlServiceDetected() {
		RelationalServiceInfo serviceInfo = new MysqlServiceInfo("my-mysql", "mysql://myuser:mypass@10.20.20.40:1234/my-mysql");
		ApplicationContext context = getTestApplicationContext("test-ds-good-context.xml", serviceInfo);
		AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs",
				DataSource.class);
		Assert.assertEquals(serviceInfo.getJdbcUrl(), replacedDataSource.getUrl());
	}

	@Test
	public void cloudDataSourceReplacesUserDataSourceIfPostgreSqlServiceDetected() {
        RelationalServiceInfo serviceInfo = new PostgresqlServiceInfo("my-postgres", "postgres://myuser:mypass@10.20.20.40:1234/my-postgres");
        ApplicationContext context = getTestApplicationContext("test-ds-good-context.xml", serviceInfo);
        AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs",
                DataSource.class);
        Assert.assertEquals(serviceInfo.getJdbcUrl(), replacedDataSource.getUrl());
	}

	@Test
	public void cloudDataSourceLeavesOriginalInPlaceIfMultipleServicesDetected() {
        RelationalServiceInfo serviceInfo1 = new MysqlServiceInfo("my-mysql", "mysql://myuser:mypass@10.20.20.40:1234/my-mysql");
        RelationalServiceInfo serviceInfo2 = new PostgresqlServiceInfo("my-postgres", "postgresql://myuser:mypass@10.20.20.40:4321/my-postgres");
		ApplicationContext context = getTestApplicationContext("test-ds-good-context.xml", serviceInfo1, serviceInfo2);
		AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs",
				DataSource.class);
		Assert.assertEquals("jdbc:hsql:localdb", replacedDataSource.getUrl());
	}

	@Test
	public void cloudDataSourceLeavesOriginalInPlaceIfNoServicesDetected() {
		ApplicationContext context = getTestApplicationContext("test-ds-good-context.xml");
		AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs",
				DataSource.class);
		Assert.assertEquals("jdbc:hsql:localdb", replacedDataSource.getUrl());
	}
}
