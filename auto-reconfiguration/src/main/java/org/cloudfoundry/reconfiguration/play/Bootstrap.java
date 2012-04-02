package org.cloudfoundry.reconfiguration.play;

import org.cloudfoundry.runtime.env.CloudEnvironment;

import play.core.server.NettyServer;

/**
 * Wrapper that takes care of auto-reconfiguration before starting the main Play
 * class.
 *
 * @author Jennifer Hickey
 *
 */
public class Bootstrap {

	public static void main(String[] args) {
		new PlayConfigurer(new CloudEnvironment()).configure();
		NettyServer.main(args);
	}

}
