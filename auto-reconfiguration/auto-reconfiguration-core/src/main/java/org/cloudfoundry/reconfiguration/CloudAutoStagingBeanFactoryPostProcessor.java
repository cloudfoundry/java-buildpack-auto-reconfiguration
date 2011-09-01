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
import org.cloudfoundry.runtime.service.relational.PostgresqlServiceCreator;
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
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * A bean factory post processor that auto-stage service-related beans.
 * <p>
 * Currently, this bean supports auto-staging of {@link DataSource} beans.
 * 
 * @author Ramnivas Laddad
 * @author Xin Li
 *
 */
public class CloudAutoStagingBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

	Logger logger = Logger.getLogger(CloudAutoStagingBeanFactoryPostProcessor.class.getName());

	private CloudEnvironment cloudEnvironment;
	
	private static final String APP_CLOUD_DATA_SOURCE_NAME = "__appCloudDataSource";
	private static final String APP_CLOUD_JPA_MYSQL_REPLACEMENT_PROPERTIES = "__appCloudJpaMySQLReplacementProperties";
	private static final String APP_CLOUD_HIBERNATE_MYSQL_REPLACEMENT_PROPERTIES = "__appCloudHibernateMySQLReplacementProperties";
	private static final String APP_CLOUD_JPA_POSTGRESQL_REPLACEMENT_PROPERTIES = "__appCloudJpaPostgreSQLReplacementProperties";
	private static final String APP_CLOUD_HIBERNATE_POSTGRESQL_REPLACEMENT_PROPERTIES = "__appCloudHibernatePostgreSQLReplacementProperties";

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (autoStagingOff(beanFactory)) {
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

	/**
	 * Recommends disabling auto-staging if beans of the following types are
	 * detected in the {@link ConfigurableListableBeanFactory} 1)
	 * {@link AbstractCloudServiceFactory} (corresponding to the specification
	 * of cloud:data-source, cloud:mongo-db-factory, etc) 2)
	 * {@link CloudServicesAutoPopulator} (cloud:auto-populate)
	 * 
	 * @param beanFactory
	 *            The {@link ConfigurableListableBeanFactory} to check for bean
	 *            definitions that should disable auto-staging
	 * @return true if auto-staging should be turned off
	 */
	boolean autoStagingOff(ConfigurableListableBeanFactory beanFactory) {
		if(usingCloudServices(beanFactory) || usingAutoPopulate(beanFactory)) {
			return true;
		}
		logger.log(Level.INFO,"Autostaging is active.");
		return false;
	}
	
	private boolean usingCloudServices(ConfigurableListableBeanFactory beanFactory) {
		//Load the class dynamically and with name in multiple parts to avoid shade plugin rename
		String packagePrefix = "org";
		Class<?> cloudServiceFactoryClazz = loadClass(packagePrefix + ".cloudfoundry.runtime.service.AbstractCloudServiceFactory");
		if(cloudServiceFactoryClazz == null || beanFactory.getBeansOfType(cloudServiceFactoryClazz).isEmpty()) {
			return false;
		}
		logger.log(Level.INFO,"Found an instance of AbstractCloudServiceFactory.  Autostaging will be skipped.");
		return true;
	}
	
	private boolean usingAutoPopulate(ConfigurableListableBeanFactory beanFactory) {
		//Load the class dynamically and with name in multiple parts to avoid shade plugin rename
		String packagePrefix = "org";
		Class<?> autoPopulatorClass = loadClass(packagePrefix + ".cloudfoundry.runtime.service.CloudServicesAutoPopulator");
		if(autoPopulatorClass == null || beanFactory.getBeansOfType(autoPopulatorClass).isEmpty()) {
			return false;
		}
		logger.log(Level.INFO,"Found an instance of CloudServicesAutoPopulator.  Autostaging will be skipped.");
		return true;
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

		ArrayList<DataSource> dataSourceList = new ArrayList<DataSource>();
		for(Map<String, Object> service:  cloudEnvironment.getServices()) {
			String label = (String) service.get("label");
			if (label == null) {
				continue;
			}
			
			if (label.startsWith("postgresql")) {
				try {
					PostgresqlServiceCreator postgresqlCreationHelper = new PostgresqlServiceCreator(cloudEnvironment);
					dataSourceList.add(postgresqlCreationHelper.createSingletonService().service);
				} catch (CloudServiceException ex) {
					logger.log(Level.INFO, "Multiple database services found. Skipping autostaging", ex);
					return false;
				}
			} else if (label.startsWith("mysql")) {
				try {
					MysqlServiceCreator mysqlCreationHelper = new MysqlServiceCreator(cloudEnvironment);
					dataSourceList.add(mysqlCreationHelper.createSingletonService().service);
				} catch (CloudServiceException ex) {
					logger.log(Level.INFO, "Multiple database services found. Skipping autostaging");
					return false;
				}
			}
		}
		
		if (dataSourceList.size() == 0) {
			logger.log(Level.INFO, "No database service found. Skipping autostaging");
			return false;
		} else if (dataSourceList.size() > 1) {
			logger.log(Level.INFO, "More than 1 (" + dataSourceList.size() + ") database services found. Skipping autostaging");
			return false;
		} else {
			defaultListableBeanFactory.registerSingleton(APP_CLOUD_DATA_SOURCE_NAME, dataSourceList.get(0));
		}
		
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
		for(Map<String, Object> service:  cloudEnvironment.getServices()) {
			String label = (String) service.get("label");
			
			if (label == null) {
				continue;
			}
			
			if (label.startsWith("postgresql"))
			{
				processBeanProperties(beanFactory, "org.springframework.orm.jpa.AbstractEntityManagerFactoryBean", 
						APP_CLOUD_JPA_POSTGRESQL_REPLACEMENT_PROPERTIES, "jpaProperties");
			}
			else if (label.startsWith("mysql"))
			{
				processBeanProperties(beanFactory, "org.springframework.orm.jpa.AbstractEntityManagerFactoryBean", 
						APP_CLOUD_JPA_MYSQL_REPLACEMENT_PROPERTIES, "jpaProperties");
			}
		}
	}
	
	private void processHibernateFactories(DefaultListableBeanFactory beanFactory) {
		for(Map<String, Object> service:  cloudEnvironment.getServices()) {
			String label = (String) service.get("label");
			if (label == null) {
				continue;
			}
			if (label.startsWith("postgresql")) {
				processBeanProperties(beanFactory, "org.springframework.orm.hibernate3.AbstractSessionFactoryBean", 
						APP_CLOUD_HIBERNATE_POSTGRESQL_REPLACEMENT_PROPERTIES, "hibernateProperties");
			}
			else if (label.startsWith("mysql")) {
				processBeanProperties(beanFactory, "org.springframework.orm.hibernate3.AbstractSessionFactoryBean", 
						APP_CLOUD_HIBERNATE_MYSQL_REPLACEMENT_PROPERTIES, "hibernateProperties");
			}
		}	
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
	
	@SuppressWarnings("unchecked")
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
