package org.cloudfoundry.runtime.service.relational;

import java.sql.DriverManager;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * Simplified access to Mysql service.
 * 
 * @author Ramnivas Laddad
 *
 */
public class MysqlServiceCreator 
    extends AbstractServiceCreator<DataSource, MysqlServiceInfo> {
	
	private static String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
	
	public MysqlServiceCreator(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, MysqlServiceInfo.class);
	}

	/**
	 * Create a datasource based on service info.
	 * 
	 * <p>
	 * Tries to create a pooled connection based on either 
	 */
	public DataSource createService(MysqlServiceInfo serviceInfo) {
		try {
			Class.forName(DRIVER_CLASS_NAME);
			// Give first preference to user's DBCP datasource
			if (hasClass("org.apache.commons.dbcp.BasicDataSource")) {
				org.apache.commons.dbcp.BasicDataSource ds = new org.apache.commons.dbcp.BasicDataSource();
				ds.setDriverClassName(DRIVER_CLASS_NAME);
				ds.setUrl(serviceInfo.getUrl());
				ds.setUsername(serviceInfo.getUserName());
				ds.setPassword(serviceInfo.getPassword());
				return ds;
				// else, we have one from Tomcat
			} else if (hasClass("org.apache.tomcat.dbcp.dbcp.BasicDataSource")) {
				org.apache.tomcat.dbcp.dbcp.BasicDataSource ds = new org.apache.tomcat.dbcp.dbcp.BasicDataSource();
				ds.setDriverClassName(DRIVER_CLASS_NAME);
				ds.setUrl(serviceInfo.getUrl());
				ds.setUsername(serviceInfo.getUserName());
				ds.setPassword(serviceInfo.getPassword());
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

	private boolean hasClass(String name) {
		try {
			Class.forName(name);
			return true;
		} catch (Throwable ex) {
			return false;
		}
	}
	
}
