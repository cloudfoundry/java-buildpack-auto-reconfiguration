package org.cloudfoundry.runtime.service.relational;

import org.cloudfoundry.runtime.env.PostgresqlServiceInfo;
import org.cloudfoundry.runtime.service.AbstractDataSourceCreator;

/**
 * Simplified access to PostgreSQL service.
 *
 * @author Jiajun Wang
 */
public class PostgresqlServiceCreator extends AbstractDataSourceCreator<PostgresqlServiceInfo> {
	
	private static final String POSTGRESQL_DRIVER_CLASS_NAME = "org.postgresql.Driver";
	private static final String VALIDATION_QUERY = "SELECT 1";
	
	@Override
	public String getDriverClassName() {
		return POSTGRESQL_DRIVER_CLASS_NAME;
	}

	@Override
	public String getValidationQuery() {
		return VALIDATION_QUERY;
	}
}
