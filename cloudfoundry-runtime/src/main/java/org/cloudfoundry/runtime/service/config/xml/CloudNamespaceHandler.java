package org.cloudfoundry.runtime.service.config.xml;

import org.cloudfoundry.runtime.env.CloudEnvironmentPropertiesFactoryBean;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.CloudServicesAutoPopulator;
import org.cloudfoundry.runtime.service.document.CloudMongoDbFactoryBean;
import org.cloudfoundry.runtime.service.keyvalue.CloudRedisConnectionFactoryBean;
import org.cloudfoundry.runtime.service.messaging.CloudRabbitConnectionFactoryBean;
import org.cloudfoundry.runtime.service.relational.CloudMySqlDataSourceFactory;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.w3c.dom.Element;

/**
 * Handler for the 'cloud' namespace
 * 
 * @author Mark Fisher
 * @author Costin Leau
 * @author Ramnivas Laddad
 */
public class CloudNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		this.registerBeanDefinitionParser("rabbit-connection-factory",
				new CloudServiceFactoryParser(CloudRabbitConnectionFactoryBean.class, RabbitServiceInfo.class));
		this.registerBeanDefinitionParser("redis-connection-factory",
				new CloudServiceFactoryParser(CloudRedisConnectionFactoryBean.class, RedisServiceInfo.class));
		this.registerBeanDefinitionParser("mongo-db-factory",
				new CloudServiceFactoryParser(CloudMongoDbFactoryBean.class, MongoServiceInfo.class));
		this.registerBeanDefinitionParser("data-source",
				new CloudServiceFactoryParser(CloudMySqlDataSourceFactory.class, MysqlServiceInfo.class));
		
		this.registerBeanDefinitionParser("service-properties", new AbstractSimpleBeanDefinitionParser() {
			@Override
			protected Class<?> getBeanClass(Element element) {
				return CloudEnvironmentPropertiesFactoryBean.class;
			}
		});
		this.registerBeanDefinitionParser("auto-populate", new AbstractSimpleBeanDefinitionParser() {
			@Override
			protected Class<?> getBeanClass(Element element) {
				return CloudServicesAutoPopulator.class;
			}

			@Override
			protected boolean shouldGenerateId() {
				return true;
			}
		});
	}
}
