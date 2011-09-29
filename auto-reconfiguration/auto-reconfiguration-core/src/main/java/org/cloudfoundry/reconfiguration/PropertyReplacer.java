/*
 * Copyright (c) 2009-2011 VMware, Inc.
 */
package org.cloudfoundry.reconfiguration;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Replaces a bean property with value type {@link ManagedProperties} with a
 * specified set of {@link ManagedProperties}
 *
 * @author Ramnivas Laddad
 * @author Xin Li
 * @author Jennifer Hickey
 *
 */
public class PropertyReplacer {

	/**
	 * Replaces a bean property with value type {@link ManagedProperties} with a
	 * specified set of {@link ManagedProperties}
	 *
	 * @param beanFactory
	 *            The {@link DefaultListableBeanFactory} containing the beans to
	 *            evaluate for property replacements
	 * @param beanClassName
	 *            The class of bean containing the property to be replaced
	 * @param replacementPropertiesName
	 *            The name of the Properties bean containing values to use in
	 *            replacement
	 * @param propertyKey
	 *            The name of the property whose value is to be replaced with replacement properties
	 */
	public void replaceProperty(DefaultListableBeanFactory beanFactory, String beanClassName,
			String replacementPropertiesName, String propertyKey) {
		Class<?> beanClass = loadClass(beanClassName);
		if (beanClass == null) {
			return;
		}
		try {
			// TODO: Required in Grails case and need to reexamine
			beanFactory.getBeanDefinition(replacementPropertiesName);
		} catch (Exception ex) {
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
					throw new IllegalStateException("Unable to process property " + originalProperties.getName()
							+ " of " + value.getClass() + " type");
				}
			} else {
				originalPropertyValue = new ManagedProperties();
			}

			ManagedProperties replacementProperties = loadReplacementPropertyValues(beanFactory,
					replacementPropertiesName);
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
			BeanDefinition beanDefinition = getBeanDefinition(beanFactory, beanReference.getBeanName());
			return getMapWrappingBeanProperties(beanDefinition);
		} catch (Exception e) {
			throw new IllegalStateException("Error processing property replacement for a BeanDefinitionHolder", e);
		}
	}

	private Properties extractProperties(TypedStringValue typeStringValue) {
		Object value = typeStringValue.getValue();
		if (value instanceof Properties) {
			return (Properties) value;
		} else if (value instanceof String) {
			Properties props = new Properties();
			try {
				props.load(new StringReader((String) value));
				return props;
			} catch (IOException e) {
				throw new IllegalStateException("Error processing property replacement for a TypedStringValue", e);
			}
		} else {
			throw new IllegalStateException(
					"Error processing property replacement for a TypedStringValue of value type " + value.getClass());
		}
	}

	private ManagedProperties loadReplacementPropertyValues(DefaultListableBeanFactory beanFactory,
			String replacementPropertiesName) {
		BeanDefinition replacementPropertiesBeanDef = beanFactory.getBeanDefinition(replacementPropertiesName);
		return (ManagedProperties) replacementPropertiesBeanDef.getPropertyValues().getPropertyValue("properties")
				.getValue();
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
						return mapToProperties((Map<String, String>) value);
					}
				} else {
					throw new IllegalArgumentException(
							"Unable to process PropertiesFactoryBean; doesn't contain either 'locations' or 'properties' property");
				}
			} catch (IOException ex) {
				throw new IllegalArgumentException("Unable to process PropertiesFactoryBean", ex);
			}
		} else {
			Map<String, String> sourceMap = (Map<String, String>) beanDefinition.getPropertyValues()
					.getPropertyValue("sourceMap").getValue();
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

	@SuppressWarnings("rawtypes")
	private Properties loadPropertiesForLocations(PropertyValue locationPV) throws IOException {
		Object locationsValue = locationPV.getValue();
		if (locationsValue instanceof ManagedList) {
			Properties props = new Properties();
			for (Object location : (ManagedList) locationsValue) {
				props.putAll(loadPropertiesForLocation(location));
			}
			return props;
		} else {
			throw new IllegalArgumentException("Unable to process 'locations' value of PropertyValue "
					+ locationsValue.getClass());
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

	private BeanDefinition getBeanDefinition(DefaultListableBeanFactory beanFactory, String beanName) {
		if (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
		}
		return beanFactory.getBeanDefinition(beanName);

	}
}
