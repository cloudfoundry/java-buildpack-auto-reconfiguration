package org.cloudfoundry.reconfiguration.util;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cloud.CloudFactory;

public class CloudFactoryUtil {
    public static CloudFactory getOrCreateCloudFactory(ConfigurableListableBeanFactory defaultListableBeanFactory, Logger logger) {
        
        // defaultListableBeanFactory.getBean(CloudEnvironment.class) will do,
        // but we go through a mechanism that will work for spring-2.5.x as well
        Map<String, CloudFactory> cloudFactoryBeans = defaultListableBeanFactory.getBeansOfType(CloudFactory.class, true, false);
        CloudFactory cloudFactory;
        if (cloudFactoryBeans.size() > 1) {
            logger.log(Level.INFO, "Multiple (" + cloudFactoryBeans.size() + ") CloudFactory beans found; zero or 1 expected");
            return null;
        } else if (cloudFactoryBeans.size() == 1) {
            cloudFactory = cloudFactoryBeans.entrySet().iterator().next().getValue();
        } else {
            cloudFactory = new CloudFactory();
            defaultListableBeanFactory.registerSingleton("__cloud_factory", cloudFactory);
        }

        return cloudFactory;
    }
}
