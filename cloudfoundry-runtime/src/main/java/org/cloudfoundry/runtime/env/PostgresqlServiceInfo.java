package org.cloudfoundry.runtime.env;

import java.util.Map;


/**
 * Information to access Postgresql service.
 * 
 * @author Jiajun Wang
 */
public class PostgresqlServiceInfo extends AbstractDataSourceServiceInfo {
	
	private static final String POSTGRESQL_DRIVER_CLASS_NAME = "org.postgresql.Driver";
	
	public PostgresqlServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
	}
	
	@Override
	public String getDriverClassName() {
		return POSTGRESQL_DRIVER_CLASS_NAME;
	}

	@Override
	public String getUrl() {
		return "jdbc:postgresql://" + getHost() + ":" + + getPort() + "/" + getDatabase();
	}
}
