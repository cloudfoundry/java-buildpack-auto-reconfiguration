/*
 * Copyright (c) 2009-2011 VMware, Inc.
 */
package org.cloudfoundry.reconfiguration.data.relational;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.cloudfoundry.reconfiguration.AbstractServiceConfigurer;
import org.cloudfoundry.reconfiguration.Configurer;
import org.cloudfoundry.reconfiguration.data.orm.HibernateConfigurer;
import org.cloudfoundry.reconfiguration.data.orm.JpaConfigurer;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.cloudfoundry.runtime.service.relational.RdbmsServiceCreator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * Implementation of {@link Configurer} that replaces a single
 * {@link DataSource} with one connecting to a postgresql or mysql cloud service
 * bound to the current application.
 *
 * @author Ramnivas Laddad
 * @author Xin Li
 * @author Jennifer Hickey
 *
 */
public class DataSourceConfigurer extends AbstractServiceConfigurer<RdbmsServiceInfo> {

	private static final String APP_CLOUD_DATA_SOURCE_NAME = "__appCloudDataSource";

	private static final String DATA_SOURCE_CLASS_NAME = "javax.sql.DataSource";

	private Configurer hibernateConfigurer;

	private Configurer jpaConfigurer;

	public DataSourceConfigurer(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, RdbmsServiceInfo.class);
		this.hibernateConfigurer = new HibernateConfigurer(cloudEnvironment);
		this.jpaConfigurer = new JpaConfigurer(cloudEnvironment);
	}

	@Override
	public boolean configure(DefaultListableBeanFactory defaultListableBeanFactory) {
		boolean configured = super.configure(defaultListableBeanFactory);
		if (configured) {
			jpaConfigurer.configure(defaultListableBeanFactory);
			hibernateConfigurer.configure(defaultListableBeanFactory);
		}
		return configured;
	}

	@Override
	public String getBeanClass() {
		return DATA_SOURCE_CLASS_NAME;
	}

	@Override
	public String getServiceBeanName() {
		return APP_CLOUD_DATA_SOURCE_NAME;
	}

	@Override
	public AbstractServiceCreator<?, RdbmsServiceInfo> getServiceCreator() {
		return new RdbmsServiceCreator();
	}

	@Override
	protected String[] getBeanNames(DefaultListableBeanFactory beanFactory) {
		String[] dataSourceBeanNames = super.getBeanNames(beanFactory);
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

	private <T> boolean contains(T[] array, T searchElement) {
		for (T element : array) {
			if (element.equals(searchElement)) {
				return true;
			}
		}
		return false;
	}

	private BeanDefinition getBeanDefinition(ConfigurableListableBeanFactory beanFactory, String beanName) {
		if (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
		}
		return beanFactory.getBeanDefinition(beanName);

	}
}
