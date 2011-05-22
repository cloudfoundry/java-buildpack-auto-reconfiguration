// Copyright (c) 2009-2011 VMware, Inc.

package org.cloudfoundry.reconfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;

/**
 * A servlet filter implementation that is used to generate a property file
 * with service credentials for use by Lift web applications.
 * 
 * @author A.B.Srinivasan
 *
 */
public class CloudLiftServicesPropertiesGeneratorFilter implements Filter {

	static final String PROPERTIES_FILE_PATH = "webapps/ROOT/WEB-INF/classes/props/";
	static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
	static final String PROPERTIES_FILE_SUFFIX = ".props";
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("In CloudLiftServicesPropertiesGeneratorFilter");
		CloudEnvironment cloudEnvironment = new CloudEnvironment();
		try {
			String propertiesFile = getPropertiesFileName();
			createPropertiesFile(propertiesFile);
			generatePropertiesFile(new FileOutputStream(propertiesFile), cloudEnvironment);
		} catch (IOException ioe) {
			throw new ServletException("Property file creation failed: ", ioe.getCause());
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

	void generatePropertiesFile(OutputStream outputStream, CloudEnvironment cloudEnvironment)
	throws IOException {
		Properties props = new Properties();
		// TODO: Currently, only the DB service info is generated into the saved
		// properties file. Mongo / redis / etc. after we have the DB service
		// working end-to-end.
		generateDBServiceProperties(cloudEnvironment, props);
		props.store(outputStream, null);
	}

	// Package scope access to facilitate unit testing.
	String getPropertiesFileName() throws IOException {
		// The properties file naming follows the "<username>.<hostname>.props" pattern
		// as per the conventions laid out in net.liftweb.util.Props
		String username = System.getProperty("user.name");
		String hostname = InetAddress.getLocalHost().getHostName();
		return PROPERTIES_FILE_PATH + username + "." + hostname + PROPERTIES_FILE_SUFFIX;
	}
	
	// Package scope access to facilitate unit testing.
	void createPropertiesFile(String propertiesFileName) throws IOException {
		File propertiesFile = new File(propertiesFileName);
		if (propertiesFile.exists()) {
			String renamedFileName = propertiesFileName + ".bak." +
				new Date(System.currentTimeMillis()).toString();
			propertiesFile.renameTo(new File(renamedFileName));
		}
		File parentPath = new File(PROPERTIES_FILE_PATH);
		if (!parentPath.exists()) {
			if (!parentPath.mkdirs()) {
				throw new IOException("Failed to create parent directory '" +
						PROPERTIES_FILE_PATH + "' for properties file");
			}
		}
		if (!propertiesFile.createNewFile()) {
			throw new IOException("Failed to create properties file '" +
					propertiesFileName + "'");
		}
	}

	// Note: Currently, the following is very specific to the MySql service and works
	// reliably with just one DB instance associated with the application.

	// Package scope access to facilitate unit testing.
	void generateDBServiceProperties(CloudEnvironment cloudEnvironment, Properties props) {

		List<MysqlServiceInfo> dbServiceInfos =
			cloudEnvironment.getServiceInfos(MysqlServiceInfo.class);
		if (dbServiceInfos.size() > 0) {
			MysqlServiceInfo serviceInfo = dbServiceInfos.get(0);
			props.setProperty("db.class", DRIVER_CLASS_NAME);
			props.setProperty("db.url", serviceInfo.getUrl());
			props.setProperty("db.user", serviceInfo.getUserName());
			props.setProperty("db.pass", serviceInfo.getPassword());
		}
	}
}
