package org.cloudfoundry.reconfiguration.data.document;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.reconfiguration.AbstractCloudConfigurerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.service.common.MongoServiceInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.test.util.ReflectionTestUtils;

import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

/**
 * Test of {@link MongoConfigurer}.
 *
 * @author Ramnivas Laddad
 *
 */
public class MongoConfigurerTest extends AbstractCloudConfigurerTest {

	@Test
	public void replacesUserDbFactory() throws UnknownHostException, MongoException {
	    MongoServiceInfo serviceInfo = new MongoServiceInfo("my-mongo", "1.2.3.4", 1234, "myuser", "mypass", "mydb");
        ApplicationContext context = getTestApplicationContext("test-mongo-good-context.xml", serviceInfo);
        SimpleMongoDbFactory replacedMongoDbFactory = (SimpleMongoDbFactory) context.getBean("myMongo", MongoDbFactory.class);
        assertMongoProps(serviceInfo.getHost(), serviceInfo.getPort(), serviceInfo.getUserName(), 
                serviceInfo.getPassword(), serviceInfo.getDatabase(), replacedMongoDbFactory);
	}
	
    @Test
    public void leavesOriginalInPlaceIfMultipleBeansDetected() {
        MongoServiceInfo serviceInfo = new MongoServiceInfo("my-mongo", "1.2.3.4", 1234, "myuser", "mypass", "mydb");
        ApplicationContext context = getTestApplicationContext("test-multiple-mongo-context.xml", serviceInfo);

        assertMongoProps("127.0.0.1", 1234, "localuser", "localpass", "localdb", 
                         (SimpleMongoDbFactory) context.getBean("myMongo1", MongoDbFactory.class));
        assertMongoProps("127.0.0.1", 4321, "localuser", "localpass", "localdb", 
                (SimpleMongoDbFactory) context.getBean("myMongo2", MongoDbFactory.class));
    }

    @Test
    public void leavesOriginalInPlaceIfMultipleServicesDetected() {
        MongoServiceInfo serviceInfo1 = new MongoServiceInfo("my-mongo1", "1.2.3.4", 1234, "myuser", "mypass", "mydb");
        MongoServiceInfo serviceInfo2 = new MongoServiceInfo("my-mongo2", "1.2.3.4", 4321, "myuser", "mypass", "mydb");
        ApplicationContext context = getTestApplicationContext("test-mongo-good-context.xml", serviceInfo1, serviceInfo2);        
        assertMongoProps("127.0.0.1", 1234, "localuser", "localpass", "localdb", 
                (SimpleMongoDbFactory) context.getBean("myMongo", MongoDbFactory.class));
    }

    @Test
    public void leavesOriginalInPlaceIfNoServicesDetected() {
        ApplicationContext context = getTestApplicationContext("test-mongo-good-context.xml");
        assertMongoProps("127.0.0.1", 1234, "localuser", "localpass", "localdb", 
                (SimpleMongoDbFactory) context.getBean("myMongo", MongoDbFactory.class));
    }
	
	private void assertMongoProps(String host, int port, String username, String password, String db, 
	        SimpleMongoDbFactory dbFactory) {
        Map<String, Object> dbFactorProps = getMongoDbFactoryProperties(dbFactory);
        
        Assert.assertEquals(host, dbFactorProps.get("host"));
        Assert.assertEquals(port, dbFactorProps.get("port"));
        Assert.assertEquals(username, dbFactorProps.get("username"));
        Assert.assertEquals(password, dbFactorProps.get("password"));
        Assert.assertEquals(db, dbFactorProps.get("databaseName"));
	}
    
	private Map<String,Object> getMongoDbFactoryProperties(SimpleMongoDbFactory dbFactory) {
	    Map<String, Object> props = new HashMap<String, Object>();
	    
	    for (String propName : new String[]{"username", "password", "databaseName"}) {
	        props.put(propName, ReflectionTestUtils.getField(dbFactory, propName));
	    }
	    Mongo mongo = (Mongo) ReflectionTestUtils.getField(dbFactory, "mongo");
	    ServerAddress address = mongo.getAddress();
	    props.put("host", address.getHost());
	    props.put("port", address.getPort());
	    
	    return props;
	}
}
