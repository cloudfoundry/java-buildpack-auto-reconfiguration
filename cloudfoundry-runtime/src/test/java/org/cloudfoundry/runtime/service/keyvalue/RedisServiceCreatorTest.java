package org.cloudfoundry.runtime.service.keyvalue;

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

	private RedisServiceCreator serviceCreator;

	@Mock
	private RedisServiceInfo mockServiceInfo;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		serviceCreator = new StubRedisServiceCreator();
	}

	@Test(expected = CloudServiceException.class)
	public void missingJedisClassCausesException() {
		serviceCreator.createService(mockServiceInfo);
	}

	private class StubRedisServiceCreator extends RedisServiceCreator {
		@Override
		protected boolean hasClass(String name) {
			return false;
		}
	}
}
