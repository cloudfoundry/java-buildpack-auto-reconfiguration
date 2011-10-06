/*
 * Copyright (c) 2009-2011 VMware, Inc.
 */
package org.cloudfoundry.reconfiguration.data.orm;

import org.cloudfoundry.reconfiguration.Configurer;
import org.cloudfoundry.reconfiguration.PropertyReplacer;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
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

	private CloudEnvironment cloudEnvironment;

	public HibernateConfigurer(CloudEnvironment cloudEnvironment) {
		this.cloudEnvironment = cloudEnvironment;
	}

	public boolean configure(DefaultListableBeanFactory beanFactory) {
		boolean configured = false;
		for (RdbmsServiceInfo service : cloudEnvironment.getServiceInfos(RdbmsServiceInfo.class)) {
			if (service.getLabel().startsWith("postgresql")) {
				replaceHibernateProperties(APP_CLOUD_HIBERNATE_POSTGRESQL_REPLACEMENT_PROPERTIES, beanFactory);
				configured = true;
			} else if (service.getLabel().startsWith("mysql")) {
				replaceHibernateProperties(APP_CLOUD_HIBERNATE_MYSQL_REPLACEMENT_PROPERTIES, beanFactory);
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