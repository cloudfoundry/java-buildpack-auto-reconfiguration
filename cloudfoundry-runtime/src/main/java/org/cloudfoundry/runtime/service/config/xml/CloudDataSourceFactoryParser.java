package org.cloudfoundry.runtime.service.config.xml;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for the <cloud:data-source> namespace element
 *
 * @author Thomas Risberg
 */
public class CloudDataSourceFactoryParser extends CloudServiceFactoryParser {

	public CloudDataSourceFactoryParser(Class<?> beanClass, Class<? extends AbstractServiceInfo> serviceInfoClass) {
		super(beanClass, serviceInfoClass);
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		super.doParse(element, parserContext, builder);
		String connectionProperties = element.getAttribute("connection-properties");
		if (StringUtils.hasText(connectionProperties)) {
			builder.addPropertyValue("connectionProperties", connectionProperties);
		}
		String validationQuery = element.getAttribute("validation-query");
		if (StringUtils.hasText(validationQuery)) {
			builder.addPropertyValue("validationQuery", validationQuery);
		}
		String validationQueryTimeout = element.getAttribute("validation-query-timeout");
		if (StringUtils.hasText(validationQueryTimeout)) {
			builder.addPropertyValue("validationQueryTimeout", validationQueryTimeout);
		}
		String testOnBorrow = element.getAttribute("test-on-borrow");
		if (StringUtils.hasText(testOnBorrow)) {
			builder.addPropertyValue("testOnBorrow", testOnBorrow);
		}
		String testOnReturn = element.getAttribute("test-on-return");
		if (StringUtils.hasText(testOnReturn)) {
			builder.addPropertyValue("testOnReturn", testOnReturn);
		}
		String testWhileIdle = element.getAttribute("test-while-idle");
		if (StringUtils.hasText(testWhileIdle)) {
			builder.addPropertyValue("testWhileIdle", testWhileIdle);
		}
		String initialSize = element.getAttribute("initial-size");
		if (StringUtils.hasText(initialSize)) {
			builder.addPropertyValue("initialSize", initialSize);
		}
		String maxActive = element.getAttribute("max-active");
		if (StringUtils.hasText(maxActive)) {
			builder.addPropertyValue("maxActive", maxActive);
		}
		String maxIdle = element.getAttribute("max-idle");
		if (StringUtils.hasText(maxIdle)) {
			builder.addPropertyValue("maxIdle", maxIdle);
		}
		String minIdle = element.getAttribute("min-idle");
		if (StringUtils.hasText(minIdle)) {
			builder.addPropertyValue("minIdle", minIdle);
		}
		String maxWait = element.getAttribute("max-wait");
		if (StringUtils.hasText(maxWait)) {
			builder.addPropertyValue("maxWait", maxWait);
		}
		String poolPreparedStatements = element.getAttribute("pool-prepared-statements");
		if (StringUtils.hasText(poolPreparedStatements)) {
			builder.addPropertyValue("poolPreparedStatements", poolPreparedStatements);
		}
		String maxOpenPreparedStatements = element.getAttribute("max-open-prepared-statements");
		if (StringUtils.hasText(maxOpenPreparedStatements)) {
			builder.addPropertyValue("maxOpenPreparedStatements", maxOpenPreparedStatements);
		}
	}
}
