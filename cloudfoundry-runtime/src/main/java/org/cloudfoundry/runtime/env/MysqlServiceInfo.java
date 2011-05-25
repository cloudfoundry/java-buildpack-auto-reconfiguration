package org.cloudfoundry.runtime.env;

import java.util.Map;


/**
 * Information to access Mysql service.
 *
 * @author Ramnivas Laddad
 *
 */
public class MysqlServiceInfo extends AbstractServiceInfo {

	private String database;

	private String userName;

	public MysqlServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);

		@SuppressWarnings("unchecked")
		Map<String, Object> credentials =
			(Map<String, Object>) serviceInfo.get("credentials");
		database = (String) credentials.get("name");

		userName = (String) credentials.get("user");
	}

	public String getUrl() {
		return "jdbc:mysql://" + getHost() + ":" + + getPort() + "/" + database;
	}

	public String getUserName() {
		return userName;
	}
}
