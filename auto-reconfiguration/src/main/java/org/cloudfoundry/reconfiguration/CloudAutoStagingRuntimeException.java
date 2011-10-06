/*
 * Copyright (c) 2009-2011 VMware, Inc.
 */

package org.cloudfoundry.reconfiguration;

/**
 * Wrapper class for <code> RuntimeExceptions </code> that are raised during
 * auto staging.
 *
 * @author A.B.Srinivasan.
 *
 */
public class CloudAutoStagingRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1522676140269706233L;

    public CloudAutoStagingRuntimeException(String message) {
        super(message);
    }

    public CloudAutoStagingRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
