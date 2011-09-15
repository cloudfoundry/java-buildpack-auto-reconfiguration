package org.cloudfoundry.runtime.service.relational;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.PostgresqlServiceInfo;
import org.cloudfoundry.runtime.service.AbstractDataSourceCreator;

/**
 * Simplified access to PostgreSQL service.
 *
 * @author Jiajun Wang
 * @author Thomas Risberg
 *
 */
public class PostgresqlServiceCreator extends AbstractDataSourceCreator<PostgresqlServiceInfo> {

	protected static final String POSTGRESQL_DRIVER_CLASS_NAME = "org.postgresql.Driver";

	private static final String VALIDATION_QUERY = "SELECT 1";


	public PostgresqlServiceCreator(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, PostgresqlServiceInfo.class);
		getDataSourceConfiguration().setValidationQuery(VALIDATION_QUERY);
		getDataSourceConfiguration().setTestOnBorrow(true);
	}

	@Override
	public String getDriverClassName() {
		return POSTGRESQL_DRIVER_CLASS_NAME;
	}
}
