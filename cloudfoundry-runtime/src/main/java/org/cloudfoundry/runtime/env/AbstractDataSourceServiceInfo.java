package org.cloudfoundry.runtime.env;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Base information for all database service types
 * 
 * @author Xin Li
 * @author Thomas Risberg
 * 
 **/

abstract public class AbstractDataSourceServiceInfo extends BaseServiceInfo {
	private String database;
	private String userName;

	public AbstractDataSourceServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
		
		@SuppressWarnings("unchecked")
		Map<String, Object> credentials = (Map<String, Object>) serviceInfo.get("credentials");
		database = (String) credentials.get("name");

		if (credentials.containsKey("user")) {
			userName = (String) credentials.get("user");
		} else if (credentials.containsKey("username")) {
			userName = (String) credentials.get("username");
		}
		extractInfoFromUri(credentials);
	}
	
	abstract public String getUrl();
	
	public String getUserName() {
		return userName;
	}

	public String getDatabase() {
		return database;
	}

	private void extractInfoFromUri(Map<String, Object> serviceInfo) {
		if (serviceInfo.containsKey("uri")) {
			String uriString = serviceInfo.get("uri").toString();
			try {
				URI uri = new URI(uriString);
				String userInfo = uri.getUserInfo();
				int userNamePasswordSeparatorIndex = userInfo.indexOf(":");
				userName = userInfo.substring(0, userNamePasswordSeparatorIndex);
				password = userInfo.substring(userNamePasswordSeparatorIndex+1);
				host = uri.getHost();
				port = uri.getPort();
				database = uri.getPath().substring(1); // remove the leading "/"
			} catch (URISyntaxException e) {
				throw new CloudServiceException("Invalid format for postgres URI " + uriString);
			}
		}
	}
}
