/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.sleepMs;
import org.codehaus.prometheus.testsupport.Delays;

/**
 * Unittests {@link EndTaskPolicy}.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_EndTaskPolicyTest extends ThreadPoolRepeater_AbstractTest{

    public void testRepeatableReturnsTrue() throws InterruptedException {
        int poolsize = 5;
        newRunningRepeater(poolsize);

        Repeatable repeatable = new DummyRepeatable(true);
        repeater.repeat(repeatable);

        sleepMs(Delays.MEDIUM_MS);

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

        sleepMs(Delays.MEDIUM_MS);

        //todo: check if the task has not been executed more times than poolsize
        assertHasRepeatable(null);
        assertIsRunning();
        assertDesiredPoolSize(poolsize);
        assertActualPoolSize(poolsize);
    }
}
