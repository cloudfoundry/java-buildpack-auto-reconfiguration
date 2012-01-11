package org.cloudfoundry.reconfiguration.data.relational;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.cloudfoundry.reconfiguration.CloudEnvironmentMockingTest;
import org.cloudfoundry.runtime.env.AbstractDataSourceServiceInfo;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
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
    private RdbmsServiceInfo mockRdbmsServiceInfo;

    @Mock
    private RdbmsServiceInfo mockRdbmsServiceInfo2;

    @Test
    public void cloudDataSourceReplacesUserDataSourceIfMySqlServiceDetected() {
        String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
        List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();
        serviceInfos.add(mockRdbmsServiceInfo);
        when(mockRdbmsServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
        when(mockRdbmsServiceInfo.getLabel()).thenReturn("mysql-5.1");
        when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(serviceInfos);

        ApplicationContext context = getTestApplicationContext("test-ds-good-context.xml");
        AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs",
                DataSource.class);
        Assert.assertEquals(serviceJdbcUrl, replacedDataSource.getUrl());
    }

    @Test
    public void cloudDataSourceReplacesUserDataSourceIfPostgreSqlServiceDetected() {
        String serviceJdbcUrl = "jdbc:postgresql://10.20.20.40:5432/mydb-1";
        List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();
        serviceInfos.add(mockRdbmsServiceInfo);
        when(mockRdbmsServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
        when(mockRdbmsServiceInfo.getLabel()).thenReturn("postgresql-9.0");
        when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(serviceInfos);

        ApplicationContext context = getTestApplicationContext("test-ds-good-context.xml");
        AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs",
                DataSource.class);
        Assert.assertEquals(serviceJdbcUrl, replacedDataSource.getUrl());
    }

    @Test
    public void cloudDataSourceReplacesUserDataSourceIfSqlfireServiceDetected() {
        String serviceJdbcUrl = "jdbc:sqlfire://10.20.20.40:5432/mydb-1";
        List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();
        serviceInfos.add(mockRdbmsServiceInfo);
        when(mockRdbmsServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
        when(mockRdbmsServiceInfo.getLabel()).thenReturn("sqlfire-1.0");
        when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(serviceInfos);

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
        serviceInfos.add(mockRdbmsServiceInfo);
        serviceInfos.add(mockRdbmsServiceInfo2);
        when(mockRdbmsServiceInfo.getUrl()).thenReturn(serviceJdbcUrl1);
        when(mockRdbmsServiceInfo2.getUrl()).thenReturn(serviceJdbcUrl2);
        when(mockEnvironment.getServiceInfos(AbstractDataSourceServiceInfo.class)).thenReturn(serviceInfos);
        ApplicationContext context = getTestApplicationContext("test-ds-good-context.xml");
        AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs",
                DataSource.class);
        Assert.assertEquals("jdbc:hsql:localdb", replacedDataSource.getUrl());
    }

    @Test
    public void cloudDataSourceLeavesOriginalInPlaceIfNoServicesDetected() {
        List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();
        when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).thenReturn(serviceInfos);
        ApplicationContext context = getTestApplicationContext("test-ds-good-context.xml");
        AbstractDriverBasedDataSource replacedDataSource = (AbstractDriverBasedDataSource) context.getBean("myDs",
                DataSource.class);
        Assert.assertEquals("jdbc:hsql:localdb", replacedDataSource.getUrl());
    }
}
