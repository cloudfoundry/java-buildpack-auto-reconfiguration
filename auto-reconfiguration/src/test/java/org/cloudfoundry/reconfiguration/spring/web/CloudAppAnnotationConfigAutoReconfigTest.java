package org.cloudfoundry.reconfiguration.spring.web;

import org.cloudfoundry.reconfiguration.CloudAutoStagingBeanFactoryPostProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Unit test of the {@link org.cloudfoundry.reconfiguration.spring.web.CloudAppAnnotationConfigAutoReconfig} class
 *
 * @author Thomas Risberg
 *
 */
public class CloudAppAnnotationConfigAutoReconfigTest {

	@Test
	public void cloudApplicationAutoReconfig() {

		GenericApplicationContext ctx =
				getTestAnnotationConfigApplicationContext("org.cloudfoundry.reconfiguration.spring.web");
		Assert.assertTrue(ctx.getBean("__appCloudJpaPostgreSQLReplacementProperties") != null);
		Assert.assertTrue(ctx.getBean("__appCloudJpaMySQLReplacementProperties") != null);
		Assert.assertTrue(ctx.getBean("__appCloudHibernatePostgreSQLReplacementProperties") != null);
		Assert.assertTrue(ctx.getBean("__appCloudHibernateMySQLReplacementProperties") != null);
		Assert.assertTrue(ctx.getBean(CloudAutoStagingBeanFactoryPostProcessor.class) != null);
	}

	protected GenericApplicationContext getTestAnnotationConfigApplicationContext(String packageName) {
		return new AnnotationConfigApplicationContext(packageName);
	}

}
