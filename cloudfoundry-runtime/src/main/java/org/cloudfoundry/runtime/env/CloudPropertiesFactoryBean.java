package org.cloudfoundry.runtime.env;

import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;

/**
 * FactoryBean exposing cloud sproperties. Suitable for property placeholder
 * processing.
 *
 * @author Scott Andrews
 */
public class CloudPropertiesFactoryBean implements FactoryBean<Properties> {

	private CloudEnvironment cloudEnvironment = new CloudEnvironment();

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public Class<Properties> getObjectType() {
		return Properties.class;
	}

	@Override
	public Properties getObject() throws Exception {
		return cloudEnvironment.getCloudProperties();
	}

	void setCloudEnvironment(CloudEnvironment cloudEnvironment) {
		this.cloudEnvironment = cloudEnvironment;
	}

}
