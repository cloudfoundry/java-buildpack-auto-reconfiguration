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
 * Parser for the <cloud:rabbit-connection-factory> namespace element
 *
 * @author Thomas Risberg
 */
public class CloudRabbitConnectionFactoryParser extends AbstractNestedElementCloudServiceFactoryParser {

	private static final String ELEMENT_CACHE = "cache";

	public CloudRabbitConnectionFactoryParser(Class<?> beanClass, Class<? extends AbstractServiceInfo> serviceInfoClass) {
		super(beanClass, serviceInfoClass);
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		super.doParse(element, parserContext, builder);
		BeanDefinition cloudCacheConfiguration = null;
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (isElement(child, parserContext, ELEMENT_CACHE)) {
				cloudCacheConfiguration = parseCacheElement((Element) child);
			}
		}
		if (cloudCacheConfiguration != null) {
			builder.addPropertyValue("cloudCacheConfiguration", cloudCacheConfiguration);
		}
	}

	private BeanDefinition parseCacheElement(Element element) {
		BeanDefinitionBuilder cloudCacheConfigurationBeanBuilder =
				BeanDefinitionBuilder.genericBeanDefinition("org.cloudfoundry.runtime.service.CloudCacheConfiguration");
		String channelCacheSize = element.getAttribute("channel-cache-size");
		if (StringUtils.hasText(channelCacheSize)) {
			cloudCacheConfigurationBeanBuilder.addPropertyValue("channelCacheSize", channelCacheSize);
		}
		return cloudCacheConfigurationBeanBuilder.getBeanDefinition();
	}
}
