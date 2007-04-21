/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.codehaus.prometheus.testsupport.SleepingRunnable;

import java.util.Random;

public class ThreadPoolRepeaterStressTest {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(SetPoolSizeStessTest.class);
        suite.addTestSuite(ShutdownStressTest.class);
        suite.addTestSuite(RepeatStressTest.class);
        return suite;
    }


    //goal of this test is to stress the tryRepeat functions
    public static class RepeatStressTest extends TestCase {
        public void testDummy(){}
    }

    //goal of this test is to see if errors occur when shutting down a repeater.
    //a lot of threads are going to call methods on the repeater. Some
    //are going to start/pause others are going to shutdown.
    public static class ShutdownStressTest extends ThreadPoolRepeater_AbstractTest {

        public void testDummy(){}
    }

    //this tests changes the poolsize from the same threads, so no prometheus calls.
    public static class SetPoolSizeStessTest extends ThreadPoolRepeater_AbstractTest {
        private Random random = new Random();

        public void test() {
            newRunningStrictRepeater(new RepeatableRunnable(new SleepingRunnable(100)));

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
