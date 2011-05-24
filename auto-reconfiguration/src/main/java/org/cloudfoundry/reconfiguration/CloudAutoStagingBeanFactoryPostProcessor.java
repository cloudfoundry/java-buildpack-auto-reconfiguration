package org.cloudfoundry.reconfiguration;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.service.relational.MysqlServiceCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.core.Ordered;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * A bean factory post processor that auto-stage service-related beans.
 * <p>
 * Currently, this bean supports auto-staging of {@link DataSource} beans.
 * 
 * @author Ramnivas Laddad
 *
 */
public class CloudAutoStagingBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

	Logger logger = Logger.getLogger(CloudAutoStagingBeanFactoryPostProcessor.class.getName());

	private CloudEnvironment cloudEnvironment;
	
	private static final String CLOUDFOUNDRY_PROPERTIES = "META-INF/cloudfoundry.properties";
	private static final String APP_CLOUD_DATA_SOURCE_NAME = "__appCloudDataSource";
	private static final String APP_CLOUD_JPA_REPLACEMENT_PROPERTIES = "__appCloudJpaReplacementProperties";
	private static final String APP_CLOUD_HIBERNATE_REPLACEMENT_PROPERTIES = "__appCloudHibernateReplacementProperties";
	
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (autoStagingOff()) {
			return;
		}
		
		DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
		
		// defaultListableBeanFactory.getBean(CloudEnvironment.class) will do, 
		// but we go through a mechanism that will work for spring-2.5.x as well
		@SuppressWarnings("unchecked")
		Map<String,CloudEnvironment> cloudEnvironmentBeans = defaultListableBeanFactory.getBeansOfType(CloudEnvironment.class);
		if (cloudEnvironmentBeans.size() > 1) {
			logger.log(Level.INFO, "Multiple (" + cloudEnvironmentBeans.size() + ") CloudEnvironmentBeans found; zero or 1 expected");
			return;
		} else if (cloudEnvironmentBeans.size() == 1) {
			cloudEnvironment = cloudEnvironmentBeans.entrySet().iterator().next().getValue();
		} else {
			cloudEnvironment = new CloudEnvironment();
		}
		 
		if(processDatasources(defaultListableBeanFactory)) {
			processJpaFactories(defaultListableBeanFactory);
			processHibernateFactories(defaultListableBeanFactory);
		}
	}

	private boolean autoStagingOff() {
		return autoStagingOff(CLOUDFOUNDRY_PROPERTIES);
	}
	
	/**
	 * WARNING: Experimental support to opt out of autostaging, useful if autostaging comes in your way.
	 * 
	 * Turn off autostaging if we find a META-INF/cloudfoundry.properties on classpath and it
	 * contains autostaging=false. Applications can opt-out of autostaging by adding a property 
	 * to src/main/resources/META-INF/cloudfoundry.properties (assuming Maven layout).
	 * 
	 * @return return true to opt out of autostaging
	 */
	boolean autoStagingOff(String propertyLocation) {
		try {
			Resource cloudfoundryConfig = new DefaultResourceLoader().getResource(propertyLocation);
			if (!cloudfoundryConfig.exists()) {
				logger.log(Level.INFO, "No 'META-INF/cloudfoundry.properties' found, autostaging is active");
				return false;
			}
			Properties cloudfoundryProperties = PropertiesLoaderUtils.loadProperties(cloudfoundryConfig);
			String autostagingStringValue = cloudfoundryProperties.getProperty("autostaging", "true");
			boolean autostagingValue = Boolean.valueOf(autostagingStringValue);
			if (!autostagingValue) {
				logger.log(Level.INFO, "Application requested to skip autostaging");
			}
			return !autostagingValue;
		} catch (Exception ex) {
			// Turn off autostaging if anything goes wrong in our detection
			return true;
		}
	}

	// Let this be the last to process
	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

	private boolean processDatasources(DefaultListableBeanFactory defaultListableBeanFactory) {
		String[] dataSourceBeanNames = getRealDataSources(defaultListableBeanFactory);
		if (dataSourceBeanNames.length == 0) {
			logger.log(Level.INFO, "No datasources found in application context");
			return false;
		} else if (dataSourceBeanNames.length > 1) {
			logger.log(Level.INFO, "More than 1 (" + dataSourceBeanNames.length + ") real datasources found in application context. Skipping autostaging.");
			return false;
		}

		DataSource theOnlyDataSource = null;
		try {
			MysqlServiceCreator mysqlCreationHelper = new MysqlServiceCreator(cloudEnvironment);
			theOnlyDataSource = mysqlCreationHelper.createSingletonService().service;
		} catch (CloudServiceException ex) {
			logger.log(Level.INFO, "Multiple database services found. Skipping autostaging");
			return false;
		}
		defaultListableBeanFactory.registerSingleton(APP_CLOUD_DATA_SOURCE_NAME, theOnlyDataSource);
		
		for (String dataSourceBeanName : dataSourceBeanNames) {
			if (dataSourceBeanName.equals(APP_CLOUD_DATA_SOURCE_NAME)) {
				continue;
			}
			defaultListableBeanFactory.removeBeanDefinition(dataSourceBeanName);
			defaultListableBeanFactory.registerAlias(APP_CLOUD_DATA_SOURCE_NAME, dataSourceBeanName);
		}
		return true;
	}
	
	private void processJpaFactories(DefaultListableBeanFactory beanFactory) {
		processBeanProperties(beanFactory, "org.springframework.orm.jpa.AbstractEntityManagerFactoryBean", 
				APP_CLOUD_JPA_REPLACEMENT_PROPERTIES, "jpaProperties");
	}
	
	private void processHibernateFactories(DefaultListableBeanFactory beanFactory) {
			processBeanProperties(beanFactory, "org.springframework.orm.hibernate3.AbstractSessionFactoryBean", 
					APP_CLOUD_HIBERNATE_REPLACEMENT_PROPERTIES, "hibernateProperties");
	}

	private void processBeanProperties(DefaultListableBeanFactory beanFactory,
									  String beanClassName, String replacementPropertiesName,
									  String propertyKey) {
		Class<?> beanClass = loadClass(beanClassName);
		if (beanClass == null) {
			return;
		}
		try {
			// TODO: Required in Grails case and need to reexamine
			beanFactory.getBeanDefinition(replacementPropertiesName);
		} catch (Exception ex){
			return;
		}
		String[] beanNames = beanFactory.getBeanNamesForType(beanClass);
		for (String beanName : beanNames) {
			BeanDefinition beanDefinition = getBeanDefinition(beanFactory, beanName);
			MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
			PropertyValue originalProperties = propertyValues.getPropertyValue(propertyKey);
			
			Properties originalPropertyValue = null;
			if (originalProperties != null) {
				Object value = originalProperties.getValue();
				if (value instanceof Properties) {
					originalPropertyValue = (Properties) originalProperties.getValue();
				} else if (value instanceof BeanDefinitionHolder) {
					originalPropertyValue = extractProperties((BeanDefinitionHolder) value);
				} else if (value instanceof BeanReference) {
					originalPropertyValue = extractProperties((BeanReference) value, beanFactory);
				} else if (value instanceof TypedStringValue) {
					originalPropertyValue = extractProperties((TypedStringValue) value);
				} else {
					throw new IllegalStateException("Unable to process property " + originalProperties.getName() + " of " + value.getClass() + " type");
				}
			} else {
				originalPropertyValue = new ManagedProperties();
			}
			
			ManagedProperties replacementProperties 
				= loadReplacementPropertyValues(beanFactory, replacementPropertiesName);
			replacementProperties.setMergeEnabled(true);
			replacementProperties = (ManagedProperties) replacementProperties.merge(originalPropertyValue);
			propertyValues.addPropertyValue(new PropertyValue(propertyKey, replacementProperties));
		}
	}

	private Properties extractProperties(BeanDefinitionHolder beanDefinitionHolder) {
		try {
			BeanDefinition valBeanDefinition = beanDefinitionHolder.getBeanDefinition();
			return getMapWrappingBeanProperties(valBeanDefinition);
		} catch (Exception e) {
			throw new IllegalStateException("Error processing property replacement for a BeanDefinitionHolder", e);
		}
	}
	
	private Properties extractProperties(BeanReference beanReference, DefaultListableBeanFactory beanFactory) {
		try {
			BeanDefinition beanDefinition = getBeanDefinition(beanFactory, 
																beanReference.getBeanName());
			return getMapWrappingBeanProperties(beanDefinition);
		} catch (Exception e) {
			throw new IllegalStateException("Error processing property replacement for a BeanDefinitionHolder", e);
		}
	}

	private Properties extractProperties(TypedStringValue typeStringValue) {
		Object value = typeStringValue.getValue();
		if (value instanceof Properties) {
			return (Properties)value;
		} else if (value instanceof String) {
			Properties props = new Properties();
			try {
				props.load(new StringReader((String)value));
				return props;
			} catch (IOException e) {
				throw new IllegalStateException("Error processing property replacement for a TypedStringValue", e);
			}
		} else {
			throw new IllegalStateException("Error processing property replacement for a TypedStringValue of value type " + value.getClass());
		}
	}
	
	private ManagedProperties loadReplacementPropertyValues(DefaultListableBeanFactory beanFactory, String replacementPropertiesName) {
		BeanDefinition replacementPropertiesBeanDef = beanFactory.getBeanDefinition(replacementPropertiesName);
		return (ManagedProperties) replacementPropertiesBeanDef.getPropertyValues().getPropertyValue("properties").getValue();
	}

	private String[] getRealDataSources(DefaultListableBeanFactory beanFactory) {
		String[] dataSourceBeanNames = beanFactory.getBeanNamesForType(DataSource.class);
		Class<?> txAwareDSClass = loadClass("org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy");
		if (txAwareDSClass == null) {
			return dataSourceBeanNames;
		}
		// In Scala, could have been one line and not even need contains()!
		String[] txAwareDSBeanNames = beanFactory.getBeanNamesForType(txAwareDSClass);
		List<String> realDSBeanNames = new ArrayList<String>();
		for (String dataSourceBeanName : dataSourceBeanNames) {
			if (!contains(txAwareDSBeanNames, dataSourceBeanName)) {
				// Skip singletons
				try {
					if (getBeanDefinition(beanFactory, dataSourceBeanName) != null) {
						realDSBeanNames.add(dataSourceBeanName);
					} 
				} catch (NoSuchBeanDefinitionException ex) {
						// skip
				}
			}
		}
		return realDSBeanNames.toArray(new String[0]);
	}
	
	private BeanDefinition getBeanDefinition(DefaultListableBeanFactory beanFactory, String beanName) {
		if (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
		}
		return beanFactory.getBeanDefinition(beanName);

	}
	
	private Properties getMapWrappingBeanProperties(BeanDefinition beanDefinition) {
		if (beanDefinition.getBeanClassName().equals(PropertiesFactoryBean.class.getName())) {
			try {
				PropertyValues propertyValues = beanDefinition.getPropertyValues();
				if (propertyValues.contains("location")) {
					return loadPropertiesForLocation(propertyValues.getPropertyValue("location"));
				} else if (propertyValues.contains("locations")) {
					return loadPropertiesForLocations(propertyValues.getPropertyValue("locations"));
				} else if (propertyValues.contains("properties")) {
					Object value = propertyValues.getPropertyValue("properties").getValue();
					if (value instanceof BeanDefinitionHolder) {
						return extractProperties((BeanDefinitionHolder) value);
					} else {
						return mapToProperties((Map<String,String>)value);
					}
				} else {
					throw new IllegalArgumentException("Unable to process PropertiesFactoryBean; doesn't contain either 'locations' or 'properties' property");
				}
			} catch (IOException ex) {
				throw new IllegalArgumentException("Unable to process PropertiesFactoryBean", ex);
			}
		} else {
			@SuppressWarnings("unchecked")
			Map<String,String> sourceMap = (Map<String,String>) beanDefinition.getPropertyValues().getPropertyValue("sourceMap").getValue();
			return mapToProperties(sourceMap);
		}
	}
	
	private Properties loadPropertiesForLocation(PropertyValue locationPV) throws IOException {
		Object locationValue = locationPV.getValue();
		return loadPropertiesForLocation(locationValue);
	}
	
	private Properties loadPropertiesForLocation(Object location) throws IOException {
		if (location instanceof String) {
			return PropertiesLoaderUtils.loadAllProperties((String) location);
		} else if (location instanceof TypedStringValue) {
			return PropertiesLoaderUtils.loadAllProperties(((TypedStringValue) location).getValue());
		} else {
			throw new IllegalArgumentException("Unable to process 'location' value of type " + location.getClass());
		}
	}

	private Properties loadPropertiesForLocations(PropertyValue locationPV) throws IOException {
		Object locationsValue = locationPV.getValue();
		if (locationsValue instanceof ManagedList) {
			Properties props = new Properties();
			for (Object location : (ManagedList)locationsValue) {
				props.putAll(loadPropertiesForLocation(location));
			}
			return props;
		} else {
			throw new IllegalArgumentException("Unable to process 'locations' value of PropertyValue " + locationsValue.getClass());
		}
	}

	
	private Properties mapToProperties(Map<String, String> map) {
		Properties properties = new Properties();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			properties.put(entry.getKey(), entry.getValue());
		}
		return properties;
	}

	private Class<?> loadClass(String name) {
		try {
			return Class.forName(name);
		} catch (Throwable ex) {
			return null;
		}
	}
	
	private <T> boolean contains(T[] array, T searchElement) {
		for (T element : array) {
			if (element.equals(searchElement)) {
				return true;
			}
		}
		return false;
	}
	
}
