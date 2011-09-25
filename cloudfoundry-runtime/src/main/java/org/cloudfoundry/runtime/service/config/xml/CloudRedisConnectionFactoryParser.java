package org.cloudfoundry.runtime.service.config.xml;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parser for the <cloud:redis-connection-factory> namespace element
 *
 * @author Thomas Risberg
 */
public class CloudRedisConnectionFactoryParser extends AbstractPoolingCloudServiceFactoryParser {

	private static final String ELEMENT_POOL = "pool";

	public CloudRedisConnectionFactoryParser(Class<?> beanClass, Class<? extends AbstractServiceInfo> serviceInfoClass) {
		super(beanClass, serviceInfoClass);
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		super.doParse(element, parserContext, builder);
		BeanDefinition cloudPoolConfiguration = null;
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (isElement(child, parserContext, ELEMENT_POOL)) {
				cloudPoolConfiguration = parsePoolElement((Element) child, parserContext);
			}
		}
		if (cloudPoolConfiguration != null) {
			builder.addPropertyValue("cloudPoolConfiguration", cloudPoolConfiguration);
		}
	}
}
