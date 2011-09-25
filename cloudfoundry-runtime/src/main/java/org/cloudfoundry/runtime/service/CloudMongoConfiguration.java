package org.cloudfoundry.runtime.service;

import com.mongodb.WriteConcern;

/**
 * Class to hold configuration values for Mongo
 *
 * @author Thomas Risberg
 */
public class CloudMongoConfiguration {

	private WriteConcern writeConcern;

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

	public WriteConcern getWriteConcern() {
		return writeConcern;
	}

	public void setWriteConcern(WriteConcern writeConcern) {
		this.writeConcern = writeConcern;
	}

	public void setConnectionsPerHost(Integer connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}

	public void setMaxWaitTime(Integer maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}
}
