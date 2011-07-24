package org.cloudfoundry.runtime.env;

import java.util.Map;


/**
 * Service info for Mongo.
 * 
 * @author Ramnivas Laddad
 * 
 */
public class MongoServiceInfo extends AbstractDatabaseServiceInfo {
	public MongoServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
		@SuppressWarnings("unchecked")
		Map<String, Object> credentials = 
			(Map<String, Object>) serviceInfo.get("credentials");
		database = (String) credentials.get("db");
	}
}
