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

	private static final String ELEMENT_RABBIT_OPTIONS = "rabbit-options";

	public CloudRabbitConnectionFactoryParser(Class<?> beanClass, Class<? extends AbstractServiceInfo> serviceInfoClass) {
		super(beanClass, serviceInfoClass);
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		super.doParse(element, parserContext, builder);
		BeanDefinition cloudRabbitConfiguration = null;
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (isElement(child, parserContext, ELEMENT_RABBIT_OPTIONS)) {
				cloudRabbitConfiguration = parseRabbitOptionsElement((Element) child);
			}
		}
		if (cloudRabbitConfiguration != null) {
			builder.addPropertyValue("cloudRabbitConfiguration", cloudRabbitConfiguration);
		}
	}

	private BeanDefinition parseRabbitOptionsElement(Element element) {
		BeanDefinitionBuilder cloudRabbitConfigurationBeanBuilder =
				BeanDefinitionBuilder.genericBeanDefinition("org.cloudfoundry.runtime.service.messaging.CloudRabbitConfiguration");
		String channelCacheSize = element.getAttribute("channel-cache-size");
		if (StringUtils.hasText(channelCacheSize)) {
			cloudRabbitConfigurationBeanBuilder.addPropertyValue("channelCacheSize", channelCacheSize);
		}
		return cloudRabbitConfigurationBeanBuilder.getBeanDefinition();
	}
}
