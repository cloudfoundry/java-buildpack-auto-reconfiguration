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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.ManagedProperties;

/**
 * Augments a set of properties declared in a bean definition with another set of properties
 */
public interface PropertyAugmenter {

    /**
     * Augment a set of properties declared as a bean definition in a {@link ConfigurableListableBeanFactory} with an
     * additional set of properties.  If a property with a key that overlaps the original bean definition exists,
     * the value from the additional list will overwrite the original.
     *
     * @param beanFactory the factory to examine for augmentation candidates
     * @param beanClass the type of bean to augment the properties within
     * @param key the key of the bean property to augment
     * @param additionalProperties the additional properties to augment with
     */
    void augment(ConfigurableListableBeanFactory beanFactory, Class<?> beanClass, String key,
                 ManagedProperties additionalProperties);

}
