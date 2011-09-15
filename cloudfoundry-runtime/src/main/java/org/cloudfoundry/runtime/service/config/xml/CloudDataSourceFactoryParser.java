package org.cloudfoundry.runtime.service.config.xml;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for the <cloud:data-source> namespace element
 *
 * @author Thomas Risberg
 */
public class CloudDataSourceFactoryParser extends CloudServiceFactoryParser {

	private static Map<String, String> NAMESPACE_TO_BEAN_PROPERTIES_MAP = new HashMap<String,String>();

	static {
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("connection-properties", "connectionProperties");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("validation-query", "validationQuery");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("validation-query-timeout", "validationQueryTimeout");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("test-on-borrow", "testOnBorrow");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("test-on-return", "testOnReturn");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("test-while-idle", "testWhileIdle");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("initial-size", "initialSize");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("max-active", "maxActive");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("max-idle", "maxIdle");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("min-idle", "minIdle");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("max-wait", "maxWait");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("pool-prepared-statements", "poolPreparedStatements");
		NAMESPACE_TO_BEAN_PROPERTIES_MAP.put("max-open-prepared-statements", "maxOpenPreparedStatements");
	}

	public CloudDataSourceFactoryParser(Class<?> beanClass, Class<? extends AbstractServiceInfo> serviceInfoClass) {
		super(beanClass, serviceInfoClass, NAMESPACE_TO_BEAN_PROPERTIES_MAP);
	}
}
