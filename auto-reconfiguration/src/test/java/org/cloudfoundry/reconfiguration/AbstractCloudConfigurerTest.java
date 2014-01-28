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
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
    protected static final String MOCK_CLOUD_BEAN_NAME = "mockCloud";
    
    protected ApplicationContext getTestApplicationContext(String fileName, ServiceInfo... serviceInfos) {
        final CloudConnector stubCloudConnector = CloudTestUtil.getTestCloudConnector(serviceInfos);
        
        String[] appContextFiles = null;
        if (fileName == null) {
            appContextFiles = new String[]{"META-INF/cloud/cloudfoundry-auto-reconfiguration-context.xml"};
        } else {
            String expandedFileName = getClass().getPackage().getName().replaceAll("\\.", "/") + "/" + fileName;
            appContextFiles = new String[]{expandedFileName, 
                    "META-INF/cloud/cloudfoundry-auto-reconfiguration-context.xml"};
        }
        
        return new ClassPathXmlApplicationContext(appContextFiles) {
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
        // Since we want tests to be run against Spring 2.5, we can't use getBean(Class<T>) that was introduced in Spring 3.0
        CloudFactory cloudFactory = (CloudFactory) context.getBean(MOCK_CLOUD_BEAN_NAME);
        
        return Pair.of(cloudFactory.getCloud(), (DefaultListableBeanFactory)context.getAutowireCapableBeanFactory());
    }
}
