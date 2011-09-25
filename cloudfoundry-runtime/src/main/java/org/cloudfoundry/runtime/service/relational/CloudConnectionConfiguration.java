package org.cloudfoundry.runtime.service.relational;

/**
 * Class to hold configuration values for a Connection
 *
 * @author Thomas Risberg
 */
public class CloudConnectionConfiguration {

	private String connectionProperties = null;

	public String getConnectionProperties() {
		return connectionProperties;
	}

	public void setConnectionProperties(String connectionProperties) {
		this.connectionProperties = connectionProperties;
	}
}
