package org.cloudfoundry.runtime.service.keyvalue;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test of the {@link RedisServiceCreator}
 *
 * @author Jennifer Hickey
 *
 */
public class RedisServiceCreatorTest {

	@Mock
	private CloudEnvironment mockRuntime;

	private RedisServiceCreator serviceCreator;

	@Mock
	private RedisServiceInfo mockServiceInfo;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		serviceCreator = new MockRedisServiceCreator(mockRuntime);
	}

	@Test(expected = CloudServiceException.class)
	public void missingJedisClassCausesException() {
		serviceCreator.createService(mockServiceInfo);
	}

	private class MockRedisServiceCreator extends RedisServiceCreator {
		public MockRedisServiceCreator(CloudEnvironment cloudEnvironment) {
			super(cloudEnvironment);
		}

		@Override
		protected boolean hasClass(String name) {
			return false;
		}
	}
}
