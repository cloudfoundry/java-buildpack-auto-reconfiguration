package org.cloudfoundry.runtime.service.config.xml;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Element;

import java.util.Map;

public abstract class AbstractCloudParserTest {
	protected CloudMongoDbFactoryParser cloudParser;
	protected GenericApplicationContext appContext;

	protected void loadContext(String fileName, Class relativeLocation, GenericApplicationContext context,
							   final BeanDefinitionParser parser) {
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
		reader.setNamespaceHandlerResolver(new NamespaceHandlerResolver() {
			@Override
			public NamespaceHandler resolve(String namespaceUri) {
				return new NamespaceHandlerSupport() {
					@Override
					public BeanDefinition parse(Element element, ParserContext parserContext) {
						return parser.parse(element, parserContext);
					}
					@Override
					public void init() {
						registerBeanDefinitionParser("mongo-db-factory", parser);
					}
				};
			}
		});
		ClassPathResource resource = new ClassPathResource(fileName, relativeLocation);
		reader.loadBeanDefinitions(resource);
	}

	public static class MapEnvironmentAccessor extends CloudEnvironment.EnvironmentAccessor {

		Map<String, String> env;

		public MapEnvironmentAccessor(Map<String, String> env) {
			this.env = env;
		}

		public String getValue(String key) {
			return env.get(key);
		}
	}
}
