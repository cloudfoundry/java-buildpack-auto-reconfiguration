package org.cloudfoundry.runtime.service.config.xml;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Node;

/**
 * @author Thomas Risberg
 */
abstract class AbstractNestedElementCloudServiceFactoryParser extends AbstractCloudServiceFactoryParser {

	public AbstractNestedElementCloudServiceFactoryParser(Class<?> beanClass, Class<? extends AbstractServiceInfo> serviceInfoClass) {
		super(beanClass, serviceInfoClass);
	}

	protected boolean isElement(Node node, ParserContext parserContext, String elementName) {
		return node.getNodeType() == Node.ELEMENT_NODE &&
				elementName.equals(parserContext.getDelegate().getLocalName(node));
	}
}
