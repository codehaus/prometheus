/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import static org.codehaus.prometheus.testsupport.TestSupport.newSleepingRunnable;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.sleepMs;
import org.codehaus.prometheus.testsupport.Delays;

/**
 * Unittests the {@link ThreadPoolRepeater#setDesiredPoolSize(int)}  method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_SetPoolSizeTest extends ThreadPoolRepeater_AbstractTest {

    public void testArguments() {
        newRunningStrictRepeater();
        int poolsize = repeater.getDesiredPoolSize();

        try {
            repeater.setDesiredPoolSize(-1);
            fail();
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }

        assertIsRunning();
        assertDesiredPoolSize(poolsize);
        assertActualPoolSize(1);
    }

    public void testWhileUnstarted() {
        newUnstartedStrictRepeater();
        int poolsize = 100;

        repeater.setDesiredPoolSize(poolsize);

        assertIsUnstarted();
        assertEquals(poolsize, repeater.getDesiredPoolSize());
        assertActualPoolSize(0);
    }

    public void testWhileStarted_poolsizeIsIncreased() {
        newRunningStrictRepeater(new RepeatableRunnable(newSleepingRunnable(Delays.TINY_MS)));

        int[] sizes = new int[]{0, 10, 1, 0, 2, 0, 20, 0};
        for (int size : sizes) {
            assertActualPoolsizeChanges(size);
        }
    }

    private void assertActualPoolsizeChanges(int poolsize) {
        int oldPoolsize = repeater.getDesiredPoolSize();

        repeater.setDesiredPoolSize(poolsize);
        assertDesiredPoolSize(poolsize);

        //give the workers enough time to terminate
        //todo: ugly
        sleepMs((poolsize + oldPoolsize) * 20 + 100);

        assertIsRunning();
        assertDesiredPoolSize(poolsize);
        assertActualPoolSize(poolsize);
    }

    public void testWhileShuttingdown() {
        newShuttingdownRepeater(Delays.SMALL_MS);
        assertSetPoolSizeThrowsIllegalStateException();
    }

    public void testWhileForcedShuttingdown(){
        newForcedShuttingdownRepeater(Delays.LONG_MS,1);
        assertSetPoolSizeThrowsIllegalStateException();
    }

    public void testWhileShutdown() {
        newShutdownRepeater();
        assertSetPoolSizeThrowsIllegalStateException();
    }

    private void assertSetPoolSizeThrowsIllegalStateException() {
        RepeaterServiceState oldState = repeater.getState();
        try {
            repeater.setDesiredPoolSize(repeater.getActualPoolSize()+1);
            fail();
        } catch (IllegalStateException ex) {
            assertTrue(true);
        }
        assertEquals(oldState, repeater.getState());
    }
}
