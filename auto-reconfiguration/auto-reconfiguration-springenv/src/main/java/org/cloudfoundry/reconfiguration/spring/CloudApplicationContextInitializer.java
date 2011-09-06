package org.cloudfoundry.reconfiguration.spring;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.runtime.env.CloudEnvironment;
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

	// use JCL for logging.  Spring uses the JCL APIs, so we know they will be available
	private static final Log logger = LogFactory.getLog(CloudApplicationContextInitializer.class);

	private static final int DEFAULT_ORDER = 0;

	private ConfigurableEnvironment springEnvironment;
	private CloudEnvironment cloudFoundryEnvironment;

	public CloudApplicationContextInitializer() {
		cloudFoundryEnvironment = new CloudEnvironment();
	}

	@Override
	public final void initialize(ConfigurableApplicationContext applicationContext) {
		if (!cloudFoundryEnvironment.isCloudFoundry()) {
			logger.info("Not running on Cloud Foundry, skipping initialization");
			return;
		}
		try {
			logger.info("Initializing Spring Environment for Cloud Foundry");
			springEnvironment = applicationContext.getEnvironment();
			addPropertySource(buildPropertySource());
			addActiveProfile("cloud");
		} catch (Throwable t) {
			// be safe
			logger.error("Unexpected exception on initialization: " + t.getMessage(), t);
		}
	}

	@Override
	public int getOrder() {
		return DEFAULT_ORDER;
	}

	private EnumerablePropertySource<?> buildPropertySource() {
		Properties properties = cloudFoundryEnvironment.getCloudProperties();
		EnumerablePropertySource<?> source = new PropertiesPropertySource("cloud", properties);
		return source;
	}

	private void addActiveProfile(String profile) {
		logger.trace("Activating profile '" + profile + "'");
		Set<String> profiles = new LinkedHashSet<String>();
		profiles.addAll(Arrays.asList(springEnvironment.getActiveProfiles()));
		profiles.add(profile);
		String[] profilesArr = new String[profiles.size()];
		profiles.toArray(profilesArr);
		springEnvironment.setActiveProfiles(profilesArr);
	}

	private void addPropertySource(EnumerablePropertySource<?> source) {
		if (logger.isTraceEnabled()) {
			logger.trace("Adding property source '" + source.getName() + "'");
			for (String name : source.getPropertyNames()) {
				logger.trace(name + " = " + source.getProperty(name));
			}
			logger.trace("End '" + source.getName() + "' properties");
		}
		springEnvironment.getPropertySources().addLast(source);
	}

	void setCloudFoundryEnvironment(CloudEnvironment cloudFoundryEnvironment) {
		this.cloudFoundryEnvironment = cloudFoundryEnvironment;
	}

}
