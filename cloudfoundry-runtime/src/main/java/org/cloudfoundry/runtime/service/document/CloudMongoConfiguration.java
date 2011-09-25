package org.cloudfoundry.runtime.service.document;

/**
 * Class to hold configuration values for Mongo
 *
 * @author Thomas Risberg
 */
public class CloudMongoConfiguration {

	private String writeConcern;

	private Integer connectionsPerHost = null;

	private Integer maxWaitTime;

	/**
	 * Getter corresponding to the MongoOptions connectionsPerHost field
	 */
	public Integer getConnectionsPerHost() {
		return connectionsPerHost;
	}

	/**
	 * Getter corresponding to the MongoOptions maxWaitTime  field
	 */
	public Integer getMaxWaitTime () {
		return maxWaitTime ;
	}

	public String getWriteConcern() {
		return writeConcern;
	}

	public void setWriteConcern(String writeConcern) {
		this.writeConcern = writeConcern;
	}

	public void setConnectionsPerHost(Integer connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}

	public void setMaxWaitTime(Integer maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}
}
