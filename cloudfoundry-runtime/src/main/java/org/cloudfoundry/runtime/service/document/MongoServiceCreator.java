package org.cloudfoundry.runtime.service.document;

import java.net.UnknownHostException;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.document.mongodb.MongoDbFactory;
import org.springframework.data.document.mongodb.SimpleMongoDbFactory;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Simplified access to creating MongoDB service objects.
 * 
 * @author Ramnivas Laddad
 *
 */
public class MongoServiceCreator extends AbstractServiceCreator<MongoDbFactory, MongoServiceInfo> {
	public MongoServiceCreator(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, MongoServiceInfo.class);
	}
	
	public MongoDbFactory createService(MongoServiceInfo serviceInfo) {
		try {
			UserCredentials credentials = new UserCredentials(serviceInfo.getUserName(), serviceInfo.getPassword());
			return new SimpleMongoDbFactory(new Mongo(serviceInfo.getHost(), serviceInfo.getPort()),
					                        serviceInfo.getDatabase(), credentials);
		} catch (UnknownHostException e) {
			throw new CloudServiceException(e);
		} catch (MongoException e) {
			throw new CloudServiceException(e);
		}
	}
}
