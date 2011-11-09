package org.cloudfoundry.runtime.env;

import java.util.Map;


/**
 * Information to access Mysql service.
 *
 * @author Ramnivas Laddad
 *
 */

public class MysqlServiceInfo extends AbstractDataSourceServiceInfo {

	private static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

	public MysqlServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
	}

	@Override
	public String getDriverClassName() {
		return MYSQL_DRIVER_CLASS_NAME;
	}

	@Override
	public String getUrl() {
		return "jdbc:mysql://" + getHost() + ":" + + getPort() + "/" + getDatabase();
	}

}
