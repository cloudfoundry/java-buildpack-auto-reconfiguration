package org.cloudfoundry.runtime.service.relational;

import org.cloudfoundry.runtime.env.*;
import org.cloudfoundry.runtime.service.AbstractDataSourceCreator;
import org.springframework.util.Assert;

import javax.sql.DataSource;

/**
 * Simplified access to RDBMS service.
 *
 * @author Thomas Risberg
 *
 */
public class RdbmsServiceCreator extends AbstractDataSourceCreator<RdbmsServiceInfo> {

	private String driverClassName;

	public RdbmsServiceCreator(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, RdbmsServiceInfo.class);
	}

	@Override
	public DataSource createService(AbstractDataSourceServiceInfo serviceInfo) {
		if (serviceInfo.getLabel() != null && serviceInfo.getLabel().startsWith("postgres")) {
			this.driverClassName = PostgresqlServiceCreator.POSTGRESQL_DRIVER_CLASS_NAME;
		}
		else {
			this.driverClassName = MysqlServiceCreator.MYSQL_DRIVER_CLASS_NAME;
		}
		return super.createService(serviceInfo);
	}

	@Override
	public String getDriverClassName() {
		return this.driverClassName;
	}
}
