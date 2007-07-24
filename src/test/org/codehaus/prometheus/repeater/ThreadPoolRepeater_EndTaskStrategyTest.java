package org.codehaus.prometheus.repeater;

import static org.codehaus.prometheus.testsupport.TestUtil.sleepMs;

/**
 * Unittests {@link EndTaskStrategy}.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_EndTaskStrategyTest extends ThreadPoolRepeater_AbstractTest{

    public void testRepeatableReturnsTrue() throws InterruptedException {
        int poolsize = 5;
        newRunningRepeater(poolsize);

        Repeatable repeatable = new DummyRepeatable(true);
        repeater.repeat(repeatable);

        sleepMs(DELAY_MEDIUM_MS);

        assertHasRepeatable(repeatable);
        assertIsRunning();
        assertDesiredPoolSize(poolsize);
        assertActualPoolSize(poolsize);
    }

    public void testRepeatableReturnsFalse() throws InterruptedException {
        int poolsize = 5;
        newRunningRepeater(poolsize);

        Repeatable repeatable = new DummyRepeatable(false);
        repeater.repeat(repeatable);

        sleepMs(DELAY_MEDIUM_MS);

        //todo: check if the task has not been executed more times than poolsize
        assertHasRepeatable(null);
        assertIsRunning();
        assertDesiredPoolSize(poolsize);
        assertActualPoolSize(poolsize);
    }
}
