package org.cloudfoundry.runtime.service;

/**
 * Class to hold configuration values for a Cache
 *
 * @author Thomas Risberg
 */
public class CloudCacheConfiguration {

	private Integer channelCacheSize;

	public Integer getChannelCacheSize() {
		return channelCacheSize;
	}

	public void setChannelCacheSize(Integer channelCacheSize) {
		this.channelCacheSize = channelCacheSize;
	}
}
