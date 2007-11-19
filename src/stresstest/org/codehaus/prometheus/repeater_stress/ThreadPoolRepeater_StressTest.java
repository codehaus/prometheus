/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater_stress;

import junit.framework.TestSuite;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newSleepingRunnable;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.sleepMs;
import org.codehaus.prometheus.repeater.ThreadPoolRepeater_AbstractTest;
import org.codehaus.prometheus.repeater.RepeatableRunnable;

import java.util.Random;

public class ThreadPoolRepeater_StressTest {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(SetPoolSizeStessTest.class);
        suite.addTestSuite(ShutdownStressTest.class);
        return suite;
    }


    //goal of this test is to see if errors occur when shutting down a repeater.
    //a lot of threads are going to call methods on the repeater. Some
    //are going to spawned_start/pause others are going to shutdown.
    public static class ShutdownStressTest extends ThreadPoolRepeater_AbstractTest {

        public void testDummy() {
        }
    }

    //this tests changes the poolsize from the same threads, so no prometheus calls.
    public static class SetPoolSizeStessTest extends ThreadPoolRepeater_AbstractTest {
        private Random random = new Random();

        public void test() {
            newRunningStrictRepeater(new RepeatableRunnable(newSleepingRunnable(100)));

            for (int k = 0; k < 20; k++) {
                assertActualPoolsizeChanges(randomPoolsize());
            }
        }

        public int randomPoolsize() {
            int x = random.nextInt(10);
            if (x == 0) {
                return 0;
            } else {
                return random.nextInt(1000);
            }
        }

        private void assertActualPoolsizeChanges(int poolsize) {
            repeater.setDesiredPoolSize(poolsize);

            assertDesiredPoolSize(poolsize);

            //give the workers enough time to terminate
            sleepMs(1000);

            assertIsRunning();
            assertDesiredPoolSize(poolsize);
            assertActualPoolSize(poolsize);
        }

    }
}
