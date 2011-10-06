/*
 * Copyright (c) 2009-2011 VMware, Inc.
 */
package org.cloudfoundry.reconfiguration;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 *
 * A {@link Configurer} reconfigures certain beans in a specified
 * {@link DefaultListableBeanFactory}
 *
 * @author Jennifer Hickey
 *
 */
public interface Configurer {

	/**
	 * Reconfigures beans in the specified {@link DefaultListableBeanFactory}
	 *
	 * @param beanFactory
	 *            The {@link DefaultListableBeanFactory} containing beans which
	 *            may need to be configured
	 * @return true if any beans were reconfigured
	 */
	boolean configure(DefaultListableBeanFactory beanFactory);
}
