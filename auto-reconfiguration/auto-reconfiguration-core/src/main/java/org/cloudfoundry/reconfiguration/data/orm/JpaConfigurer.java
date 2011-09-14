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
 * Implementation of {@link Configurer} that replaces the jpaProperties of
 * AbstractEntityManagerFactoryBean with properties specific to the
 * cloud-provided DataSource
 *
 * @author Ramnivas Laddad
 * @author Xin Li
 * @author Jennifer Hickey
 *
 */
public class JpaConfigurer implements Configurer {

	private static final String APP_CLOUD_JPA_MYSQL_REPLACEMENT_PROPERTIES = "__appCloudJpaMySQLReplacementProperties";

	private static final String APP_CLOUD_JPA_POSTGRESQL_REPLACEMENT_PROPERTIES = "__appCloudJpaPostgreSQLReplacementProperties";

	private PropertyReplacer propertyReplacer = new PropertyReplacer();

	private List<Map<String,Object>> cloudServices;

	public JpaConfigurer(List<Map<String,Object>> cloudServices) {
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
				propertyReplacer.replaceProperty(beanFactory,
						"org.springframework.orm.jpa.AbstractEntityManagerFactoryBean",
						APP_CLOUD_JPA_POSTGRESQL_REPLACEMENT_PROPERTIES, "jpaProperties");
				configured = true;
			} else if (label.startsWith("mysql")) {
				propertyReplacer.replaceProperty(beanFactory,
						"org.springframework.orm.jpa.AbstractEntityManagerFactoryBean",
						APP_CLOUD_JPA_MYSQL_REPLACEMENT_PROPERTIES, "jpaProperties");
				configured = true;
			}
		}
		return configured;
	}
}
