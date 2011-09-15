package org.cloudfoundry.runtime.service;

import java.beans.PropertyDescriptor;
import java.sql.DriverManager;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.AbstractDataSourceServiceInfo;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.service.relational.DataSourceConfiguration;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * Abstract service creator for data service.
 * @author Ramnivas Laddad
 * @author Thomas Risberg
 *
 */
abstract public class AbstractDataSourceCreator<SI extends AbstractDataSourceServiceInfo> 
extends AbstractServiceCreator<DataSource, SI> {

	protected DataSourceConfiguration dataSourceConfiguration =
			new DataSourceConfiguration();

	public AbstractDataSourceCreator(CloudEnvironment cloudEnvironment,
			Class<SI> serviceInfoClass) {
		super(cloudEnvironment, serviceInfoClass);
	}
	
	abstract public String getDriverClassName();

	public DataSourceConfiguration getDataSourceConfiguration() {
		return this.dataSourceConfiguration;
	}

	/**
	 * Create a datasource based on service info.
	 *
	 * <p>
	 * Tries to create a pooled connection based on either Commons DBCP or
	 * Tomcat's internal DBCP.  The connection pool should validate connections
	 * as they are dispensed.
	 *
	 * <p>
	 * User provided properties for the DataSource will be applied to the created DataSource.
	 *
	 * <p>If validation query was not provided we will add one and enable testOnBorrow.
	 * Testing on borrow will add slight overhead for requests needing a
	 * database connection as the validation query needs to execute before the
	 * connection is returned by the pool.  testOnBorrow is the best option
	 * provided by DBCP.
	 */
	public DataSource createService(AbstractDataSourceServiceInfo serviceInfo) {
		try {
			Class.forName(getDriverClassName());
			// Give first preference to user's DBCP datasource
			if (hasClass("org.apache.commons.dbcp.BasicDataSource")) {
				org.apache.commons.dbcp.BasicDataSource ds = new org.apache.commons.dbcp.BasicDataSource();
				setBasicDataSourceProperties(ds, serviceInfo);
				return ds;
				// else, we have one from Tomcat
			} else if (hasClass("org.apache.tomcat.dbcp.dbcp.BasicDataSource")) {
				org.apache.tomcat.dbcp.dbcp.BasicDataSource ds = new org.apache.tomcat.dbcp.dbcp.BasicDataSource();
				setBasicDataSourceProperties(ds, serviceInfo);
				return ds;
			} else {
				// Only for testing outside Tomcat/CloudFoundry
				return new SimpleDriverDataSource(DriverManager.getDriver(serviceInfo.getUrl()),
						serviceInfo.getUrl(),
						serviceInfo.getUserName(),
						serviceInfo.getPassword());
			}
		} catch (Exception e) {
			throw new CloudServiceException("Failed to created cloud datasource for " + serviceInfo.getServiceName() + " service", e);
		}
	}

	private void setBasicDataSourceProperties(Object basicDataSource, AbstractDataSourceServiceInfo serviceInfo) {
		BeanWrapper target = new BeanWrapperImpl(basicDataSource);
		target.setPropertyValue("driverClassName", getDriverClassName());
		target.setPropertyValue("url", serviceInfo.getUrl());
		target.setPropertyValue("username", serviceInfo.getUserName());
		target.setPropertyValue("password", serviceInfo.getPassword());
		BeanWrapper source = new BeanWrapperImpl(dataSourceConfiguration);
		for (PropertyDescriptor pd : source.getPropertyDescriptors()) {
			String property = pd.getName();
			if (source.isReadableProperty(pd.getName()) && source.getPropertyValue(pd.getName()) != null) {
				setCorrespondingProperty(pd.getName(), source, target);
			}
		}
	}

	private void setCorrespondingProperty(String property, BeanWrapper source, BeanWrapper target) {
		if (target.isWritableProperty(property)) {
			target.setPropertyValue(property, source.getPropertyValue(property));
		}
	}

	private boolean hasClass(String name) {
		try {
			Class.forName(name);
			return true;
		} catch (Throwable ex) {
			return false;
		}
	}
}
