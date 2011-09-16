package org.cloudfoundry.runtime.service.document;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.net.UnknownHostException;

import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.util.ReflectionUtils;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Simplified access to creating MongoDB service objects.
 *
 * @author Ramnivas Laddad
 * @author Thomas Risberg
 *
 */
public class MongoServiceCreator extends AbstractServiceCreator<MongoDbFactory, MongoServiceInfo> {

	private MongoConfiguration mongoConfiguration = new MongoConfiguration();

	public MongoServiceCreator(CloudEnvironment cloudEnvironment) {
		super(cloudEnvironment, MongoServiceInfo.class);
	}

	public MongoConfiguration getMongoConfiguration() {
		return mongoConfiguration;
	}

	public MongoDbFactory createService(MongoServiceInfo serviceInfo) {
		try {
			MongoOptions mongoOptionsToUse = getMongoOptions();
			Mongo mongo = null;
			if (mongoOptionsToUse != null) {
				ServerAddress serverAddress = new ServerAddress(serviceInfo.getHost(), serviceInfo.getPort());
				mongo = new Mongo(serverAddress, mongoOptionsToUse);
			}
			else {
				mongo = new Mongo(serviceInfo.getHost(), serviceInfo.getPort());
			}
			UserCredentials credentials = new UserCredentials(serviceInfo.getUserName(), serviceInfo.getPassword());
			return new SimpleMongoDbFactory(mongo, serviceInfo.getDatabase(), credentials);
		} catch (UnknownHostException e) {
			throw new CloudServiceException(e);
		} catch (MongoException e) {
			throw new CloudServiceException(e);
		}
	}

	private MongoOptions getMongoOptions() {
		MongoOptions mongoOptions = null;
		BeanWrapper source = new BeanWrapperImpl(mongoConfiguration);
		for (PropertyDescriptor pd : source.getPropertyDescriptors()) {
			String property = pd.getName();
			if (source.isReadableProperty(property) && source.getPropertyValue(property) != null) {
				Field field = ReflectionUtils.findField(MongoOptions.class, property);
				if (field != null) {
					if (mongoOptions == null) {
						mongoOptions = new MongoOptions();
					}
					ReflectionUtils.setField(field, mongoOptions, source.getPropertyValue(property));
				}
			}
		}
		return mongoOptions;
	}
}
