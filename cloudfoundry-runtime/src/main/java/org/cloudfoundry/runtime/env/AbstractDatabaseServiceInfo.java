package org.cloudfoundry.runtime.env;

import java.util.Map;

/**
 * Service information for database-like services (relational and Mongo currently).
 * <p>
 * These services have username and password for credentials (unlike Redis that has
 * only password). They also have a database name.
 * 
 * @author Ramnivas Laddad
 *
 */
public abstract class AbstractDatabaseServiceInfo extends BaseServiceInfo {

	protected String database;
	protected String userName;

	public AbstractDatabaseServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
		@SuppressWarnings("unchecked")
		Map<String, Object> credentials = 
			(Map<String, Object>) serviceInfo.get("credentials");
		database = (String) credentials.get("name");
		
		userName = (String) credentials.get("user");
		if (userName == null) {
			userName = (String) credentials.get("username");
		}
	}

	public String getUserName() {
		return userName;
	}

	public String getDatabase() {
		return database;
	}

}