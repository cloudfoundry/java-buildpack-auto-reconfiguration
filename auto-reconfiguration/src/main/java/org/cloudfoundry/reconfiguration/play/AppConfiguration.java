package org.cloudfoundry.reconfiguration.play;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.RelationalServiceInfo;

/**
 * The Play app configuration, including any CF services bound to the app
 *
 * @author Jennifer Hickey
 *
 */
public class AppConfiguration {

	private Cloud cloud;

	public AppConfiguration(Cloud cloud) {
		this.cloud = cloud;
	}
	
	public AppConfiguration() {
		this.cloud = new CloudFactory().getCloud();
	}
	

	/**
	 *
	 * @return Properties read from the application config file
	 * @throws IOException
	 */
	public Properties getPlayConfiguration() throws IOException {
		Properties playProps = new Properties();
		String configFile = System.getProperty("config.file");
		if (configFile != null) {
			getPlayConfiguration(playProps, configFile, false);
		} else {
			try {
				getPlayConfiguration(playProps, "application.conf", true);
			}catch(FileNotFoundException e) {
				getPlayConfiguration(playProps, "application.properties", true);
			}
		}
		return playProps;
	}

	private void getPlayConfiguration(Properties playProps, String fileName, boolean loadFromClasspath)
			throws IOException {
		Pattern pattern = Pattern.compile("include \"(.*)\"");
		InputStream inputStream = getInputStream(fileName, loadFromClasspath);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				Matcher matcher = pattern.matcher(strLine);
				if (matcher.find()) {
					getPlayConfiguration(playProps, matcher.group(1), true);
				}
			}
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				System.err.println("Error closing input stream for file " + fileName + ": " + e.getMessage());
			}
		}
		playProps.putAll(getPropertiesFromFile(fileName, loadFromClasspath));
		playProps.remove("include");
	}

	/**
	 *
	 * @return The names databases from Play configuration or an empty set if
	 *         there was an error reading the configuration
	 */
	public Set<String> getPlayDatabaseNames() {
		Set<String> databases = new HashSet<String>();
		Properties props;
		try {
			props = getPlayConfiguration();
		} catch (IOException e) {
			System.err.println("Error reading Play configuration: " + e.getMessage());
			return databases;
		}
		Pattern pattern = Pattern.compile("db.(.*).driver");
		for (Object key : props.keySet()) {
			String candidate = (String) key;
			Matcher matcher = pattern.matcher(candidate);
			if (matcher.find()) {

				databases.add(matcher.group(1));

			}
		}
		return databases;
	}

	/**
	 *
	 * @return Properties loaded from a cloudfoundry.properties file in the Play
	 *         app
	 * @throws IOException
	 *             If file is not found or error reading file
	 */
	public Properties getCFConfiguration() throws IOException {
		return getPropertiesFromFile("cloudfoundry.properties", true);
	}

	/**
	 * @return A single relational database service bound to application, or a
	 *         single database service whose name ends with 'production' or
	 *         'prod'. Null if no such service is found.
	 */
	public RelationalServiceInfo getDatabaseBinding() {
		List<ServiceInfo> dbservices = cloud.getServiceInfos(DataSource.class);
		RelationalServiceInfo serviceInfo = null;
		if (dbservices.size() > 1) {
			for (ServiceInfo dbservice : dbservices) {
				if (dbservice.getId().endsWith("production")
						|| dbservice.getId().endsWith("prod")) {
					if (serviceInfo != null) {
						System.out
								.println("Multiple database services named '*.production' or '*.prod' found.");
						return null;
					}
					serviceInfo = (RelationalServiceInfo) dbservice;
				}
			}
			if (serviceInfo == null) {
				System.out.println("Multiple database services found.");
			}

		} else if (dbservices.size() == 1) {
			serviceInfo = (RelationalServiceInfo) dbservices.get(0);
		} else {
			System.out.println("No database services found.");
		}
		return serviceInfo;
	}
	
	private Properties getPropertiesFromFile(String propFileName, boolean loadFromClasspath)
			throws IOException {
		Properties props = new Properties();
		InputStream inputStream = getInputStream(propFileName, loadFromClasspath);
		try {
			props.load(inputStream);
			return props;
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				System.err.println("Error closing input stream for file " + propFileName + ": "
						+ e.getMessage());
			}
		}
	}

	private InputStream getInputStream(String fileName, boolean loadFromClasspath) throws IOException {
		InputStream inputStream;
		if (loadFromClasspath) {
			inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
		} else {
			inputStream = new FileInputStream(fileName);
		}
		if (inputStream == null) {
			throw new FileNotFoundException("property file '" + fileName + "' not found");
		}
		return inputStream;
	}
}
