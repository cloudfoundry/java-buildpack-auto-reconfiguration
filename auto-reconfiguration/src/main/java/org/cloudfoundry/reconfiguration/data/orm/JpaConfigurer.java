/*
 * Copyright (c) 2009-2011 VMware, Inc.
 */
package org.cloudfoundry.reconfiguration.data.orm;

import javax.sql.DataSource;

import org.cloudfoundry.reconfiguration.Configurer;
import org.cloudfoundry.reconfiguration.PropertyReplacer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;

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

	private Cloud cloud;

	public JpaConfigurer(Cloud cloud) {
		this.cloud = cloud;
	}

	public boolean configure(DefaultListableBeanFactory beanFactory) {
		boolean configured = false;
		for (ServiceInfo serviceInfo : cloud.getServiceInfos(DataSource.class)) {
			if (serviceInfo instanceof PostgresqlServiceInfo) {
				propertyReplacer.replaceProperty(beanFactory,
						"org.springframework.orm.jpa.AbstractEntityManagerFactoryBean",
						APP_CLOUD_JPA_POSTGRESQL_REPLACEMENT_PROPERTIES, "jpaProperties");
				configured = true;
			} else if (serviceInfo instanceof MysqlServiceInfo) {
				propertyReplacer.replaceProperty(beanFactory,
						"org.springframework.orm.jpa.AbstractEntityManagerFactoryBean",
						APP_CLOUD_JPA_MYSQL_REPLACEMENT_PROPERTIES, "jpaProperties");
				configured = true;
			}
		}
		return configured;
	}
}
