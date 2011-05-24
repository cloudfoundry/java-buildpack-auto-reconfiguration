package org.cloudfoundry.runtime.service.relational;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.PostgresqlServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;

/**
 * Spring factory bean for PostgreSQL datasource service.
 * 
 * @author Jiajun Wang
 *
 */
public class CloudDataSourceFactoryPostgresql extends AbstractCloudServiceFactory<DataSource, PostgresqlServiceInfo> {
	public CloudDataSourceFactoryPostgresql(CloudEnvironment cloudEnvironment) {
		super(new PostgresqlServiceCreator(cloudEnvironment));
	}

	public CloudDataSourceFactoryPostgresql() {
		super(new PostgresqlServiceCreator(new CloudEnvironment()));
	}

	@Override
	public Class<? extends DataSource> getObjectType() {
		return DataSource.class;
	}
}
