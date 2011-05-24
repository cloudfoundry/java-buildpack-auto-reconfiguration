package org.cloudfoundry.runtime.service.relational;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;

/**
 * Spring factory bean for datasource service.
 * 
 * @author Ramnivas Laddad
 *
 */
public class CloudMySqlDataSourceFactory extends AbstractCloudServiceFactory<DataSource, MysqlServiceInfo> {
	public CloudMySqlDataSourceFactory(CloudEnvironment cloudEnvironment) {
		super(new MysqlServiceCreator(cloudEnvironment));
	}

	public CloudMySqlDataSourceFactory() {
		super(new MysqlServiceCreator(new CloudEnvironment()));
	}

	@Override
	public Class<? extends DataSource> getObjectType() {
		return DataSource.class;
	}
}
