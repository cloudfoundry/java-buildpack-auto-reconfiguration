package org.cloudfoundry.runtime.env;

import java.util.Map;


/**
 * Service info for Redis.
 * 
 * @author Ramnivas Laddad
 * 
 */
public class RedisServiceInfo extends AbstractServiceInfo {
	public RedisServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
	}
}