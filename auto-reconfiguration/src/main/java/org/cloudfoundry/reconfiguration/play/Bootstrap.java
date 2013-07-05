package org.cloudfoundry.reconfiguration.play;

import play.core.server.NettyServer;

/**
 * Wrapper that starts the main Play class.
 * <p />
 * Environment initialization and auto-reconfiguration is now handled by PlayPlugin, but this class is retained
 * to reduce the impact on cloudfoundry-buildpack-java.
 *
 * @author Jennifer Hickey
 *
 */
public class Bootstrap {

	public static void main(String[] args) {
		NettyServer.main(args);
	}

}
