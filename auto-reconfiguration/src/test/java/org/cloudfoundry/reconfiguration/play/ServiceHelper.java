package org.cloudfoundry.reconfiguration.play;

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.runtime.env.RdbmsServiceInfo;

/**
 * Helper methods for testing with services
 *
 * @author Jennifer Hickey
 *
 */
abstract class ServiceHelper {

	/**
	 * Creates a representation of a relational DB service
	 *
	 * @param serviceName
	 * @param hostname
	 * @param port
	 * @param user
	 * @param password
	 * @param name
	 * @param label
	 * @return A representation of a relational DB service
	 */
	static RdbmsServiceInfo createServiceInfo(String serviceName, String hostname, int port, String user,
			String password, String name, String label) {
		Map<String, Object> serviceInfo = new HashMap<String, Object>();
		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("hostname", hostname);
		credentials.put("port", port);
		credentials.put("password", password);
		credentials.put("name", name);
		credentials.put("user", user);
		serviceInfo.put("credentials", credentials);
		serviceInfo.put("name", serviceName);
		serviceInfo.put("play", "free");
		serviceInfo.put("label", label);
		return new RdbmsServiceInfo(serviceInfo);
	}
}
