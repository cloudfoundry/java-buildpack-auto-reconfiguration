package org.cloudfoundry.runtime.service.relational;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;

/**
 * Spring factory bean for datasource service.
 *
 * @author Ramnivas Laddad
 * @author Thomas Risberg
 *
 */
public class CloudDataSourceFactory extends AbstractCloudServiceFactory<DataSource, RdbmsServiceInfo> {

	public CloudDataSourceFactory(CloudEnvironment cloudEnvironment) {
		super(new RdbmsServiceCreator(cloudEnvironment));
	}

	public CloudDataSourceFactory() {
		super(new RdbmsServiceCreator(new CloudEnvironment()));
	}

	@Override
	public Class<? extends DataSource> getObjectType() {
		return DataSource.class;
	}
}
