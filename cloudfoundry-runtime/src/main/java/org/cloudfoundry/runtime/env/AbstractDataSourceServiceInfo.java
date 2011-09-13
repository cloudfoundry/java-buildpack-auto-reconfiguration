package org.cloudfoundry.runtime.env;

import java.util.Map;

/**
 * Base information for all database service types
 * 
 * @author Xin Li
 * @author Thomas Risberg
 * 
 **/

abstract public class AbstractDataSourceServiceInfo extends BaseServiceInfo {
	private String database;
	private String userName;

	public AbstractDataSourceServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
		
		@SuppressWarnings("unchecked")
		Map<String, Object> credentials = (Map<String, Object>) serviceInfo.get("credentials");
		database = (String) credentials.get("name");
		userName = (String) credentials.get("user");
	}
	
	abstract public String getUrl();
	
	public String getUserName() {
		return userName;
	}

	public String getDatabase() {
		return database;
	}
}
