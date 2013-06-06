package org.cloudfoundry.runtime.env;

import java.util.Map;

/**
 * Base information for services that use the common credentials format
 *
 * @author Ramnivas Laddad
 *
 */

public abstract class BaseServiceInfo extends AbstractServiceInfo {
	protected String host;
	protected int port;
	protected String password;

	public BaseServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);

		@SuppressWarnings("unchecked")
		Map<String,Object> credentials = (Map<String, Object>) serviceInfo.get("credentials");

		if (credentials.containsKey("hostname")) {
			host = (String) credentials.get("hostname");
		}
		if (credentials.containsKey("port")) {
			port = Integer.parseInt(credentials.get("port").toString()); // allows the port attribute to be quoted or plain
		}

		if (credentials.containsKey("password")) {
			password = (String) credentials.get("password");
		}
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
