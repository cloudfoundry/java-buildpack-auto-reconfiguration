package org.cloudfoundry.runtime.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Test;

/**
 * Unit test for {@link CloudPropertiesFactoryBean}.
 *
 * @author Scott Andrews
 */
public class CloudPropertiesFactoryBeanTest {

	@Test
	public void testFactory() throws Exception {
		CloudEnvironment environment = mock(CloudEnvironment.class);
		CloudPropertiesFactoryBean factory = new CloudPropertiesFactoryBean();
		factory.setCloudEnvironment(environment);
		Properties properties = new Properties();
		when(environment.getCloudProperties()).thenReturn(properties);
		assertTrue(factory.isSingleton());
		assertEquals(Properties.class, factory.getObjectType());
		assertSame(properties, factory.getObject());
	}

}
