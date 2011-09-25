package org.cloudfoundry.runtime.service.config.xml;

import org.cloudfoundry.runtime.env.CloudPropertiesFactoryBean;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.CloudServicesScanner;
import org.cloudfoundry.runtime.service.document.CloudMongoDbFactoryBean;
import org.cloudfoundry.runtime.service.keyvalue.CloudRedisConnectionFactoryBean;
import org.cloudfoundry.runtime.service.messaging.CloudRabbitConnectionFactoryBean;
import org.cloudfoundry.runtime.service.relational.CloudDataSourceFactory;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.w3c.dom.Element;

/**
 * Handler for the 'cloud' namespace
 *
 * @author Mark Fisher
 * @author Costin Leau
 * @author Ramnivas Laddad
 * @author Scott Andrews
 * @author Thomas Risberg
 *
 */
public class CloudNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		this.registerBeanDefinitionParser("rabbit-connection-factory",
				new CloudRabbitConnectionFactoryParser(CloudRabbitConnectionFactoryBean.class, RabbitServiceInfo.class));
		this.registerBeanDefinitionParser("redis-connection-factory",
				new CloudRedisConnectionFactoryParser(CloudRedisConnectionFactoryBean.class, RedisServiceInfo.class));
		this.registerBeanDefinitionParser("mongo-db-factory",
				new CloudMongoDbFactoryParser(CloudMongoDbFactoryBean.class, MongoServiceInfo.class));
		this.registerBeanDefinitionParser("data-source",
				new CloudDataSourceFactoryParser(CloudDataSourceFactory.class, RdbmsServiceInfo.class));

		this.registerBeanDefinitionParser("properties", new AbstractSimpleBeanDefinitionParser() {
			@Override
			protected Class<?> getBeanClass(Element element) {
				return CloudPropertiesFactoryBean.class;
			}
		});
		this.registerBeanDefinitionParser("service-scan", new AbstractSimpleBeanDefinitionParser() {
			@Override
			protected Class<?> getBeanClass(Element element) {
				return CloudServicesScanner.class;
			}

			@Override
			protected boolean shouldGenerateId() {
				return true;
			}
		});
	}
}
