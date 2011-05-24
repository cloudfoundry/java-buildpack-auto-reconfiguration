package org.cloudfoundry.runtime.env;

import java.util.Map;

/**
 * Base information for all service types
 * 
 * @author Ramnivas Laddad
 *
 */
public abstract class AbstractServiceInfo {
	private String serviceName;
	private String plan;
	
	private String host;
	private int port;
	private String password;

	public AbstractServiceInfo(Map<String, Object> serviceInfo) {
		serviceName = (String) serviceInfo.get("name");
		plan = (String) serviceInfo.get("plan");
		
		@SuppressWarnings("unchecked")
		Map<String,Object> credentials = (Map<String, Object>) serviceInfo.get("credentials");
		host = (String) credentials.get("hostname");
		port = (Integer) credentials.get("port");
		
		password = (String) credentials.get("password");
		if (password == null) {
			// For rabbitmq that uses 'pass'
			password = (String) credentials.get("pass");
		}
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getPlan() {
		return plan;
	}
	
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getPassword() {
		return password;
	}
}
