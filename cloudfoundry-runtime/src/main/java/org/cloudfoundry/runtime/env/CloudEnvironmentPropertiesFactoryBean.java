package org.cloudfoundry.runtime.env;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;

/**
 * FactoryBean exposing the current cloud service settings as properties, 
 * suitable for property placeholder processing.
 * 
 * @author Costin Leau
 */
public class CloudEnvironmentPropertiesFactoryBean implements FactoryBean<Properties> {

	private Properties properties;
	private CloudEnvironment cloudEnv;

	/**
	 * Sets the cloud environment (optional) for this factory bean.
	 * 
	 * @param cloudEnv The cloudEnv to set.
	 */
	public void setCloudEnv(CloudEnvironment cloudEnv) {
		this.cloudEnv = cloudEnv;
	}

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
		if (properties == null) {
			properties = loadProperties();
		}
		return properties;
	}

	@SuppressWarnings("unchecked")
	private Properties loadProperties() {
		Properties props = new Properties();
		CloudEnvironment cloudEnv = (this.cloudEnv != null ? this.cloudEnv : new CloudEnvironment());
		List<Map<String, Object>> services = cloudEnv.getServices();

		for (Map<String, Object> map : services) {
			String name = String.valueOf(map.get("name"));

			Map<String, Object> ops = (Map<String, Object>) map.get("credentials");

			if (ops != null) {
				for (Map.Entry<String, Object> entry : ops.entrySet()) {
					props.put(name + "." + entry.getKey(), String.valueOf(entry.getValue()));
				}
			}
		}

		return props;
	}
}