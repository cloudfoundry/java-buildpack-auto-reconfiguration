package org.cloudfoundry.reconfiguration;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudConnector;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.test.CloudTestUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Base class for configurer tests.
 * 
 * Offers creating application context with auto-reconfig added in (simulating 
 * the work buildpacks do.
 *
 * @author Ramnivas Laddad
 *
 */
abstract public class AbstractCloudConfigurerTest {
    private static final String MOCK_CLOUD_BEAN_NAME = "mockCloud";
    
    protected ApplicationContext getTestApplicationContext(String fileName, ServiceInfo... serviceInfos) {
        final CloudConnector stubCloudConnector = CloudTestUtil.getTestCloudConnector(serviceInfos);
        
        String expandedFileName = getClass().getPackage().getName().replaceAll("\\.", "/") + "/" + fileName;
        return new ClassPathXmlApplicationContext(new String[]{
                expandedFileName, 
                "META-INF/cloud/cloudfoundry-auto-reconfiguration-context.xml"}) {
            @Override
            protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                CloudFactory cloudFactory = new CloudFactory();
                cloudFactory.registerCloudConnector(stubCloudConnector);
                getBeanFactory().registerSingleton(MOCK_CLOUD_BEAN_NAME, cloudFactory);
                super.prepareBeanFactory(beanFactory);
            }
        };
    }
    
    protected GenericApplicationContext getTestAnnotationConfigApplicationContext(String packageName, ServiceInfo... serviceInfos) {
        final CloudConnector stubCloudConnector = CloudTestUtil.getTestCloudConnector(serviceInfos);

        return new AnnotationConfigApplicationContext(packageName) {
            @Override
            protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                CloudFactory cloudFactory = new CloudFactory();
                cloudFactory.registerCloudConnector(stubCloudConnector);
                getBeanFactory().registerSingleton(MOCK_CLOUD_BEAN_NAME, cloudFactory);
                super.prepareBeanFactory(beanFactory);
            }
        };
    }    
    
    protected Pair<Cloud, DefaultListableBeanFactory> getCloudAndBeanFactory(String fileName, ServiceInfo... serviceInfos) {
        ApplicationContext context = getTestApplicationContext(fileName, serviceInfos);
        CloudFactory cloudFactory = context.getBean(CloudFactory.class);
        
        return Pair.of(cloudFactory.getCloud(), (DefaultListableBeanFactory)context.getAutowireCapableBeanFactory());
    }
}
