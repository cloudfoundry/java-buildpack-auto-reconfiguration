package org.cloudfoundry.runtime.service.document;

/**
 * Class to hold configuration values for MongoOptions
 *
 * @author Thomas Risberg
 *
 */

public class MongoConfiguration {

	private Boolean autoConnectRetry = null;
	private Long maxAutoConnectRetryTime = null;
	private Boolean socketKeepAlive = null;
	private Integer socketTimeout = null;
	private Boolean fsync = null;
	private Integer connectionsPerHost = null;
	private Integer threadsAllowedToBlockForConnectionMultiplier = null;
	private Integer connectTimeout = null;
	private Integer maxWaitTime = null;

	public Boolean getAutoConnectRetry() {
		return autoConnectRetry;
	}

	public void setAutoConnectRetry(Boolean autoConnectRetry) {
		this.autoConnectRetry = autoConnectRetry;
	}

	public Long getMaxAutoConnectRetryTime() {
		return maxAutoConnectRetryTime;
	}

	public void setMaxAutoConnectRetryTime(Long maxAutoConnectRetryTime) {
		this.maxAutoConnectRetryTime = maxAutoConnectRetryTime;
	}

	public Boolean getSocketKeepAlive() {
		return socketKeepAlive;
	}

	public void setSocketKeepAlive(Boolean socketKeepAlive) {
		this.socketKeepAlive = socketKeepAlive;
	}

	public Integer getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public Boolean getFsync() {
		return fsync;
	}

	public void setFsync(Boolean fsync) {
		this.fsync = fsync;
	}

	public Integer getConnectionsPerHost() {
		return connectionsPerHost;
	}

	public void setConnectionsPerHost(Integer connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}

	public Integer getThreadsAllowedToBlockForConnectionMultiplier() {
		return threadsAllowedToBlockForConnectionMultiplier;
	}

	public void setThreadsAllowedToBlockForConnectionMultiplier(Integer threadsAllowedToBlockForConnectionMultiplier) {
		this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
	}

	public Integer getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Integer getMaxWaitTime() {
		return maxWaitTime;
	}

	public void setMaxWaitTime(Integer maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}
}
