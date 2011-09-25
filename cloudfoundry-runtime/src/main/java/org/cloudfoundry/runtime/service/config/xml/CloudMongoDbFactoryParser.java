package org.cloudfoundry.runtime.service.config.xml;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parser for the <cloud:mongo-db-factory> namespace element
 *
 * @author Thomas Risberg
 */
public class CloudMongoDbFactoryParser extends AbstractNestedElementCloudServiceFactoryParser {

	private static final String ELEMENT_MONGO_OPTIONS = "mongo-options";

	public CloudMongoDbFactoryParser(Class<?> beanClass, Class<? extends AbstractServiceInfo> serviceInfoClass) {
		super(beanClass, serviceInfoClass);
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		super.doParse(element, parserContext, builder);
		BeanDefinition cloudMongoConfiguration = null;
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (isElement(child, parserContext, ELEMENT_MONGO_OPTIONS)) {
				cloudMongoConfiguration = parseMongoOptionsElement((Element) child);
			}
		}
		if (cloudMongoConfiguration != null) {
			builder.addPropertyValue("cloudMongoConfiguration", cloudMongoConfiguration);
		}
	}

	private BeanDefinition parseMongoOptionsElement(Element element) {
		BeanDefinitionBuilder cloudMongoConfigurationBeanBuilder =
				BeanDefinitionBuilder.genericBeanDefinition("org.cloudfoundry.runtime.service.CloudMongoConfiguration");
		String writeFsync = element.getAttribute("write-fsync");
		if (StringUtils.hasText(writeFsync)) {
			cloudMongoConfigurationBeanBuilder.addPropertyValue("writeFsync", writeFsync);
		}
		String connectionsPerHost = element.getAttribute("connections-per-host");
		if (StringUtils.hasText(connectionsPerHost)) {
			cloudMongoConfigurationBeanBuilder.addPropertyValue("connectionsPerHost", connectionsPerHost);
		}
		String maxWaitTime = element.getAttribute("max-wait-time");
		if (StringUtils.hasText(maxWaitTime)) {
			cloudMongoConfigurationBeanBuilder.addPropertyValue("maxWaitTime", maxWaitTime);
		}
		return cloudMongoConfigurationBeanBuilder.getBeanDefinition();
	}
}
