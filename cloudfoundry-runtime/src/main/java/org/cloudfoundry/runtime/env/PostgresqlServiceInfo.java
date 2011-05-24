package org.cloudfoundry.runtime.env;

import java.util.Map;


/**
 * Information to access Postgresql service.
 * 
 * @author Jiajun Wang
 */
public class PostgresqlServiceInfo extends AbstractDataSourceServiceInfo {
	public PostgresqlServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
	}
	
	@Override
	public String getUrl() {
		return "jdbc:postgresql://" + getHost() + ":" + + getPort() + "/" + getDatabase();
	}
}
