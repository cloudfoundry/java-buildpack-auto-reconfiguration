package org.cloudfoundry.reconfiguration.play;

import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;

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

	private CloudEnvironment cloudEnvironment;

	static final String MYSQL_DRIVER_CLASS = "com.mysql.jdbc.Driver";

	static final String POSTGRES_DRIVER_CLASS = "org.postgresql.Driver";

	public PropertySetter(CloudEnvironment cloudEnvironment) {
		this.cloudEnvironment = cloudEnvironment;
	}

	/**
	 * Set system properties for each cloud property, such that Play user can
	 * refer to these properties in a conf file. For example:
	 * db.default.url=${cloud.services.mysql.connection.url}
	 */
	public void setCloudProperties() {
		Properties cloudProperties = cloudEnvironment.getCloudProperties();
		for (Entry<Object, Object> entry : cloudProperties.entrySet()) {
			System.setProperty((String) entry.getKey(), (String) entry.getValue());
		}
		// Set driver and URL system properties
		List<RdbmsServiceInfo> dbservices = cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class);
		for (RdbmsServiceInfo service : dbservices) {
			System.setProperty("cloud.services." + service.getServiceName() + ".connection.url",
					service.getUrl());
			if (service.getLabel().startsWith("postgresql")) {
				System.setProperty("cloud.services." + service.getServiceName() + ".connection.driver",
						POSTGRES_DRIVER_CLASS);
				if (cloudProperties.containsKey("cloud.services.postgresql.connection.name")) {
					System.setProperty("cloud.services.postgresql.connection.driver", POSTGRES_DRIVER_CLASS);
					System.setProperty("cloud.services.postgresql.connection.url", service.getUrl());
				}
			} else {
				// Assume MYSQL
				System.setProperty("cloud.services." + service.getServiceName() + ".connection.driver",
						MYSQL_DRIVER_CLASS);
				if (cloudProperties.containsKey("cloud.services.mysql.connection.name")) {
					System.setProperty("cloud.services.mysql.connection.driver", MYSQL_DRIVER_CLASS);
					System.setProperty("cloud.services.mysql.connection.url", service.getUrl());
				}
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
