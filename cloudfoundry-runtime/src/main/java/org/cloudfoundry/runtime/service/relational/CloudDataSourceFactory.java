package org.cloudfoundry.runtime.service.relational;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;
import org.cloudfoundry.runtime.service.CloudConnectionConfiguration;
import org.cloudfoundry.runtime.service.CloudPoolConfiguration;

/**
 * Spring factory bean for datasource service.
 *
 * @author Ramnivas Laddad
 * @author Thomas Risberg
 *
 */
public class CloudDataSourceFactory extends AbstractCloudServiceFactory<DataSource, RdbmsServiceInfo> {

	private RdbmsServiceCreator rdbmsServiceCreator;

	public CloudDataSourceFactory(CloudEnvironment cloudEnvironment) {
		super(new RdbmsServiceCreator(), RdbmsServiceInfo.class, cloudEnvironment);
		this.rdbmsServiceCreator = (RdbmsServiceCreator) super.serviceCreator;
	}

	public CloudDataSourceFactory() {
		this(new CloudEnvironment());
	}

	public void setCloudConnectionConfiguration(CloudConnectionConfiguration cloudConnectionConfiguration) {
		this.rdbmsServiceCreator.setCloudConnectionConfiguration(cloudConnectionConfiguration);
	}

	public void setCloudPoolConfiguration(CloudPoolConfiguration cloudPoolConfiguration) {
		this.rdbmsServiceCreator.setCloudPoolConfiguration(cloudPoolConfiguration);
	}

	@Override
	public Class<? extends DataSource> getObjectType() {
		return DataSource.class;
	}
}
