package org.cloudfoundry.reconfiguration;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.cloudfoundry.reconfiguration.data.document.MongoConfigurer;
import org.cloudfoundry.reconfiguration.data.keyvalue.RedisConfigurer;
import org.cloudfoundry.reconfiguration.data.relational.DataSourceConfigurer;
import org.cloudfoundry.reconfiguration.messaging.RabbitConfigurer;
import org.cloudfoundry.reconfiguration.util.CloudFactoryUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;


/**
 * A bean factory post processor that auto-stages service-related beans.
 * <p>
 * Currently, this bean supports auto-staging of {@link DataSource} beans.
 *
 * @author Ramnivas Laddad
 * @author Xin Li
 * @author Jennifer Hickey
 *
 */
public class CloudAutoStagingBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered, ApplicationContextAware {

	static final String CLOUD_SERVICES_FILE = "classpath*:/META-INF/cloud/cloud-services";

	Logger logger = Logger.getLogger(CloudAutoStagingBeanFactoryPostProcessor.class.getName());

	private ApplicationContext applicationContext;


	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (autoStagingOff(CLOUD_SERVICES_FILE,beanFactory)) {
			return;
		}

		DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;

		CloudFactory cloudFactory = CloudFactoryUtil.getOrCreateCloudFactory(defaultListableBeanFactory, logger);
		
		Cloud cloud = cloudFactory.getCloud();
		
		new DataSourceConfigurer(cloud).configure(defaultListableBeanFactory);
		new MongoConfigurer(cloud).configure(defaultListableBeanFactory);
		new RedisConfigurer(cloud).configure(defaultListableBeanFactory);
		new RabbitConfigurer(cloud).configure(defaultListableBeanFactory);
	}

	/**
	 * Finds and reads files that contain fully qualified class names of cloud
	 * services. If any cloud services are detected in the user's
	 * {@link ConfigurableListableBeanFactory}, auto-staging will be turned off
	 *
	 * @param cloudServiceFileLocations
	 *            The pattern to use when scanning the
	 *            {@link ApplicationContext} for cloud service files
	 * @param beanFactory
	 *            The {@link ConfigurableListableBeanFactory} to check for bean
	 *            definitions that should disable auto-staging
	 * @return true if auto-staging should be turned off
	 */
	boolean autoStagingOff(String cloudServiceFileLocations, ConfigurableListableBeanFactory beanFactory) {
		Resource[] resources;
		try {
			resources = applicationContext.getResources(cloudServiceFileLocations);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error scanning for cloud services files: " + cloudServiceFileLocations
					+ ".  Autostaging will be active.", e);
			return false;
		}
		for (Resource resource : resources) {
			BufferedReader fileReader = null;
			try {
				fileReader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
				for (String line = fileReader.readLine(); line != null; line = fileReader.readLine()) {
					String cloudServiceClassName = line.trim();
					if (usingCloudService(beanFactory, cloudServiceClassName)) {
						return true;
					}
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Error reading cloud service file: " + resource
						+ ".  File contents will not be evaluated for cloud services.", e);
			} finally {
				if (fileReader != null) {
					try {
						fileReader.close();
					} catch (IOException e) {
						logger.log(Level.WARNING, "Error closing file: " + fileReader, e);
					}
				}
			}
		}
		logger.log(Level.INFO, "Autostaging is active.");
		return false;
	}

	private boolean usingCloudService(ConfigurableListableBeanFactory beanFactory, String cloudServiceClassName) {
		Class<?> cloudServiceFactoryClazz = loadClass(cloudServiceClassName);
		if(cloudServiceFactoryClazz == null || beanFactory.getBeanNamesForType(cloudServiceFactoryClazz,true,false).length == 0) {
			return false;
		}
		logger.log(Level.INFO,"Found an instance of " + cloudServiceClassName + ".  Autostaging will be skipped.");
		return true;
	}

	// Let this be the last to process
	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

	private Class<?> loadClass(String name) {
		try {
			return Class.forName(name);
		} catch (Throwable ex) {
			return null;
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}