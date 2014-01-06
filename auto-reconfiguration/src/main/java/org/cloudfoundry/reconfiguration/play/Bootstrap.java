package org.cloudfoundry.reconfiguration.play;

import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;

import play.core.server.NettyServer;

/**
 * Wrapper that takes care of environment initialization and
 * auto-reconfiguration before starting the main Play class.
 *
 * @author Jennifer Hickey
 *
 */
public class Bootstrap {

	public static void main(String[] args) {
		Cloud cloud = new CloudFactory().getCloud();
		new Configurer(new AppConfiguration(cloud), new PropertySetter(cloud)).configure();
		NettyServer.main(args);
	}

}
