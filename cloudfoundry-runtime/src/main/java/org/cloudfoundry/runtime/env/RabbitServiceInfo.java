package org.cloudfoundry.runtime.env;

import java.util.Map;


/**
 * Information to access RabbitMQ service.
 * 
 * <p>
 * This class expects the environment exposed with RabbitMQ service info to be
 * in the following format:
 * 
 * @author Ramnivas Laddad
 * 
 */
public class RabbitServiceInfo extends AbstractServiceInfo {

	private String virtualHost;

	private String userName;

	public RabbitServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
		
		@SuppressWarnings("unchecked")
		Map<String, Object> credentials = 
			(Map<String, Object>) serviceInfo.get("credentials");
		userName = (String) credentials.get("user");
		virtualHost = (String) credentials.get("vhost");
	}

	public String getVirtualHost() {
		return virtualHost;
	}

	public String getUserName() {
		return userName;
	}
}
