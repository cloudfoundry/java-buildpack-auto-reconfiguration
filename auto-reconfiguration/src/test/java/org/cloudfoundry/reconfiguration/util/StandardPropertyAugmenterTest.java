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

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedProperties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public final class StandardPropertyAugmenterTest {

    private final ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);

    private final Class<?> beanClass = Object.class;

    private final BeanDefinition beanDefinition = mock(BeanDefinition.class);

    private final String beanName = "beanName";

    private final String key = "key";

    private final ManagedProperties additionalProperties = new ManagedProperties();

    private final MutablePropertyValues propertyValues = new MutablePropertyValues();

    private final StandardPropertyAugmenter propertyAugmenter = new StandardPropertyAugmenter();

    @Before
    public void setUpMocks() {
        when(this.beanFactory.getBeanNamesForType(this.beanClass, true, false)).thenReturn(new String[]{this
                .beanName});
        when(this.beanFactory.getBeanDefinition(this.beanName)).thenReturn(this.beanDefinition);
        when(this.beanDefinition.getPropertyValues()).thenReturn(this.propertyValues);
        when(this.beanDefinition.getBeanClassName()).thenReturn(this.beanClass.getCanonicalName());
    }

    @Test
    public void additionalPropertiesMergable() {
        when(this.beanFactory.getBeanNamesForType(this.beanClass, true, false)).thenReturn(new String[0]);

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);

        assertTrue(this.additionalProperties.isMergeEnabled());
    }

    @Test
    public void newManagedProperties() {
        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);

        assertTrue(this.propertyValues.contains(this.key));
    }

    @Test
    public void factoryBean() {
        when(this.beanFactory.getBeanNamesForType(this.beanClass, true, false)).thenReturn(new String[]{String.format
                ("%s%s", BeanFactory.FACTORY_BEAN_PREFIX, this.beanName)});

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);

        assertTrue(this.propertyValues.contains(this.key));
    }

    @Test
    public void existingProperties() {
        Properties p = new Properties();
        p.put("additional-key", "additional-value");
        this.propertyValues.addPropertyValue(this.key, p);

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);

        assertTrue(this.propertyValues.contains(this.key));
        assertContainsKey("additional-key");
    }

    @Test
    public void existingBeanDefinitionHolder() {
        BeanDefinitionHolder bdh = new BeanDefinitionHolder(this.beanDefinition, "");
        this.propertyValues.addPropertyValue(this.key, bdh);
        Map<String, String> m = new HashMap<String, String>();
        m.put("additional-key", "additional-value");
        this.propertyValues.addPropertyValue("sourceMap", m);

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);

        assertTrue(this.propertyValues.contains(this.key));
        assertContainsKey("additional-key");
    }

    @Test
    public void existingBeanReference() {
        BeanReference br = new RuntimeBeanNameReference(this.beanName);
        this.propertyValues.addPropertyValue(this.key, br);
        Map<String, String> m = new HashMap<String, String>();
        m.put("additional-key", "additional-value");
        this.propertyValues.addPropertyValue("sourceMap", m);

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);

        assertTrue(this.propertyValues.contains(this.key));
        assertContainsKey("additional-key");
    }

    @Test
    public void existingPropertiesFactoryBeanLocation() {
        BeanReference br = new RuntimeBeanNameReference(this.beanName);
        this.propertyValues.addPropertyValue(this.key, br);
        when(this.beanDefinition.getBeanClassName()).thenReturn(PropertiesFactoryBean.class.getCanonicalName());
        this.propertyValues.addPropertyValue("location", "alpha.properties");

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);

        assertTrue(this.propertyValues.contains(this.key));
        assertContainsKey("alpha");
    }

    @Test
    public void existingPropertiesFactoryBeanLocationTypedStringValue() {
        BeanReference br = new RuntimeBeanNameReference(this.beanName);
        this.propertyValues.addPropertyValue(this.key, br);
        when(this.beanDefinition.getBeanClassName()).thenReturn(PropertiesFactoryBean.class.getCanonicalName());
        this.propertyValues.addPropertyValue("location", new TypedStringValue("alpha.properties"));

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);

        assertTrue(this.propertyValues.contains(this.key));
        assertContainsKey("alpha");
    }

    @Test(expected = IllegalArgumentException.class)
    public void existingPropertiesFactoryBeanLocationIllegal() {
        BeanReference br = new RuntimeBeanNameReference(this.beanName);
        this.propertyValues.addPropertyValue(this.key, br);
        when(this.beanDefinition.getBeanClassName()).thenReturn(PropertiesFactoryBean.class.getCanonicalName());
        this.propertyValues.addPropertyValue("location", new Object());

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);
    }

    @Test
    public void existingPropertiesFactoryBeanLocationsList() {
        BeanReference br = new RuntimeBeanNameReference(this.beanName);
        this.propertyValues.addPropertyValue(this.key, br);
        when(this.beanDefinition.getBeanClassName()).thenReturn(PropertiesFactoryBean.class.getCanonicalName());
        this.propertyValues.addPropertyValue("locations", Arrays.asList("alpha.properties", "bravo.properties"));

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);

        assertTrue(this.propertyValues.contains(this.key));
        assertContainsKey("alpha");
        assertContainsKey("bravo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void existingPropertiesFactoryBeanLocationsIllegal() {
        BeanReference br = new RuntimeBeanNameReference(this.beanName);
        this.propertyValues.addPropertyValue(this.key, br);
        when(this.beanDefinition.getBeanClassName()).thenReturn(PropertiesFactoryBean.class.getCanonicalName());
        this.propertyValues.addPropertyValue("locations", Sets.asSet("alpha.properties", "bravo.properties"));

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void existingPropertiesFactoryBeanNoLocationOrProperties() {
        BeanReference br = new RuntimeBeanNameReference(this.beanName);
        this.propertyValues.addPropertyValue(this.key, br);
        when(this.beanDefinition.getBeanClassName()).thenReturn(PropertiesFactoryBean.class.getCanonicalName());

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);
    }

    @Test
    public void existingTypedStringValue() {
        TypedStringValue tsv = new TypedStringValue("additional-key=additional-value");
        this.propertyValues.addPropertyValue(this.key, tsv);

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);

        assertTrue(this.propertyValues.contains(this.key));
        assertContainsKey("additional-key");
    }

    @Test
    public void existingPropertiesFactoryBeanPropertiesBeanDefinitionHolder() {
        BeanReference br = new RuntimeBeanNameReference(this.beanName);
        this.propertyValues.addPropertyValue(this.key, br);
        when(this.beanDefinition.getBeanClassName()).thenReturn(PropertiesFactoryBean.class.getCanonicalName(),
                Map.class.getCanonicalName());
        BeanDefinitionHolder bdh = new BeanDefinitionHolder(this.beanDefinition, "");
        this.propertyValues.addPropertyValue("properties", bdh);
        Map<String, String> m = new HashMap<String, String>();
        m.put("additional-key", "additional-value");
        this.propertyValues.addPropertyValue("sourceMap", m);

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);

        assertTrue(this.propertyValues.contains(this.key));
        assertContainsKey("additional-key");
    }

    @Test
    public void existingPropertiesFactoryBeanPropertiesMap() {
        BeanReference br = new RuntimeBeanNameReference(this.beanName);
        this.propertyValues.addPropertyValue(this.key, br);
        when(this.beanDefinition.getBeanClassName()).thenReturn(PropertiesFactoryBean.class.getCanonicalName());
        Map<String, String> m = new HashMap<String, String>();
        m.put("additional-key", "additional-value");
        this.propertyValues.addPropertyValue("properties", m);

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);

        assertTrue(this.propertyValues.contains(this.key));
        assertContainsKey("additional-key");
    }

    @Test(expected = IllegalStateException.class)
    public void existingIllegal() {
        Object o = new Object();
        this.propertyValues.addPropertyValue(this.key, o);

        this.propertyAugmenter.augment(this.beanFactory, this.beanClass, this.key, this.additionalProperties);
    }

    private void assertContainsKey(String key) {
        PropertyValue propertyValue = this.propertyValues.getPropertyValue(this.key);
        ManagedProperties managedProperties = (ManagedProperties) propertyValue.getValue();
        assertTrue(managedProperties.containsKey(key));
    }
}
