package org.cloudfoundry.runtime.service.messaging;

/**
 * Class to hold configuration values for a Rabbit
 *
 * @author Thomas Risberg
 */
public class CloudRabbitConfiguration {

	private Integer channelCacheSize;

	public Integer getChannelCacheSize() {
		return channelCacheSize;
	}

	public void setChannelCacheSize(Integer channelCacheSize) {
		this.channelCacheSize = channelCacheSize;
	}
}
