/*
 * Copyright (c) 2009-2011 VMware, Inc.
 */
package org.cloudfoundry.reconfiguration.data.relational;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.cloudfoundry.reconfiguration.Configurer;
import org.cloudfoundry.reconfiguration.data.orm.HibernateConfigurer;
import org.cloudfoundry.reconfiguration.data.orm.JpaConfigurer;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.service.relational.MysqlServiceCreator;
import org.cloudfoundry.runtime.service.relational.PostgresqlServiceCreator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * Implementation of {@link Configurer} that replaces a single
 * {@link DataSource} with one connecting to a postgresql or mysql cloud service bound
 * to the current application.
 *
 * @author Ramnivas Laddad
 * @author Xin Li
 * @author Jennifer Hickey
 *
 */
public class DataSourceConfigurer implements Configurer {

	private final Logger logger = Logger.getLogger(DataSourceConfigurer.class.getName());

	private static final String APP_CLOUD_DATA_SOURCE_NAME = "__appCloudDataSource";

	private Configurer hibernateConfigurer;

	private Configurer jpaConfigurer;

	private CloudEnvironment cloudEnvironment;

	public DataSourceConfigurer(CloudEnvironment cloudEnvironment) {
		this.cloudEnvironment = cloudEnvironment;
		this.hibernateConfigurer = new HibernateConfigurer(cloudEnvironment);
		this.jpaConfigurer = new JpaConfigurer(cloudEnvironment);
	}

	public boolean configure(DefaultListableBeanFactory defaultListableBeanFactory) {
		String[] dataSourceBeanNames = getRealDataSources(defaultListableBeanFactory);
		if (dataSourceBeanNames.length == 0) {
			logger.log(Level.INFO, "No datasources found in application context");
			return false;
		} else if (dataSourceBeanNames.length > 1) {
			logger.log(Level.INFO, "More than 1 (" + dataSourceBeanNames.length
					+ ") real datasources found in application context. Skipping autostaging.");
			return false;
		}

		ArrayList<DataSource> dataSourceList = new ArrayList<DataSource>();
		for (Map<String, Object> service : cloudEnvironment.getServices()) {
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
			logger.log(Level.INFO, "More than 1 (" + dataSourceList.size()
					+ ") database services found. Skipping autostaging");
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

		jpaConfigurer.configure(defaultListableBeanFactory);
		hibernateConfigurer.configure(defaultListableBeanFactory);
		return true;
	}

	private String[] getRealDataSources(ConfigurableListableBeanFactory beanFactory) {
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

	private BeanDefinition getBeanDefinition(ConfigurableListableBeanFactory beanFactory, String beanName) {
		if (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
		}
		return beanFactory.getBeanDefinition(beanName);

	}
}
