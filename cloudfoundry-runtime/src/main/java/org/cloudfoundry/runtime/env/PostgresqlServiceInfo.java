package org.cloudfoundry.runtime.env;

import java.util.Map;


/**
 * Information to access Postgresql service.
 * 
 * @author Jiajun Wang
 */
public class PostgresqlServiceInfo extends AbstractServiceInfo {

	private String database;

	private String userName;

	public PostgresqlServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);

		@SuppressWarnings("unchecked")
		Map<String, Object> credentials = 
			(Map<String, Object>) serviceInfo.get("credentials");
		database = (String) credentials.get("name");
		
		userName = (String) credentials.get("user");
	}
	
	public String getUrl() {
		return "jdbc:postgresql://" + getHost() + ":" + + getPort() + "/" + database;
	}
	
	public String getUserName() {
		return userName;
	}
}
