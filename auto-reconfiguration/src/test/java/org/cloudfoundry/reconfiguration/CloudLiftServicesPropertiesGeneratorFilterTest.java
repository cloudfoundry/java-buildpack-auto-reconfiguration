package org.cloudfoundry.reconfiguration;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit-tests for {@link CloudLiftServicesPropertiesGeneratorFilter}.
 *
 * @author A.B.Srinivasan
 *
 */
public class CloudLiftServicesPropertiesGeneratorFilterTest {
    @Mock private MysqlServiceInfo mockMysqlServiceInfo1;
    @Mock private MysqlServiceInfo mockMysqlServiceInfo2;
    @Mock private CloudEnvironment mockEnvironment;

    private CloudLiftServicesPropertiesGeneratorFilter
        testLiftPropertiesGeneratorFilter;
    private String fileName = "/tmp/cloudLiftServicesProperties.props";
    private String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
    private String serviceUserName = "testUser";
    private String servicePassword = "testPassword";


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        testLiftPropertiesGeneratorFilter =
            new CloudLiftServicesPropertiesGeneratorFilter();
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
            CloudLiftServicesPropertiesGeneratorFilter.PROPERTIES_FILE_PATH +
            System.getProperty("user.name") + "." +
            InetAddress.getLocalHost().getHostName() +
            CloudLiftServicesPropertiesGeneratorFilter.PROPERTIES_FILE_SUFFIX;

        generatedName = testLiftPropertiesGeneratorFilter.getPropertiesFileName();

        Assert.assertNotNull(propertiesFileName);
        Assert.assertNotNull(generatedName);
        Assert.assertEquals(propertiesFileName, generatedName);
    }

    @Test
    public void cloudLiftServicesPropertiesGeneratorCreatesFilesCorrectly()
    throws IOException {
        testLiftPropertiesGeneratorFilter.createPropertiesFile(fileName);
        Assert.assertTrue(new File(fileName).exists());
    }

    @Test (expected=ServletException.class)
    public void cloudLiftServicesPropertiesGenerationFailsIfMultipleMysqlServicesPresent()
    throws ServletException {
        setUpMultipleMysqlServicesMock();

        Properties props = new Properties();
        testLiftPropertiesGeneratorFilter.generateDBServiceProperties(
            mockEnvironment, props);
    }

    @Test
    public void cloudLiftServicesPropertiesNotGeneratedIfNoMysqlServicePresent()
    throws IOException, ServletException {
        setUpNoMysqlServiceMock();

        Properties props = new Properties();
        testLiftPropertiesGeneratorFilter.generateDBServiceProperties(
            mockEnvironment, props);

        Assert.assertTrue(props.isEmpty());
    }

    @Test
    public void cloudLiftServicesPropertiesFileNotCreatedIfNoMysqlServicePresent()
    throws IOException, ServletException {
        setUpNoMysqlServiceMock();

        testLiftPropertiesGeneratorFilter.generatePropertiesFile(
                fileName, mockEnvironment);

        Assert.assertFalse(new File(fileName).exists());
    }

    @Test
    public void cloudLiftServicesPropertiesGeneratedCorrectlyForMysqlServiceIfOnePresent()
    throws IOException, ServletException {
        setUpSingleMysqlServiceMock();

        Properties props = new Properties();
        testLiftPropertiesGeneratorFilter.generateDBServiceProperties(
            mockEnvironment, props);

        Assert.assertEquals(
            CloudLiftServicesPropertiesGeneratorFilter.DRIVER_CLASS_NAME,
            props.getProperty("db.class"));
        Assert.assertEquals(serviceJdbcUrl, props.getProperty("db.url"));
        Assert.assertEquals(serviceUserName, props.getProperty("db.user"));
        Assert.assertEquals(servicePassword, props.getProperty("db.pass"));
    }

    @Test
    public void cloudLiftServicesPropertiesFileGeneratedCorrectlyForMysqlServiceIfOnePresent()
    throws ServletException, IOException {
        setUpSingleMysqlServiceMock();

        testLiftPropertiesGeneratorFilter.generatePropertiesFile(
            fileName, mockEnvironment);
        Properties properties = new Properties();
        properties.load(new FileInputStream(fileName));

        Assert.assertEquals(
            CloudLiftServicesPropertiesGeneratorFilter.DRIVER_CLASS_NAME,
            properties.getProperty("db.class"));
        Assert.assertEquals(serviceJdbcUrl, properties.getProperty("db.url"));
        Assert.assertEquals(serviceUserName, properties.getProperty("db.user"));
        Assert.assertEquals(servicePassword, properties.getProperty("db.pass"));
    }

    private void setUpNoMysqlServiceMock() {
        List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
        when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).
            thenReturn(serviceInfos);
    }

    private void setUpSingleMysqlServiceMock() {
        List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();

        serviceInfos.add(mockMysqlServiceInfo1);
        when(mockMysqlServiceInfo1.getUrl()).thenReturn(serviceJdbcUrl);
        when(mockMysqlServiceInfo1.getUserName()).thenReturn(serviceUserName);
        when(mockMysqlServiceInfo1.getPassword()).thenReturn(servicePassword);

        when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).
            thenReturn(serviceInfos);
    }

    private void setUpMultipleMysqlServicesMock() {
        List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();

        serviceInfos.add(mockMysqlServiceInfo1);
        when(mockMysqlServiceInfo1.getUrl()).thenReturn(serviceJdbcUrl);
        when(mockMysqlServiceInfo1.getUserName()).thenReturn(serviceUserName);
        when(mockMysqlServiceInfo1.getPassword()).thenReturn(servicePassword);

        serviceInfos.add(mockMysqlServiceInfo2);
        when(mockMysqlServiceInfo2.getUrl()).thenReturn(serviceJdbcUrl);
        when(mockMysqlServiceInfo2.getUserName()).thenReturn(serviceUserName);
        when(mockMysqlServiceInfo2.getPassword()).thenReturn(servicePassword);

        when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).
            thenReturn(serviceInfos);
    }
}
