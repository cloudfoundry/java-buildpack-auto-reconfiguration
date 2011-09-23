package org.cloudfoundry.runtime.service.relational;

import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.cloudfoundry.runtime.service.AbstractDataSourceCreator;

/**
 * Simplified access to Mysql service.
 * 
 * @author Ramnivas Laddad
 *
 */
public class MysqlServiceCreator extends AbstractDataSourceCreator<MysqlServiceInfo> {
	
	private static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

	/**
	 * Validation query obtained from the MySQL reference manual:
	 * http://dev.mysql.com/doc/refman/5.1/en/connector-j-usagenotes-j2ee.html
	 */
	private static final String VALIDATION_QUERY = "/* ping */ SELECT 1";
	
	@Override
	public String getDriverClassName() {
		return MYSQL_DRIVER_CLASS_NAME;
	}
	
	@Override
	public String getValidationQuery() {
		return VALIDATION_QUERY;
	}
}
