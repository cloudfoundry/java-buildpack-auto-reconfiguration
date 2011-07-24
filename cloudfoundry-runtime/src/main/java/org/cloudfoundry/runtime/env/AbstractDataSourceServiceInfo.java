package org.cloudfoundry.runtime.env;

import java.util.Map;

/**
 * Base information for all database service types
 * 
 * @author Xin Li
 * 
 **/

abstract public class AbstractDataSourceServiceInfo extends AbstractDatabaseServiceInfo {
	public AbstractDataSourceServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
	}
	
	abstract public String getUrl();
}
