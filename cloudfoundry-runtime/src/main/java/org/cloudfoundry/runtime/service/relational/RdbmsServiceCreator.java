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

	@Override
	public DataSource createService(AbstractDataSourceServiceInfo serviceInfo) {
		if (serviceInfo.getLabel() != null && serviceInfo.getLabel().startsWith("postgres")) {
			this.delegate = new PostgresqlServiceCreator();
		}
		else {
			this.delegate = new MysqlServiceCreator();
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
