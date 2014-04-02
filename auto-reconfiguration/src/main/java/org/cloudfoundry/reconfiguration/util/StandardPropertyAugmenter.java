/*
 * Copyright 2011-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.reconfiguration.util;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Standard implementation of {@link PropertyAugmenter}
 */
public final class StandardPropertyAugmenter implements PropertyAugmenter {

    @Override
    public void augment(ConfigurableListableBeanFactory beanFactory, Class<?> beanClass, String key,
                        ManagedProperties additionalProperties) {
        additionalProperties.setMergeEnabled(true);

        for (String beanName : beanFactory.getBeanNamesForType(beanClass, true, false)) {
            MutablePropertyValues propertyValues = getBeanDefinition(beanFactory, beanName).getPropertyValues();
            Properties originalValue = extractOriginalValue(beanFactory, propertyValues.getPropertyValue(key));

            propertyValues.addPropertyValue(key, additionalProperties.merge(originalValue));
        }
    }

    private Properties extractOriginalValue(ConfigurableListableBeanFactory beanFactory,
                                            PropertyValue originalProperty) {
        if (originalProperty == null) {
            return new ManagedProperties();
        }

        Object value = originalProperty.getValue();
        if (value instanceof Properties) {
            return (Properties) value;
        } else if (value instanceof BeanDefinitionHolder) {
            return extractProperties((BeanDefinitionHolder) value);
        } else if (value instanceof BeanReference) {
            return extractProperties((BeanReference) value, beanFactory);
        } else if (value instanceof TypedStringValue) {
            return extractProperties((TypedStringValue) value);
        }

        throw new IllegalStateException(String.format("Unable to process property '%s' of type %s",
                originalProperty.getName(), value.getClass().getName()));
    }

    private BeanDefinition getBeanDefinition(ConfigurableListableBeanFactory beanFactory, String beanName) {
        String canonicalBeanName;
        if (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
            canonicalBeanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
        } else {
            canonicalBeanName = beanName;
        }

        return beanFactory.getBeanDefinition(canonicalBeanName);
    }

    private Properties extractProperties(BeanDefinitionHolder beanDefinitionHolder) {
        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
        return getMapWrappingBeanProperties(beanDefinition);
    }

    private Properties extractProperties(BeanReference beanReference, ConfigurableListableBeanFactory beanFactory) {
        BeanDefinition beanDefinition = getBeanDefinition(beanFactory, beanReference.getBeanName());
        return getMapWrappingBeanProperties(beanDefinition);
    }

    private Properties extractProperties(TypedStringValue value) {
        try {
            Properties properties = new Properties();
            properties.load(new StringReader(value.getValue()));
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Error loading properties from '%s'", value), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Properties getMapWrappingBeanProperties(BeanDefinition beanDefinition) {
        if (beanDefinition.getBeanClassName().equals(PropertiesFactoryBean.class.getName())) {
            return getPropertiesFactoryBeanProperties(beanDefinition);
        } else {
            return convert((Map<String, String>) beanDefinition.getPropertyValues().getPropertyValue
                    ("sourceMap").getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private Properties getPropertiesFactoryBeanProperties(BeanDefinition beanDefinition) {
        PropertyValues propertyValues = beanDefinition.getPropertyValues();
        if (propertyValues.contains("location")) {
            return loadPropertiesForLocation(propertyValues.getPropertyValue("location").getValue());
        } else if (propertyValues.contains("locations")) {
            return loadPropertiesForLocations(propertyValues.getPropertyValue("locations").getValue());
        } else if (propertyValues.contains("properties")) {
            Object value = propertyValues.getPropertyValue("properties").getValue();
            if (value instanceof BeanDefinitionHolder) {
                return extractProperties((BeanDefinitionHolder) value);
            } else {
                return convert((Map<String, String>) value);
            }
        }

        throw new IllegalArgumentException(
                "Unable to process PropertiesFactoryBean; doesn't contain either 'locations' or 'properties' property");
    }

    private Properties loadPropertiesForLocation(Object location) {
        try {
            if (location instanceof String) {
                return PropertiesLoaderUtils.loadAllProperties((String) location);
            } else if (location instanceof TypedStringValue) {
                return PropertiesLoaderUtils.loadAllProperties(((TypedStringValue) location).getValue());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to load properties from '%s'", location), e);
        }

        throw new IllegalArgumentException(String.format("Unable to process 'location' value of type %s",
                location.getClass().getName()));
    }

    private Properties loadPropertiesForLocations(Object locations) {
        if (locations instanceof List) {
            Properties props = new Properties();
            for (Object location : (List) locations) {
                props.putAll(loadPropertiesForLocation(location));
            }
            return props;
        }

        throw new IllegalArgumentException(String.format("Unable to process 'locations' value of type %s",
                locations.getClass().getName()));
    }

    private Properties convert(Map<String, String> map) {
        Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

}

