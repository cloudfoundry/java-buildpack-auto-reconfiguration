package org.cloudfoundry.runtime.env;

import java.util.Map;


/**
 * Service info for Mongo.
 *
 * @author Ramnivas Laddad
 *
 */
public class MongoServiceInfo extends BaseServiceInfo {
	protected String uri;
	protected String database;
	protected String userName;

	public MongoServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
		@SuppressWarnings("unchecked")
		Map<String, Object> credentials =
				(Map<String, Object>) serviceInfo.get("credentials");
		uri = (String) credentials.get("uri");
		userName = (String) credentials.get("username");
		database = (String) credentials.get("db");
	}

	public String getUserName() {
		return userName;
	}

	public String getDatabase() {
		return database;
	}

	public String getUri() {
		return uri;
	}
}
