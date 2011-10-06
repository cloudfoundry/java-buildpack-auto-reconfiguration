/*
 * Copyright (c) 2009-2011 VMware, Inc.
 */

package org.cloudfoundry.reconfiguration;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit-tests for {@link CloudLiftServicesPropertiesGenerator}.
 *
 * @author A.B.Srinivasan
 *
 */
public class CloudLiftServicesPropertiesGeneratorTest {
    @Mock private RdbmsServiceInfo mockRdbmsServiceInfo1;
    @Mock private RdbmsServiceInfo mockRdbmsServiceInfo2;
    @Mock private CloudEnvironment mockEnvironment;

    private CloudLiftServicesPropertiesGenerator
        testLiftPropertiesGenerator;
    private String fileName = "/tmp/cloudLiftServicesProperties.props";
    private String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
    private String serviceUserName = "testUser";
    private String servicePassword = "testPassword";


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        testLiftPropertiesGenerator =
            new CloudLiftServicesPropertiesGenerator();
    }

    @After
    public void tearDown() {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void cloudLiftServicesPropertiesFileNamedCorrectly()
    throws IOException {
        String propertiesFileName = null;
        String generatedName = null;
        propertiesFileName =
            CloudLiftServicesPropertiesGenerator.PROPERTIES_FILE_PATH +
            System.getProperty("user.name") + "." +
            InetAddress.getLocalHost().getHostName() +
            CloudLiftServicesPropertiesGenerator.PROPERTIES_FILE_SUFFIX;

        generatedName = testLiftPropertiesGenerator.getPropertiesFileName();

        Assert.assertNotNull(propertiesFileName);
        Assert.assertNotNull(generatedName);
        Assert.assertEquals(propertiesFileName, generatedName);
    }

    @Test
    public void cloudLiftServicesPropertiesGeneratorCreatesFilesCorrectly()
    throws IOException {
        testLiftPropertiesGenerator.createPropertiesFile(fileName);
        Assert.assertTrue(new File(fileName).exists());
    }

    @Test (expected=CloudAutoStagingRuntimeException.class)
    public void cloudLiftServicesPropertiesGenerationFailsIfMultipleMysqlServicesPresent() {
        setUpMultipleRdbmsServicesMock();

        testLiftPropertiesGenerator.generateDBServiceProperties(mockEnvironment);
    }

    @Test
    public void cloudLiftServicesPropertiesNotGeneratedIfNoMysqlServicePresent()
    throws IOException {
        setUpNoRdbmsServiceMock();

        Properties props = testLiftPropertiesGenerator.generateDBServiceProperties(
            mockEnvironment);

        Assert.assertTrue(props.isEmpty());
    }

    @Test
    public void cloudLiftServicesPropertiesFileNotCreatedIfNoRdbmsServicesPresent()
    throws IOException {
        setUpNoRdbmsServiceMock();

        testLiftPropertiesGenerator.generatePropertiesFile(
                fileName, mockEnvironment);

        Assert.assertFalse(new File(fileName).exists());
    }

    @Test
    public void cloudLiftServicesPropertiesFileNotCreatedIfNoMysqlServicesPresent()
    throws IOException {
        setUpRdbmsServiceNotMysqlMock();

        testLiftPropertiesGenerator.generatePropertiesFile(
                fileName, mockEnvironment);

        Assert.assertFalse(new File(fileName).exists());
    }

    @Test
    public void cloudLiftServicesPropertiesGeneratedCorrectlyForMysqlServiceIfOnePresent()
    throws IOException {
        setUpSingleMysqlServiceMock();

        Properties props = testLiftPropertiesGenerator.generateDBServiceProperties(
            mockEnvironment);

        Assert.assertEquals(
            CloudLiftServicesPropertiesGenerator.MYSQL_DRIVER_CLASS_NAME,
            props.getProperty("db.class"));
        Assert.assertEquals(serviceJdbcUrl, props.getProperty("db.url"));
        Assert.assertEquals(serviceUserName, props.getProperty("db.user"));
        Assert.assertEquals(servicePassword, props.getProperty("db.pass"));
    }

    @Test
    public void cloudLiftServicesPropertiesFileGeneratedCorrectlyForMysqlServiceIfOnePresent()
    throws IOException {
        setUpSingleMysqlServiceMock();

        testLiftPropertiesGenerator.generatePropertiesFile(
            fileName, mockEnvironment);
        Properties properties = new Properties();
        properties.load(new FileInputStream(fileName));

        Assert.assertEquals(
            CloudLiftServicesPropertiesGenerator.MYSQL_DRIVER_CLASS_NAME,
            properties.getProperty("db.class"));
        Assert.assertEquals(serviceJdbcUrl, properties.getProperty("db.url"));
        Assert.assertEquals(serviceUserName, properties.getProperty("db.user"));
        Assert.assertEquals(servicePassword, properties.getProperty("db.pass"));
    }

    private void setUpNoRdbmsServiceMock() {
        List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();
        when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).
            thenReturn(serviceInfos);
    }

    private void setUpSingleMysqlServiceMock() {
        List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();

        serviceInfos.add(mockRdbmsServiceInfo1);
        when(mockRdbmsServiceInfo1.getLabel()).thenReturn("mysql-5.1");
        when(mockRdbmsServiceInfo1.getUrl()).thenReturn(serviceJdbcUrl);
        when(mockRdbmsServiceInfo1.getUserName()).thenReturn(serviceUserName);
        when(mockRdbmsServiceInfo1.getPassword()).thenReturn(servicePassword);

        when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).
            thenReturn(serviceInfos);
    }

    private void setUpMultipleRdbmsServicesMock() {
        List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();

        serviceInfos.add(mockRdbmsServiceInfo1);
        when(mockRdbmsServiceInfo1.getLabel()).thenReturn("mysql-5.1");
        when(mockRdbmsServiceInfo1.getUrl()).thenReturn(serviceJdbcUrl);
        when(mockRdbmsServiceInfo1.getUserName()).thenReturn(serviceUserName);
        when(mockRdbmsServiceInfo1.getPassword()).thenReturn(servicePassword);

        serviceInfos.add(mockRdbmsServiceInfo2);
        when(mockRdbmsServiceInfo2.getLabel()).thenReturn("mysql-5.1");
        when(mockRdbmsServiceInfo2.getUrl()).thenReturn(serviceJdbcUrl);
        when(mockRdbmsServiceInfo2.getUserName()).thenReturn(serviceUserName);
        when(mockRdbmsServiceInfo2.getPassword()).thenReturn(servicePassword);

        when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).
            thenReturn(serviceInfos);
    }

    private void setUpRdbmsServiceNotMysqlMock() {
		List<RdbmsServiceInfo> serviceInfos = new ArrayList<RdbmsServiceInfo>();
		serviceInfos.add(mockRdbmsServiceInfo1);
        when(mockRdbmsServiceInfo1.getLabel()).thenReturn("postgresql-9.0");
        when(mockEnvironment.getServiceInfos(RdbmsServiceInfo.class)).
             thenReturn(serviceInfos);
    }
}
