package org.cloudfoundry.runtime.env;

/**
 * @author Ramnivas Laddad
 */
public class CloudServiceException extends RuntimeException {
	private static final long serialVersionUID = -8609074700303153444L;

	public CloudServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public CloudServiceException(Throwable cause) {
		super(cause);
	}

	public CloudServiceException(String message) {
		super(message);
	}

}
