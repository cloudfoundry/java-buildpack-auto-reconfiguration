/*
 * Copyright (c) 2009-2011 VMware, Inc.
 */

package org.cloudfoundry.reconfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;

/**
 * A ServletContextListener implementation that generates a properties file when a Lift web
 * app is initialized to contain service access credentials. Lift web applications consult
 * this properties file when binding to the contained services.
 * 
 * @author A.B.Srinivasan.
 *
 */
public class CloudLiftServicesPropertiesGenerator implements
        ServletContextListener {

    static final String PROPERTIES_FILE_PATH =
        "webapps/ROOT/WEB-INF/classes/props/";
    static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
    static final String PROPERTIES_FILE_SUFFIX = ".props";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        CloudEnvironment cloudEnvironment = new CloudEnvironment();
        try {
            generatePropertiesFile(getPropertiesFileName(), cloudEnvironment);
        } catch (IOException ioe) {
            throw new CloudAutoStagingRuntimeException("Lift services properties file " +
                "generation failed: ", ioe.getCause());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
    
    void generatePropertiesFile(String propertiesFile,
            CloudEnvironment cloudEnvironment)
    throws IOException {
        Properties props = generateDBServiceProperties(cloudEnvironment);
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
            return;
        }
        File parentPath = new File(PROPERTIES_FILE_PATH);
        if (!parentPath.exists()) {
            if (!parentPath.mkdirs()) {
                throw new IOException(
                        "Failed to create parent directory '" +
                        PROPERTIES_FILE_PATH + "' for properties file");
            }
        }
        if (!propertiesFile.createNewFile()) {
            throw new IOException(
                    "Failed to create properties file '" + propertiesFileName + "'");
        }
    }

    Properties generateDBServiceProperties(CloudEnvironment cloudEnvironment) {
        List<MysqlServiceInfo> dbServiceInfos =
            cloudEnvironment.getServiceInfos(MysqlServiceInfo.class);
        Properties props = new Properties();
        if (dbServiceInfos.size() == 0) {
            return props;
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
                return props;
            } else {
                throw new CloudAutoStagingRuntimeException(
                        "Lift services properties file generation failed: " +
                        "application has more than one DB instance bound to it");
            }
        }
    }
}
