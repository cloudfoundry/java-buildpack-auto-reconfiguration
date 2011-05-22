package org.cloudfoundry.reconfiguration;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
	@Mock private MysqlServiceInfo mockMysqlServiceInfo;
	@Mock private CloudEnvironment mockEnvironment;
	
	private CloudLiftServicesPropertiesGeneratorFilter testLiftPropertiesGeneratorFilter;
	private String fileName = "/tmp/cloudLiftServicesProperties.props";
	private String serviceJdbcUrl = "jdbc:mysql://10.20.20.40:1234/mysql-1";
	private String serviceUserName = "testUser";
	private String servicePassword = "testPassword";


	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		testLiftPropertiesGeneratorFilter = new CloudLiftServicesPropertiesGeneratorFilter();
	}
	
	@After
	public void tearDown() {
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
	}
	
	@Test
	public void cloudLiftServicesPropertiesFileNamedCorrectly() {
		String propertiesFileName = null;
		String generatedName = null;
		try {
			propertiesFileName = CloudLiftServicesPropertiesGeneratorFilter.PROPERTIES_FILE_PATH + 
				System.getProperty("user.name") + "." + InetAddress.getLocalHost().getHostName() + 
				CloudLiftServicesPropertiesGeneratorFilter.PROPERTIES_FILE_SUFFIX;
			generatedName = testLiftPropertiesGeneratorFilter.getPropertiesFileName();
		} catch (IOException ioe) {
			// Ignore
		}
		Assert.assertNotNull(propertiesFileName);
		Assert.assertNotNull(generatedName);
		Assert.assertEquals(propertiesFileName, generatedName);
	}
	
	@Test
	public void cloudLiftServicesPropertiesGeneratorCreatesFilesCorrectly() {
		try {
			testLiftPropertiesGeneratorFilter.createPropertiesFile(fileName);
		} catch (IOException ioe) {
			// Ignore
		}
		Assert.assertTrue(new File(fileName).exists());
	}
	
	@Test
	public void cloudLiftServicesPropertiesGeneratedForMysqlServiceIfOnePresent() {
		setUpMocks();
		Properties props = new Properties();
		testLiftPropertiesGeneratorFilter.generateDBServiceProperties(
				mockEnvironment, props);
		Assert.assertEquals(CloudLiftServicesPropertiesGeneratorFilter.DRIVER_CLASS_NAME,
				props.getProperty("db.class"));
		Assert.assertEquals(serviceJdbcUrl, props.getProperty("db.url"));
		Assert.assertEquals(serviceUserName, props.getProperty("db.user"));
		Assert.assertEquals(servicePassword, props.getProperty("db.pass"));
	}
	
	@Test
	public void cloudLiftServicesPropertiesFileGeneratedCorrectly() {
		setUpMocks();
		Properties properties = new Properties();
		try {
			testLiftPropertiesGeneratorFilter.createPropertiesFile(fileName);
			testLiftPropertiesGeneratorFilter.generatePropertiesFile(
					new FileOutputStream(fileName), mockEnvironment);
		} catch (IOException ioe) {
		}
		try {
		    properties.load(new FileInputStream(fileName));
		} catch (IOException e) {
		}

		Assert.assertEquals(CloudLiftServicesPropertiesGeneratorFilter.DRIVER_CLASS_NAME,
				properties.getProperty("db.class"));
		Assert.assertEquals(serviceJdbcUrl, properties.getProperty("db.url"));
		Assert.assertEquals(serviceUserName, properties.getProperty("db.user"));
		Assert.assertEquals(servicePassword, properties.getProperty("db.pass"));
	}

	private void setUpMocks() {
		List<MysqlServiceInfo> serviceInfos = new ArrayList<MysqlServiceInfo>();
		serviceInfos.add(mockMysqlServiceInfo);
		when(mockMysqlServiceInfo.getUrl()).thenReturn(serviceJdbcUrl);
		when(mockMysqlServiceInfo.getUserName()).thenReturn(serviceUserName);
		when(mockMysqlServiceInfo.getPassword()).thenReturn(servicePassword);
		when(mockEnvironment.getServiceInfos(MysqlServiceInfo.class)).thenReturn(serviceInfos);
	}

}
