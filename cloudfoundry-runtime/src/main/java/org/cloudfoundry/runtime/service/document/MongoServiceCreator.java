package org.cloudfoundry.runtime.service.document;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.net.UnknownHostException;

import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import org.cloudfoundry.runtime.env.CloudServiceException;
import org.cloudfoundry.runtime.env.MongoServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.Mongo;
import com.mongodb.MongoException;
import org.springframework.util.ReflectionUtils;

/**
 * Simplified access to creating MongoDB service objects.
 *
 * @author Ramnivas Laddad
 * @author Thomas Risberg
 *
 */
public class MongoServiceCreator extends AbstractServiceCreator<MongoDbFactory, MongoServiceInfo> {

	private CloudMongoConfiguration cloudMongoConfiguration;

	public void setCloudMongoConfiguration(CloudMongoConfiguration cloudMongoConfiguration) {
		this.cloudMongoConfiguration = cloudMongoConfiguration;
	}

	public MongoDbFactory createService(MongoServiceInfo serviceInfo) {
		try {
			UserCredentials credentials = new UserCredentials(serviceInfo.getUserName(), serviceInfo.getPassword());
			Mongo mongo = null;
			MongoOptions mongoOptionsToUse = getMongoOptions();
			if (mongoOptionsToUse != null) {
				ServerAddress serverAddress = new ServerAddress(serviceInfo.getHost(), serviceInfo.getPort());
				mongo = new Mongo(serverAddress, mongoOptionsToUse);
			}
			else {
				mongo = new Mongo(serviceInfo.getHost(), serviceInfo.getPort());
			}
			SimpleMongoDbFactory mongoDbFactory =
					new SimpleMongoDbFactory(mongo, serviceInfo.getDatabase(), credentials);
			if (cloudMongoConfiguration != null && cloudMongoConfiguration.getWriteConcern() != null) {
				WriteConcern writeConcern = WriteConcern.valueOf(cloudMongoConfiguration.getWriteConcern());
				if (writeConcern != null) {
					mongoDbFactory.setWriteConcern(writeConcern);
				}
			}
			return mongoDbFactory;
		} catch (UnknownHostException e) {
			throw new CloudServiceException(e);
		} catch (MongoException e) {
			throw new CloudServiceException(e);
		}
	}

	private MongoOptions getMongoOptions() {
		if (cloudMongoConfiguration == null) {
			return null;
		}
		MongoOptions mongoOptions = null;
		BeanWrapper source = new BeanWrapperImpl(cloudMongoConfiguration);
		for (PropertyDescriptor pd : source.getPropertyDescriptors()) {
			String property = pd.getName();
			if (!"class".equals(property) && source.isReadableProperty(property) &&
						source.getPropertyValue(property) != null) {
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
