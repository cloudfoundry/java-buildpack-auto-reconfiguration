package org.cloudfoundry.reconfiguration;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Base class for auto-reconfig tests that instantiate an application context
 * and need to mock the {@link CloudEnvironment} bean
 *
 * @author Jennifer Hickey
 *
 */
abstract public class CloudEnvironmentMockingTest {

	@Mock
	protected CloudEnvironment mockEnvironment;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	protected ClassPathXmlApplicationContext getTestApplicationContext(String fileName) {
		return new ClassPathXmlApplicationContext(new String[] {
				getClass().getPackage().getName().replaceAll("\\.", "/") + "/" + fileName,
				"META-INF/cloud/cloudfoundry-auto-reconfiguration-context.xml" }) {
			@Override
			protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
				getBeanFactory().registerSingleton("test-cloudEnvironment", mockEnvironment);
				super.prepareBeanFactory(beanFactory);
			}
		};
	}
}
