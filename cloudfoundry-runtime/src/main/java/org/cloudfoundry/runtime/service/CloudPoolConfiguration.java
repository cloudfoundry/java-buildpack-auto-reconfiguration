package org.cloudfoundry.runtime.service;

import org.springframework.util.StringUtils;

/**
 * Class to hold configuration values for a Pool
 *
 * @author Mark Fisher
 * @author Thomas Risberg
 */
public class CloudPoolConfiguration {

	private Integer minPoolSize;

	private Integer maxPoolSize;

	private Integer maxWaitTime;

	/**
	 * Getter corresponding to the DBCP initialSize property
	 */
	public Integer getInitialSize() {
		return minPoolSize;
	}

	/**
	 * Getter corresponding to the DBCP minIdle property
	 */
	public Integer getMinIdle() {
		return minPoolSize;
	}

	/**
	 * Getter corresponding to the DBCP maxActive property
	 */
	public Integer getMaxActive() {
		return maxPoolSize;
	}

	/**
	 * Getter corresponding to the DBCP maxWait property
	 */
	public Integer getMaxWait() {
		return maxWaitTime;
	}

	public void setPoolSize(String poolSize) {
		determinePoolSizeRange(poolSize);
	}

	public void setMaxWaitTime(Integer maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}

	private void determinePoolSizeRange(String poolSize) {
		if (StringUtils.hasText(poolSize)) {
			try {
				int minPoolSize;
				int maxPoolSize;
				int separatorIndex = poolSize.indexOf('-');
				if (separatorIndex != -1) {
					minPoolSize = Integer.valueOf(poolSize.substring(0, separatorIndex));
					maxPoolSize = Integer.valueOf(poolSize.substring(separatorIndex + 1, poolSize.length()));
					if (minPoolSize > maxPoolSize) {
						throw new IllegalArgumentException(
								"Lower bound of pool-size range must not exceed the upper bound");
					}
				}
				else {
					Integer value = Integer.valueOf(poolSize);
					minPoolSize = 0;
					maxPoolSize = value;
				}
				this.minPoolSize = Integer.valueOf(minPoolSize);
				this.maxPoolSize = Integer.valueOf(maxPoolSize);
			}
			catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Invalid pool-size value [" + poolSize + "]: only single " +
						"maximum integer (e.g. \"5\") and minimum-maximum range (e.g. \"3-5\") are supported", ex);
			}
		}
	}
}
