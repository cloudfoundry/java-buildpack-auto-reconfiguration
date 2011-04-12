package org.cloudfoundry.runtime.service.document;

import java.net.UnknownHostException;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Simplified access to creating MongoDB service objects.
 * 
 * @author Ramnivas Laddad
 *
 */
public class MongoServiceCreator extends AbstractServiceCreator<Mongo, MongoServiceInfo> {
	public MongoServiceCreator(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, MongoServiceInfo.class);
	}
	
	public Mongo createService(MongoServiceInfo serviceInfo) {
		try {
			return new Mongo(serviceInfo.getHost(), serviceInfo.getPort());
		} catch (UnknownHostException e) {
			throw new CloudServiceException(e);
		} catch (MongoException e) {
			throw new CloudServiceException(e);
		}
	}
}
