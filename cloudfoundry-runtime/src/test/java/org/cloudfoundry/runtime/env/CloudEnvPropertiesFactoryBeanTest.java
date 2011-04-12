package org.cloudfoundry.runtime.env;

import static org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper.getServicesPayload;
import static org.cloudfoundry.runtime.service.CloudEnvironmentTestHelper.getRedisServicePayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.cloudfoundry.runtime.env.CloudEnvironment.EnvironmentAccessor;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link CloudEnvironmentPropertiesFactoryBean}.
 * 
 * @author Costin Leau
 */
public class CloudEnvPropertiesFactoryBeanTest {

	EnvironmentAccessor envAcc = Mockito.mock(EnvironmentAccessor.class);

	@Test
	public void testBasicProperties() throws Exception {

		CloudEnvironmentPropertiesFactoryBean fb = new CloudEnvironmentPropertiesFactoryBean();
		CloudEnvironment env = new CloudEnvironment();
		env.setCloudEnvironment(envAcc);
		fb.setCloudEnv(env);

		when(envAcc.getValue("VCAP_SERVICES")).thenReturn(
				getServicesPayload(null,
								   new String[]{getRedisServicePayload("myredis", "10.20.30.40", 50, "pass", "r1")},
								   null,
								   null));
		Properties props = fb.getObject();
		assertEquals(5, props.size());
		assertTrue(props.containsKey("myredis.hostname"));
	}
}
