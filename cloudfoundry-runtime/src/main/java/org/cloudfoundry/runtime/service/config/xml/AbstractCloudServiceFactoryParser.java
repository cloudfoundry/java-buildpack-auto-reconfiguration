package org.cloudfoundry.runtime.service.config.xml;

import java.util.List;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Mark Fisher
 */
class AbstractCloudServiceFactoryParser extends AbstractSingleBeanDefinitionParser {

	private final Class<?> beanClass;

	private final Class<? extends AbstractServiceInfo> serviceInfoClass;

	private final CloudEnvironment cloudEnvironment;


	public AbstractCloudServiceFactoryParser(Class<?> beanClass, Class<? extends AbstractServiceInfo> serviceInfoClass) {
		Assert.notNull(beanClass, "beanClass must not be null");
		Assert.notNull(serviceInfoClass, "serviceInfoClass must not be null");
		this.beanClass = beanClass;
		this.serviceInfoClass = serviceInfoClass;
		this.cloudEnvironment = new CloudEnvironment();
	}


	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
		String id = element.getAttribute(ID_ATTRIBUTE);
		if (!StringUtils.hasText(id)) {
			id = element.getAttribute("service-name");
		}
		if (!StringUtils.hasText(id)) {
			List<? extends AbstractServiceInfo> serviceInfos = this.cloudEnvironment.getServiceInfos(this.serviceInfoClass);
			if (serviceInfos.size() != 1) {
				throw new CloudServiceException("No 'service-name' specified. Expected 1 service of " + serviceInfoClass + " type, but found " + serviceInfos.size());
			}
			id = serviceInfos.get(0).getServiceName();
		}
		return id;
	}

	@Override
	protected final Class<?> getBeanClass(Element element) {
		return this.beanClass;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		builder.addConstructorArgValue(this.cloudEnvironment);
		String serviceName = element.getAttribute("service-name");
		if (StringUtils.hasText(serviceName)) {
			builder.addPropertyValue("serviceName", serviceName);
		}
	}
}
