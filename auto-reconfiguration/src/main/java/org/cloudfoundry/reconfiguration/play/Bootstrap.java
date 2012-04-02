package org.cloudfoundry.reconfiguration.play;

import org.cloudfoundry.runtime.env.CloudEnvironment;

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
		CloudEnvironment environment = new CloudEnvironment();
		new Configurer(new AppConfiguration(environment), new PropertySetter(environment)).configure();
		NettyServer.main(args);
	}

}
