package org.cloudfoundry.reconfiguration.play;

import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.cloudfoundry.reconfiguration.CloudAutoStagingRuntimeException;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;
import org.springframework.cloud.service.common.RelationalServiceInfo;

/**
 * Sets system properties to be used by any Play app, even those with
 * auto-reconfiguration disabled. This includes cloud-related properties for
 * optional use in configuration and properties to automatically apply database
 * evolutions
 *
 * @author Jennifer Hickey
 *
 */
public class PropertySetter {

	private Cloud cloud;

	static final String MYSQL_DRIVER_CLASS = "com.mysql.jdbc.Driver";

	static final String POSTGRES_DRIVER_CLASS = "org.postgresql.Driver";

	public PropertySetter(Cloud cloud) {
		this.cloud = cloud;
	}

	/**
	 * Set system properties for each cloud property, such that Play user can
	 * refer to these properties in a conf file. For example:
	 * db.default.url=${cloud.services.mysql.connection.url}
	 */
	public void setCloudProperties() {
		Properties cloudProperties = cloud.getCloudProperties();
		for (Entry<Object, Object> entry : cloudProperties.entrySet()) {
			System.setProperty(entry.getKey().toString(), entry.getValue().toString());
		}
		// Set driver and URL system properties
		List<ServiceInfo> dbservices = cloud.getServiceInfos(DataSource.class);
		for (ServiceInfo serviceInfo : dbservices) {
			RelationalServiceInfo dbServiceInfo = (RelationalServiceInfo) serviceInfo;
			System.setProperty("cloud.services." + dbServiceInfo.getId() + ".connection.jdbcUrl",
					dbServiceInfo.getJdbcUrl());
			if (serviceInfo instanceof PostgresqlServiceInfo) {
				System.setProperty("cloud.services." + dbServiceInfo.getId() + ".connection.driver",
						POSTGRES_DRIVER_CLASS);
				if (cloudProperties.containsKey("cloud.services.postgresql.connection.name")) {
					System.setProperty("cloud.services.postgresql.connection.driver", POSTGRES_DRIVER_CLASS);
					System.setProperty("cloud.services.postgresql.connection.jdbcUrl", dbServiceInfo.getJdbcUrl());
				}
			} else if (serviceInfo instanceof MysqlServiceInfo) {
				// Assume MYSQL
				System.setProperty("cloud.services." + dbServiceInfo.getId() + ".connection.driver",
						MYSQL_DRIVER_CLASS);
				if (cloudProperties.containsKey("cloud.services.mysql.connection.name")) {
					System.setProperty("cloud.services.mysql.connection.driver", MYSQL_DRIVER_CLASS);
					System.setProperty("cloud.services.mysql.connection.jdbcUrl", dbServiceInfo.getJdbcUrl());
				}
			} else {
				throw new CloudAutoStagingRuntimeException("Failed to auto-reconfigure application. Unrecognized database service "
						+ dbServiceInfo.getClass().getName() + " found.");
			}
		}
	}

	/**
	 * Sets a system property to apply evolutions to the specified Play databases
	 *
	 * @param playDbNames
	 *            The names of the databases in the Play config
	 */
	public void setDatabaseProperties(Set<String> playDbNames) {
		for(String playDbName: playDbNames) {
			System.setProperty("applyEvolutions." + playDbName, "true");
		}
	}
}
