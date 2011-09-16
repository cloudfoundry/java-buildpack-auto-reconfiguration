package org.cloudfoundry.runtime.service.config.xml;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for the <cloud:data-source> namespace element
 *
 * @author Thomas Risberg
 */
public class CloudMongoDbFactoryParser extends CloudServiceFactoryParser {

	private static Map<String, String> NAMESPACE_TO_BEAN_PROPERTIES_MAP = new HashMap<String,String>();

	static {
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("auto-connect-retry", "autoConnectRetry");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("max-auto-connect-retry-time", "maxAutoConnectRetryTime");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("socket-keep-alive", "socketKeepAlive");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("socket-timeout", "socketTimeout");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("write-fsync", "fsync");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("connections-per-host", "connectionsPerHost");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("threads-allowed-to-block-for-connection-multiplier",
				"threadsAllowedToBlockForConnectionMultiplier");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("connect-timeout", "connectTimeout");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("max-wait-time", "maxWaitTime");
	}

	public CloudMongoDbFactoryParser(Class<?> beanClass, Class<? extends AbstractServiceInfo> serviceInfoClass) {
		super(beanClass, serviceInfoClass, NAMESPACE_TO_BEAN_PROPERTIES_MAP);
	}
}
