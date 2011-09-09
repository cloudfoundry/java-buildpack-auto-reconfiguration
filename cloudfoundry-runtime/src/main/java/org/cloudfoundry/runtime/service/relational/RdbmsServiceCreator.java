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

	private AbstractDataSourceCreator delegate;
	private CloudEnvironment cloudEnvironment;

	public RdbmsServiceCreator(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, RdbmsServiceInfo.class);
		this.cloudEnvironment = cloudEnvironment;
	}

	@Override
	public DataSource createService(AbstractDataSourceServiceInfo serviceInfo) {
		if (serviceInfo.getLabel() != null && serviceInfo.getLabel().startsWith("postgres")) {
			this.delegate = new PostgresqlServiceCreator(cloudEnvironment);
		}
		else {
			this.delegate = new MysqlServiceCreator(cloudEnvironment);
		}
		return super.createService(serviceInfo);
	}

	@Override
	public String getDriverClassName() {
		Assert.notNull(delegate, "DataSourceCreator delegate was not populated");
		return delegate.getDriverClassName();
	}

	@Override
	public String getValidationQuery() {
		Assert.notNull(delegate, "DataSourceCreator delegate was not populated");
		return delegate.getValidationQuery();
	}
}
