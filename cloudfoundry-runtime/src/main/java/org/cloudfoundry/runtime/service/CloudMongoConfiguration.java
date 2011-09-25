package org.cloudfoundry.runtime.service;

/**
 * Class to hold configuration values for Mongo
 *
 * @author Thomas Risberg
 */
public class CloudMongoConfiguration {

	private Boolean writeFsync = null;

	private Integer connectionsPerHost = null;

	private Integer maxWaitTime;

	/**
	 * Getter corresponding to the MongoOptions fsync field
	 */
	public Boolean getFsync() {
		return writeFsync;
	}

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

	public void setWriteFsync(Boolean writeFsync) {
		this.writeFsync = writeFsync;
	}

	public void setConnectionsPerHost(Integer connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}

	public void setMaxWaitTime(Integer maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}
}
