package org.cloudfoundry.reconfiguration;

import static org.mockito.Mockito.when;

import java.io.IOException;

import org.cloudfoundry.runtime.service.AbstractCloudServiceFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Unit test of the {@link CloudAutoStagingBeanFactoryPostProcessor}
 *
 * @author Jennifer Hickey
 *
 */
public class CloudAutoStagingBeanFactoryPostProcessorTest {

	@Mock
	private ConfigurableListableBeanFactory beanFactory;
	@Mock
	private ApplicationContext applicationContext;
	@Mock
	private Resource cloudServicesFile;

	private CloudAutoStagingBeanFactoryPostProcessor testBFPP;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		testBFPP = new CloudAutoStagingBeanFactoryPostProcessor();
	}

	@Test
	public void autoStagingOffCloudServiceFound() throws IOException {
		String cloudServicesFileLocation = "classpath:/org/cloudfoundry/reconfiguration/test-cloud-services-1";
		when(applicationContext.getResources(cloudServicesFileLocation)).thenReturn(
				new Resource[] { new ClassPathResource("/org/cloudfoundry/reconfiguration/test-cloud-services-1") });
		String[] beans = new String[] { "intBean" };
		when(beanFactory.getBeanNamesForType(AbstractCloudServiceFactory.class)).thenReturn(new String[0]);
		when(beanFactory.getBeanNamesForType(Integer.class)).thenReturn(beans);
		testBFPP.setApplicationContext(applicationContext);
		Assert.assertTrue(testBFPP.autoStagingOff(cloudServicesFileLocation, beanFactory));
	}

	@Test
	public void autoStagingOffCloudServiceFoundMultipleFiles() throws IOException {
		String cloudServicesFileLocation = "classpath:/org/cloudfoundry/reconfiguration/test-cloud-services-*";
		when(applicationContext.getResources(cloudServicesFileLocation)).thenReturn(
				new Resource[] { new ClassPathResource("/org/cloudfoundry/reconfiguration/test-cloud-services-1"),
						new ClassPathResource("/org/cloudfoundry/reconfiguration/test-cloud-services-2") });
		String[] beans = new String[] { "stringBean" };
		when(beanFactory.getBeanNamesForType(AbstractCloudServiceFactory.class)).thenReturn(new String[0]);
		when(beanFactory.getBeanNamesForType(Integer.class)).thenReturn(new String[0]);
		when(beanFactory.getBeanNamesForType(String.class)).thenReturn(beans);
		testBFPP.setApplicationContext(applicationContext);
		Assert.assertTrue(testBFPP.autoStagingOff(cloudServicesFileLocation, beanFactory));
	}

	@Test
	public void autoStagingOffCloudServiceNotFound() throws IOException {
		String cloudServicesFileLocation = "classpath:/org/cloudfoundry/reconfiguration/test-cloud-services-1";
		when(applicationContext.getResources(cloudServicesFileLocation)).thenReturn(
				new Resource[] { new ClassPathResource("/org/cloudfoundry/reconfiguration/test-cloud-services-1") });
		when(beanFactory.getBeanNamesForType(AbstractCloudServiceFactory.class)).thenReturn(new String[0]);
		when(beanFactory.getBeanNamesForType(Integer.class)).thenReturn(new String[0]);
		testBFPP.setApplicationContext(applicationContext);
		Assert.assertFalse(testBFPP.autoStagingOff(cloudServicesFileLocation, beanFactory));
	}

	@Test
	public void autoStagingOffCloudServiceClassNotFound() throws IOException {
		String cloudServicesFileLocation = "classpath:/org/cloudfoundry/reconfiguration/test-invalid-cloud-services";
		when(applicationContext.getResources(cloudServicesFileLocation))
				.thenReturn(
						new Resource[] { new ClassPathResource(
								"/org/cloudfoundry/reconfiguration/test-invalid-cloud-services") });
		testBFPP.setApplicationContext(applicationContext);
		Assert.assertFalse(testBFPP.autoStagingOff(cloudServicesFileLocation, beanFactory));
	}

	@Test
	public void autoStagingOffCloudServiceFilesNotFound() throws IOException {
		String cloudServicesFileLocation = "classpath:/org/cloudfoundry/reconfiguration/test-nonexistent-cloud-services";
		when(applicationContext.getResources(cloudServicesFileLocation)).thenReturn(new Resource[0]);
		testBFPP.setApplicationContext(applicationContext);
		Assert.assertFalse(testBFPP.autoStagingOff(cloudServicesFileLocation, beanFactory));
	}

	@Test
	public void autoStagingOffErrorLookingForCloudServicesFile() throws IOException {
		String cloudServicesFileLocation = "classpath:/org/cloudfoundry/reconfiguration/test-nonexistent-cloud-services";
		when(applicationContext.getResources(cloudServicesFileLocation)).thenThrow(new IOException());
		testBFPP.setApplicationContext(applicationContext);
		Assert.assertFalse(testBFPP.autoStagingOff(cloudServicesFileLocation, beanFactory));
	}

	@Test
	public void autoStagingOffErrorReadingCloudServicesFile() throws IOException {
		String cloudServicesFileLocation = "classpath:/org/cloudfoundry/reconfiguration/test-nonexistent-cloud-services";
		when(applicationContext.getResources(cloudServicesFileLocation)).thenReturn(
				new Resource[] { cloudServicesFile });
		when(cloudServicesFile.getInputStream()).thenThrow(new IOException());
		testBFPP.setApplicationContext(applicationContext);
		Assert.assertFalse(testBFPP.autoStagingOff(cloudServicesFileLocation, beanFactory));
	}
}
