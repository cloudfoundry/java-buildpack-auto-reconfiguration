package org.cloudfoundry.runtime.env;

import java.util.Map;

/**
 * Base information for services that use the common credentials format
 *
 * @author Ramnivas Laddad
 *
 */

public abstract class BaseServiceInfo extends AbstractServiceInfo {
	private String host;
	private int port;
	private String password;

	public BaseServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);

		@SuppressWarnings("unchecked")
		Map<String,Object> credentials = (Map<String, Object>) serviceInfo.get("credentials");
		host = (String) credentials.get("hostname");
		port = (Integer) credentials.get("port");

		password = (String) credentials.get("password");
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
