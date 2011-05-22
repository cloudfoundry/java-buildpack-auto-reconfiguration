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

    static final String PROPERTIES_FILE_PATH =
        "webapps/ROOT/WEB-INF/classes/props/";
    static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
    static final String PROPERTIES_FILE_SUFFIX = ".props";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        CloudEnvironment cloudEnvironment = new CloudEnvironment();
        try {
            generatePropertiesFile(getPropertiesFileName(), cloudEnvironment);
        } catch (IOException ioe) {
            throw new ServletException("Lift services properties file " +
                "generation failed: ", ioe.getCause());
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

    void generatePropertiesFile(String propertiesFile,
        CloudEnvironment cloudEnvironment)
        throws ServletException, IOException {
        Properties props = new Properties();
        generateDBServiceProperties(cloudEnvironment, props);
        if (!props.isEmpty()) {
            createPropertiesFile(propertiesFile);
            props.store(new FileOutputStream(propertiesFile), null);
        }
    }

    String getPropertiesFileName() throws IOException {
        // The properties file naming follows the "<username>.<hostname>.props"
        // pattern as per the conventions laid out in net.liftweb.util.Props
        String username = System.getProperty("user.name");
        String hostname = InetAddress.getLocalHost().getHostName();
        return PROPERTIES_FILE_PATH + username + "." +
            hostname + PROPERTIES_FILE_SUFFIX;
    }

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
                throw new IOException(
                    "Failed to create parent directory '" +
                    PROPERTIES_FILE_PATH +
                    "' for properties file");
            }
        }
        if (!propertiesFile.createNewFile()) {
            throw new IOException(
                "Failed to create properties file '" +
                propertiesFileName + "'");
        }
    }

    void generateDBServiceProperties(CloudEnvironment cloudEnvironment,
        Properties props) throws ServletException {
        List<MysqlServiceInfo> dbServiceInfos =
            cloudEnvironment.getServiceInfos(MysqlServiceInfo.class);
        if (dbServiceInfos.size() == 0) {
            return;
        } else {
            // Note: Currently, the following is very specific to the
            // MySql service and works only if there is one DB instance
            // associated with the application.
            if (dbServiceInfos.size() == 1) {
                MysqlServiceInfo serviceInfo = dbServiceInfos.get(0);
                props.setProperty("db.class", DRIVER_CLASS_NAME);
                props.setProperty("db.url", serviceInfo.getUrl());
                props.setProperty("db.user", serviceInfo.getUserName());
                props.setProperty("db.pass", serviceInfo.getPassword());
            } else {
                throw new ServletException(
                    "Lift services properties file generation failed: " +
                    "application has more than one DB instance bound to it");
            }
        }
    }
}
