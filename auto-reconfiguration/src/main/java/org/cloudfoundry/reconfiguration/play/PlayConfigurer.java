package org.cloudfoundry.reconfiguration.play;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;

/**
 * Handles auto-reconfiguration for Play apps by setting system properties to
 * override DB configuration and point to a single database service bound to the
 * application
 *
 * @author Jennifer Hickey
 *
 */
public class PlayConfigurer {

	private CloudEnvironment cloudEnvironment;

	static final String MYSQL_DRIVER_CLASS = "com.mysql.jdbc.Driver";

	static final String POSTGRES_DRIVER_CLASS = "org.postgresql.Driver";

	public PlayConfigurer(CloudEnvironment cloudEnvironment) {
		this.cloudEnvironment = cloudEnvironment;
	}

	public void configure() {
		String configFile = System.getProperty("config.file", "application.conf");
		Properties props;
		try {
			props = getPropertiesFromClasspath(configFile);
		} catch (IOException e) {
			System.err.println("Error reading config from " + configFile + ": "
					+ e.getMessage() + ".  Skipping auto-reconfiguration.");
			return;
		}
		String dbName = getPlayDatabaseName(props);
		if (dbName != null) {
			//Apply evolutions, even if we don't auto-reconfigure
			System.setProperty("applyEvolutions." + dbName, "true");
		}
		if (autoconfigDisabled("conf/cloudfoundry.properties")) {
			System.out.println("Auto-reconfiguration disabled");
			return;
		}
		RdbmsServiceInfo dbServiceInfo = getDatabaseBinding();
		if (dbServiceInfo == null) {
			return;
		}
		if (dbName != null) {
			//Apply evolutions, even if we don't auto-reconfigure
			System.setProperty("applyEvolutions." + dbName, "true");
			configureDatabase(dbName, props, dbServiceInfo);
			configureJpa(props);
		}
	}

	public RdbmsServiceInfo getDatabaseBinding() {
		List<RdbmsServiceInfo> dbservices = cloudEnvironment
				.getServiceInfos(RdbmsServiceInfo.class);
		RdbmsServiceInfo serviceInfo = null;
		if (dbservices.size() > 1) {
			for (RdbmsServiceInfo dbservice : dbservices) {
				if (dbservice.getServiceName().endsWith("production")
						|| dbservice.getServiceName().endsWith("prod")) {
					if (serviceInfo != null) {
						System.out
								.println("Multiple database services named '*.production' or '*.prod' found."
										+ "  Skipping auto-reconfiguration");
						return null;
					}
					serviceInfo = dbservice;
				}
			}
			if (serviceInfo == null) {
				System.out
						.println("Multiple database services found.  Skipping auto-reconfiguration.");
			}

		} else if (dbservices.size() == 1) {
			serviceInfo = dbservices.get(0);
		} else {
			System.out
					.println("No database services found.  Skipping auto-reconfiguration.");
		}
		return serviceInfo;
	}

	boolean autoconfigDisabled(String cfConfigFile) {
		File dir = new File("lib");
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return Pattern.compile("cloudfoundry-runtime-*.jar")
						.matcher(file.getName()).find();
			}
		});
		if (files != null && files.length > 0) {
			System.out
					.println("Found cloudfoundry-runtime lib.  Auto-reconfiguration disabled.");
			return true;
		}
		try {
			Properties cfProperties = getPropertiesFromClasspath(cfConfigFile);
			String autoconfig = cfProperties.getProperty("autoconfig");
			if ("false".equals(autoconfig)) {
				return true;
			}
		} catch (FileNotFoundException e) {
			// No file found
		} catch (IOException e) {
			System.err
					.println("Error reading conf/cloudfoundry.properties.  Auto-reconfiguration will be enabled.");
		}
		return false;
	}

	private void configureDatabase(String dbName, Properties props,
			RdbmsServiceInfo dbServiceInfo) {
		System.out.println("Auto-reconfiguring " + dbName);
		System.setProperty("db." + dbName + ".url", dbServiceInfo.getUrl());
		System.setProperty("db." + dbName + ".user",
				dbServiceInfo.getUserName());
		System.setProperty("db." + dbName + ".password",
				dbServiceInfo.getPassword());
		if (dbServiceInfo.getLabel().startsWith("postgres")) {
			System.setProperty("db." + dbName + ".driver",
					POSTGRES_DRIVER_CLASS);
		} else {
			// Assume MySQL
			System.setProperty("db." + dbName + ".driver", MYSQL_DRIVER_CLASS);
		}
	}

	private void configureJpa(Properties props) {
		String jpaPluginStatus = props.getProperty("jpaplugin");
		if (!"disabled".equals(jpaPluginStatus)) {
			System.setProperty("cfjpaplugin", "enabled");
			System.out.println("Enabling JPA auto-reconfiguration");
		}
		// Disable Play's JPAPlugin so ours will be used instead
		System.setProperty("jpaplugin", "disabled");
	}

	private String getPlayDatabaseName(Properties props) {
		String databaseName = null;
		Pattern pattern = Pattern.compile("db.(.*).driver");
		for (Object key : props.keySet()) {
			String candidate = (String) key;
			Matcher matcher = pattern.matcher(candidate);
			if (matcher.find()) {
				if (databaseName != null) {
					System.out
							.println("Found multiple databases in Play configuration.  Skipping auto-reconfiguration");
					return null;
				}
				databaseName = matcher.group(1);
			}
		}
		return databaseName;
	}

	private Properties getPropertiesFromClasspath(String propFileName)
			throws IOException {
		// loading xmlProfileGen.properties from the classpath
		Properties props = new Properties();
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream(propFileName);
		if (inputStream == null) {
			throw new FileNotFoundException("property file '" + propFileName
					+ "' not found in the classpath");
		}
		props.load(inputStream);
		return props;
	}
}
