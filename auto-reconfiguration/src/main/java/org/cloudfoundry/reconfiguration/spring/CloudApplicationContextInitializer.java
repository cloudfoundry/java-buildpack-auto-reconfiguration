package org.cloudfoundry.reconfiguration.spring;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cloudfoundry.reconfiguration.util.CloudFactoryUtil;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * Cloud Foundry {@link ApplicationContextInitializer}.
 *
 * <p>The CloudApplicationContextInitializer will do two things:
 * <ol>
 * <li>active the "<code>cloud</code>" profile
 * <li>add a {@link PropertySource} featuring Cloud Foundry specific properties
 * </ol>
 *
 * <p>Properties provided by the {@link PropertySource} are automatically
 * available via Spring's property placeholder.  These two features make
 * configuring Spring applications to use Cloud Foundry services even easier.
 *
 * <p>This class implements {@link Ordered} with a default value of
 * <code>0</code>.  Other {@link ApplicationContextInitializer} can define a
 * higher order to ensure this classes properties are defined first.
 *
 * <p>Note: Spring 3.1 (or later) is required
 *
 * @see ConfigurableEnvironment
 * @see CloudEnvironment
 *
 * @author Scott Andrews
 */
public final class CloudApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

	private static final Logger logger = Logger.getLogger(CloudApplicationContextInitializer.class.getName());

	private static final int DEFAULT_ORDER = 0;

	private ConfigurableEnvironment springEnvironment;
	private Cloud cloud;

	@Override
	public final void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            CloudFactory cloudFactory = CloudFactoryUtil.getOrCreateCloudFactory(applicationContext, logger);
            cloud = cloudFactory.getCloud();
			logger.info("Initializing Spring Environment for Cloud Foundry");
			springEnvironment = applicationContext.getEnvironment();
			addPropertySource(buildPropertySource());
			addActiveProfile("cloud");
        } catch (CloudException ex) {
            logger.info("Not running on Cloud Foundry, skipping initialization");
            return;
		} catch (Throwable t) {
			// be safe
			logger.log(Level.SEVERE, "Unexpected exception on initialization: " + t.getMessage(), t);
		}
	}

	@Override
	public int getOrder() {
		return DEFAULT_ORDER;
	}

	private EnumerablePropertySource<?> buildPropertySource() {
		Properties properties = cloud.getCloudProperties();
		EnumerablePropertySource<?> source = new PropertiesPropertySource("cloud", properties);
		return source;
	}

	private void addActiveProfile(String profile) {
		logger.log(Level.FINE, "Activating profile '" + profile + "'");
		Set<String> profiles = new LinkedHashSet<String>();
		profiles.addAll(Arrays.asList(springEnvironment.getActiveProfiles()));
		profiles.add(profile);
		String[] profilesArr = new String[profiles.size()];
		profiles.toArray(profilesArr);
		springEnvironment.setActiveProfiles(profilesArr);
	}

	private void addPropertySource(EnumerablePropertySource<?> source) {
		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, "Adding property source '" + source.getName() + "'");
			for (String name : source.getPropertyNames()) {
			    logger.log(Level.FINE, name + " = " + source.getProperty(name));
			}
			logger.log(Level.FINE, "End '" + source.getName() + "' properties");
		}
		springEnvironment.getPropertySources().addLast(source);
	}
}
