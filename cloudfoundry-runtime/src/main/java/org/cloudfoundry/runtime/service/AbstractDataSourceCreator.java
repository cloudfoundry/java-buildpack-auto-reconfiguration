package org.cloudfoundry.runtime.service;

import java.sql.DriverManager;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.AbstractDataSourceServiceInfo;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.service.relational.CloudConnectionConfiguration;
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

	private CloudConnectionConfiguration cloudConnectionConfiguration;
	private CloudPoolConfiguration cloudPoolConfiguration;

	abstract public String getDriverClassName();
	abstract public String getValidationQuery();

	public void setCloudConnectionConfiguration(CloudConnectionConfiguration cloudConnectionConfiguration) {
		this.cloudConnectionConfiguration = cloudConnectionConfiguration;
	}

	public void setCloudPoolConfiguration(CloudPoolConfiguration cloudPoolConfiguration) {
		this.cloudPoolConfiguration = cloudPoolConfiguration;
	}

	/**
	 * Create a datasource based on service info.
	 *
	 * <p>
	 * Tries to create a pooled connection based on either Commons DBCP or
	 * Tomcat's internal DBCP.  The connection pool will validate connections
	 * as they are dispensed.
	 *
	 * <p>
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
		if (getValidationQuery() != null) {
			target.setPropertyValue("validationQuery", getValidationQuery());
			target.setPropertyValue("testOnBorrow", true);
		}
		if (cloudConnectionConfiguration != null) {
			BeanWrapper connectionSource = new BeanWrapperImpl(cloudConnectionConfiguration);
			setCorrespondingProperties(target, connectionSource);
		}
		if (cloudPoolConfiguration != null) {
			BeanWrapper poolSource = new BeanWrapperImpl(cloudPoolConfiguration);
			setCorrespondingProperties(target, poolSource);
		}
	}
}
