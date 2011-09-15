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

	private RdbmsServiceCreator rdbmsServiceCreator;


	public CloudDataSourceFactory(CloudEnvironment cloudEnvironment) {
		super(new RdbmsServiceCreator(cloudEnvironment));
		this.rdbmsServiceCreator = (RdbmsServiceCreator) serviceCreator;
	}

	public CloudDataSourceFactory() {
		this(new CloudEnvironment());
	}

	/**
	 * Optional connectionProperties property.
	 *
	 * @param connectionProperties
	 */
	public void setConnectionProperties(String connectionProperties) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setConnectionProperties(connectionProperties);
	}

	/**
	 * Optional validationQuery property.
	 *
	 * @param validationQuery
	 */
	public void setValidationQuery(String validationQuery) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setValidationQuery(validationQuery);
	}

	/**
	 * Optional validationQueryTimeout property.
	 *
	 * @param validationQueryTimeout
	 */
	public void setValidationQueryTimeout(Integer validationQueryTimeout) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setValidationQueryTimeout(validationQueryTimeout);
	}

	/**
	 * Optional testOnBorrow property.
	 *
	 * @param testOnBorrow
	 */
	public void setTestOnBorrow(Boolean testOnBorrow) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setTestOnBorrow(testOnBorrow);
	}

	/**
	 * Optional testOnReturn property.
	 *
	 * @param testOnReturn
	 */
	public void setTestOnReturn(Boolean testOnReturn) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setTestOnReturn(testOnReturn);
	}

	/**
	 * Optional testWhileIdle property.
	 *
	 * @param testWhileIdle
	 */
	public void setTestWhileIdle(Boolean testWhileIdle) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setTestWhileIdle(testWhileIdle);
	}

	/**
	 * Optional initialSize property.
	 *
	 * @param initialSize
	 */
	public void setInitialSize(Integer initialSize) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setInitialSize(initialSize);
	}

	/**
	 * Optional maxActive property.
	 *
	 * @param maxActive
	 */
	public void setMaxActive(Integer maxActive) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setMaxActive(maxActive);
	}

	/**
	 * Optional maxIdle property.
	 *
	 * @param maxIdle
	 */
	public void setMaxIdle(Integer maxIdle) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setMaxIdle(maxIdle);
	}

	/**
	 * Optional minIdle property.
	 *
	 * @param minIdle
	 */
	public void setMinIdle(Integer minIdle) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setMinIdle(minIdle);
	}

	/**
	 * Optional maxWait property.
	 *
	 * @param maxWait
	 */
	public void setMaxWait(Long maxWait) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setMaxWait(maxWait);
	}

	/**
	 * Optional poolPreparedStatements property.
	 *
	 * @param poolPreparedStatements
	 */
	public void setPoolPreparedStatements(Boolean poolPreparedStatements) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setPoolPreparedStatements(poolPreparedStatements);
	}

	/**
	 * Optional maxOpenPreparedStatements property.
	 *
	 * @param maxOpenPreparedStatements
	 */
	public void setMaxOpenPreparedStatements(Integer maxOpenPreparedStatements) {
		this.rdbmsServiceCreator.getDataSourceConfiguration().setMaxOpenPreparedStatements(maxOpenPreparedStatements);
	}

	@Override
	public Class<? extends DataSource> getObjectType() {
		return DataSource.class;
	}
}
