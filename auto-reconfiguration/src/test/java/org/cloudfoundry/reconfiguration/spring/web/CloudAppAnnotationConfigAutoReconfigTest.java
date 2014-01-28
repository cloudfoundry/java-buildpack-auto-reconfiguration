package org.cloudfoundry.reconfiguration.spring.web;

import org.cloudfoundry.reconfiguration.AbstractCloudConfigurerTest;
import org.cloudfoundry.reconfiguration.CloudAutoStagingBeanFactoryPostProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cloud.CloudConnector;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.test.CloudTestUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Unit test of the {@link org.cloudfoundry.reconfiguration.spring.web.CloudAppAnnotationConfigAutoReconfig} class
 *
 * @author Thomas Risberg
 *
 */
public class CloudAppAnnotationConfigAutoReconfigTest extends AbstractCloudConfigurerTest {

	@Test
	public void cloudApplicationAutoReconfig() {

		ApplicationContext ctx =
				getTestAnnotationConfigApplicationContext("org.cloudfoundry.reconfiguration.spring.web");
		Assert.assertTrue(ctx.getBean("__appCloudJpaPostgreSQLReplacementProperties") != null);
		Assert.assertTrue(ctx.getBean("__appCloudJpaMySQLReplacementProperties") != null);
		Assert.assertTrue(ctx.getBean("__appCloudHibernatePostgreSQLReplacementProperties") != null);
		Assert.assertTrue(ctx.getBean("__appCloudHibernateMySQLReplacementProperties") != null);
		Assert.assertTrue(ctx.getBean(CloudAutoStagingBeanFactoryPostProcessor.class) != null);
	}

    protected ApplicationContext getTestAnnotationConfigApplicationContext(String packageName, ServiceInfo... serviceInfos) {
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
}
