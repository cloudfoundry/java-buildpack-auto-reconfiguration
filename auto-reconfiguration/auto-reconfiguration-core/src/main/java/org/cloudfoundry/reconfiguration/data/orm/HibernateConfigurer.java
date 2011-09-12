/*
 * Copyright (c) 2009-2011 VMware, Inc.
 */
package org.cloudfoundry.reconfiguration.data.orm;

import java.util.List;
import java.util.Map;

import org.cloudfoundry.reconfiguration.Configurer;
import org.cloudfoundry.reconfiguration.PropertyReplacer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * Implementation of {@link Configurer} that replaces the hibernateProperties of
 * AbstractSessionFactoryBean with properties specific to the cloud-provided
 * DataSource
 *
 * @author Ramnivas Laddad
 * @author Xin Li
 * @author Jennifer Hickey
 *
 */
public class HibernateConfigurer implements Configurer {

	private static final String APP_CLOUD_HIBERNATE_MYSQL_REPLACEMENT_PROPERTIES = "__appCloudHibernateMySQLReplacementProperties";

	private static final String APP_CLOUD_HIBERNATE_POSTGRESQL_REPLACEMENT_PROPERTIES = "__appCloudHibernatePostgreSQLReplacementProperties";

	private PropertyReplacer propertyReplacer = new PropertyReplacer();

	private List<Map<String,Object>> cloudServices;

	public HibernateConfigurer(List<Map<String,Object>> cloudServices) {
		this.cloudServices = cloudServices;
	}

	public boolean configure(DefaultListableBeanFactory beanFactory) {
		boolean configured = false;
		for (Map<String, Object> service : cloudServices) {
			String label = (String) service.get("label");
			if (label == null) {
				continue;
			}
			if (label.startsWith("postgresql")) {
				replaceHibernateProperties(APP_CLOUD_HIBERNATE_POSTGRESQL_REPLACEMENT_PROPERTIES,beanFactory);
				configured = true;
			} else if (label.startsWith("mysql")) {
				replaceHibernateProperties(APP_CLOUD_HIBERNATE_MYSQL_REPLACEMENT_PROPERTIES,beanFactory);
				configured = true;
			}
		}
		return configured;
	}

	private void replaceHibernateProperties(String replacementPropertiesBeanName, DefaultListableBeanFactory beanFactory) {
		propertyReplacer.replaceProperty(beanFactory, "org.springframework.orm.hibernate3.AbstractSessionFactoryBean",
				replacementPropertiesBeanName, "hibernateProperties");
		// Spring 3.1
		propertyReplacer.replaceProperty(beanFactory,
				"org.springframework.orm.hibernate3.SessionFactoryBuilderSupport", replacementPropertiesBeanName,
				"hibernateProperties");
	}

}