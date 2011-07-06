package org.cloudfoundry.runtime.env;

import java.util.Map;


/**
 * Information to access Mysql service.
 *
 * @author Ramnivas Laddad
 *
 */

public class MysqlServiceInfo extends AbstractDataSourceServiceInfo {

	public MysqlServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
	}

	@Override
	public String getUrl() {
		return "jdbc:mysql://" + getHost() + ":" + + getPort() + "/" + getDatabase();
	}
}
