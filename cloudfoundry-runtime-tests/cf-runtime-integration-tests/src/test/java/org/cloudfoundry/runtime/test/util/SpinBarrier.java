package org.cloudfoundry.runtime.test.util;

/**
 * Utility that continuously evaluates a specified condition at specified retry
 * intervals, until either the condition is true or the specified timeout is
 * reached. This utility can be used, for example, by test classes that need to
 * verify the completion of some asynchronous task.
 *
 * @author Jennifer Hickey
 *
 */
public class SpinBarrier {
	private long timeout = 120000l;
	private long retryInterval = 250l;
	private final SpinBarrierCondition condition;

	/**
	 * Constructor that uses the default timeout and retry interval
	 *
	 * @param condition
	 *            The condition to evaluate
	 */
	public SpinBarrier(SpinBarrierCondition condition) {
		this.condition = condition;
	}

	/**
	 *
	 * @param timeout
	 *            The amount of time, in milliseconds, to continue evaluating
	 *            the condition if it has not been met
	 * @param retryInterval
	 *            The amount of time to wait between evaluations of the
	 *            condition
	 * @param condition
	 *            The condition to evaluate
	 */
	public SpinBarrier(long timeout, long retryInterval, SpinBarrierCondition condition) {
		this.timeout = timeout;
		this.retryInterval = retryInterval;
		this.condition = condition;
	}

	/**
	 * Evaluates a specified condition at specified retry intervals, until
	 * either the condition is true or the specified timeout is reached
	 *
	 * @return true if the condition evaluated to true before the timeout was
	 *         reached
	 */
	public boolean waitFor() {
		final long startTime = System.currentTimeMillis();
		boolean conditionMet = false;
		while (!(conditionMet) && (System.currentTimeMillis() < (startTime + timeout))) {
			conditionMet = condition.evaluate();
			if (conditionMet) {
				continue;
			}
			try {
				Thread.sleep(retryInterval);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		return conditionMet;
	}
}
