package org.cloudfoundry.reconfiguration.play;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.cloudfoundry.reconfiguration.CloudAutoStagingRuntimeException;
import org.cloudfoundry.reconfiguration.Constants;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;

/**
 * Configures Play apps by setting service-related system properties for use by
 * any application and auto-reconfiguring database connections.
 * Auto-reconfiguration is done by setting system properties to override DB
 * configuration and point to a single database service bound to the
 * application.
 *
 * @author Jennifer Hickey
 *
 */
public class Configurer {

	private PropertySetter propertySetter;

	private AppConfiguration appConfiguration;

	public Configurer(AppConfiguration appConfiguration, PropertySetter propertySetter) {
		this.appConfiguration = appConfiguration;
		this.propertySetter = propertySetter;
	}

	public void configure() {
		propertySetter.setCloudProperties();
		Properties props;
		try {
			props = appConfiguration.getPlayConfiguration();
		} catch (IOException e) {
			System.err.println("Error reading Play configuration: " + e.getMessage()
					+ ".  Skipping auto-reconfiguration.");
			return;
		}
		Set<String> dbnames = appConfiguration.getPlayDatabaseNames();
		if(dbnames.isEmpty()) {
			System.out.println("No database found in Play configuration.  Skipping auto-reconfiguration.");
			return;
		}
		propertySetter.setDatabaseProperties(dbnames);
		if(dbnames.size() > 1) {
			System.out.println("Found multiple databases in Play configuration.  Skipping auto-reconfiguration.");
			return;
		}
		String dbName = dbnames.iterator().next();
		if (autoconfigDisabled(props)) {
			return;
		}
		RdbmsServiceInfo dbServiceInfo = appConfiguration.getDatabaseBinding();
		if (dbServiceInfo == null) {
			System.out
					.println("Found 0 or multiple database services bound to app.  Skipping auto-reconfiguration.");
			return;
		}
		configureDatabase(dbName, props, dbServiceInfo);
		configureJpa(props);
	}

	boolean autoconfigDisabled(Properties appProps) {
		return autoconfigDisabledByFile() || autoconfigDisabledByCFRuntime()
				|| autoconfigDisabledByUseofSysProps(appProps);
	}

	/**
	 * Disable auto-reconfiguration if cf config file is found on classpath and
	 * contains the entry autoconfig:false
	 *
	 * @return true if file is found and contains the entry autoconfig:false
	 */
	private boolean autoconfigDisabledByFile() {
		try {
			Properties cfProperties = appConfiguration.getCFConfiguration();
			String autoconfig = cfProperties.getProperty("autoconfig");
			if ("false".equals(autoconfig)) {
				System.out.println("User disabled auto-reconfiguration");
				return true;
			}
		} catch (FileNotFoundException e) {
			// No file found
		} catch (IOException e) {
			System.err.println("Error reading cloudfoundry properties: " + e.getMessage()
					+ ".  Auto-reconfiguration will be enabled.");
		}
		return false;
	}

	/**
	 * Disable auto-reconfiguration if user is including cf-runtime jar. We
	 * don't know of a use case for this yet, but could be a module developed,
	 * etc in future
	 *
	 * @return true if cf-runtime jar is present
	 */
	private boolean autoconfigDisabledByCFRuntime() {
		final Pattern pattern = Pattern.compile("cloudfoundry-runtime-.*.jar");
		File dir = new File("lib");
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return pattern.matcher(file.getName()).find();
			}
		});
		if (files != null && files.length > 0) {
			System.out.println("Found cloudfoundry-runtime lib.  Auto-reconfiguration disabled.");
			return true;
		}
		return false;
	}

	/**
	 * Disable auto-reconfiguration if user is using a cloud property in their
	 * config file
	 *
	 * @param appProps
	 *            Properties from the Play app config file
	 * @return true if props are using cloud property placeholder
	 */
	private boolean autoconfigDisabledByUseofSysProps(Properties appProps) {
		for (Object value : appProps.values()) {
			if (((String) value).contains("${cloud.") || ((String) value).contains("${?cloud.")) {
				System.out
						.println("Found cloud properties in configuration.  Auto-reconfiguration disabled.");
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets system properties that will override DB settings in Play app
	 *
	 * @param dbName
	 *            The name of the database in Play app
	 * @param props
	 *            The user configuration
	 * @param dbServiceInfo
	 *            The relational DB service bound to app
	 */
	private void configureDatabase(String dbName, Properties props, RdbmsServiceInfo dbServiceInfo) {
		System.out.println("Auto-reconfiguring " + dbName);
		System.setProperty("db." + dbName + ".url", dbServiceInfo.getUrl());
		System.setProperty("db." + dbName + ".user", dbServiceInfo.getUserName());
		System.setProperty("db." + dbName + ".password", dbServiceInfo.getPassword());
		if (dbServiceInfo.getLabel().startsWith(Constants.POSTGRES_LABEL_START)) {
			System.setProperty("db." + dbName + ".driver", PropertySetter.POSTGRES_DRIVER_CLASS);
		} else if (dbServiceInfo.getLabel().startsWith(Constants.MYSQL_LABEL_START)){
			System.setProperty("db." + dbName + ".driver", PropertySetter.MYSQL_DRIVER_CLASS);
		} else {
			throw new CloudAutoStagingRuntimeException("Failed to auto-reconfigure application. Unrecognized database service with label "
					+ dbServiceInfo.getLabel() + " found.");
		}
	}

	/**
	 * Activates our JPAPlugin and disables Play's JPAPlugin, so the proper
	 * dialect will be applied
	 *
	 * @param props
	 *            The user configuration
	 */
	private void configureJpa(Properties props) {
		String jpaPluginStatus = props.getProperty("jpaplugin");
		if (!"disabled".equals(jpaPluginStatus)) {
			System.setProperty("cfjpaplugin", "enabled");
			System.out.println("Enabling JPA auto-reconfiguration");
		}
		System.setProperty("jpaplugin", "disabled");
	}
}
