package org.cloudfoundry.runtime.env;

import java.util.Map;


/**
 * Service info for Mongo.
 * 
 * @author Ramnivas Laddad
 * 
 */
public class MongoServiceInfo extends BaseServiceInfo {
	public MongoServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
	}
}
