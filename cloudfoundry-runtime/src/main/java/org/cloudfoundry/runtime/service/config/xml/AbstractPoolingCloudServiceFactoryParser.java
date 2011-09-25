package org.cloudfoundry.runtime.service.config.xml;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Thomas Risberg
 */
abstract class AbstractPoolingCloudServiceFactoryParser extends AbstractNestedElementCloudServiceFactoryParser {

	public AbstractPoolingCloudServiceFactoryParser(Class<?> beanClass, Class<? extends AbstractServiceInfo> serviceInfoClass) {
		super(beanClass, serviceInfoClass);
	}

	protected BeanDefinition parsePoolElement(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder cloudPoolConfigurationBeanBuilder =
				BeanDefinitionBuilder.genericBeanDefinition("org.cloudfoundry.runtime.service.CloudPoolConfiguration");
		String poolSize = element.getAttribute("pool-size");
		if (StringUtils.hasText(poolSize)) {
			cloudPoolConfigurationBeanBuilder.addPropertyValue("poolSize", poolSize);
		}
		String maxWaitTime = element.getAttribute("max-wait-time");
		if (StringUtils.hasText(maxWaitTime)) {
			cloudPoolConfigurationBeanBuilder.addPropertyValue("maxWaitTime", maxWaitTime);
		}
		return cloudPoolConfigurationBeanBuilder.getBeanDefinition();
	}
}
