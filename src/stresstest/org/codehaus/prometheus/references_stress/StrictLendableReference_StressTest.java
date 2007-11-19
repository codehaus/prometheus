/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references_stress;

import junit.framework.TestSuite;
import org.codehaus.prometheus.concurrenttesting.ConcurrentTestCase;
import org.codehaus.prometheus.concurrenttesting.TestThread;
import org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil;
import org.codehaus.prometheus.concurrenttesting.Delays;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.*;
import org.codehaus.prometheus.references.StrictLendableReference;

import java.util.LinkedList;
import java.util.List;

/**
 * It should test that it doesn't happen that multiple references
 * are lend.
 *
 * takeback and takebackandreset should be tested
 *
 * @author Peter Veentjer.
 */
public class StrictLendableReference_StressTest {

     public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new SomeTest(1, 500, 1, 500));
        suite.addTest(new SomeTest(1, 500, 10, 500));
        suite.addTest(new SomeTest(10, 500, 1, 500));
        suite.addTest(new SomeTest(2, 500, 10, 500));
        suite.addTest(new SomeTest(20, 500, 20, 500));
        suite.addTest(new SomeTest(5, 500, 20, 500));
        suite.addTest(new SomeTest(20, 500, 50, 500));
        return suite;
    }

    public static class SomeTest extends ConcurrentTestCase {
        private volatile StrictLendableReference<Integer> lendableReference;
        private volatile List<TestThread> threads;
        private final int numberOfUsers;
        private final int numberOfPutters;
        private final int repeatCountUsers;
        private final int repeatCountPutters;

        public SomeTest(int numberOfUsers, int repeatCountUsers, int numberOfPutters, int repeatCountPutters) {
            this.numberOfUsers = numberOfUsers;
            this.repeatCountUsers = repeatCountUsers;
            this.numberOfPutters = numberOfPutters;
            this.repeatCountPutters = repeatCountPutters;
        }

        public void setUp() throws Exception {
            super.setUp();
            
            lendableReference = new StrictLendableReference<Integer>();
            threads = new LinkedList<TestThread>();
        }

        public void runTest() {
            startUsers();
            startPutters();

            TestThread[] threads = (TestThread[]) this.threads.toArray(new TestThread[this.threads.size()]);
            joinAll(100 * Delays.LONG_MS, threads);

            assertIsTerminatedWithoutThrowing(threads);
        }

        private void assertIsTerminatedWithoutThrowing(TestThread... threads) {
            for (TestThread t : threads)
                t.assertIsTerminatedNormally();
        }

        private void startPutters() {
            for (int k = 0; k < numberOfPutters; k++) {
                PutThread t = new PutThread();
                this.threads.add(t);
                t.start();
            }
        }

        private void startUsers() {
            for (int k = 0; k < numberOfUsers; k++) {
                UsingThread t = new UsingThread();
                this.threads.add(t);
                t.start();
            }
        }

        public class PutThread extends TestThread {

            public PutThread() {
                setPriority(Thread.MIN_PRIORITY);
            }

            public void runInternal() throws InterruptedException {
                for (int k = 0; k < repeatCountPutters; k++) {
                    lendableReference.put(randomInt(100000));
                    sleepRandomMs(10);
                    ConcurrentTestUtil.someCalculation(randomInt(100000));
                }
            }
        }

        public class UsingThread extends TestThread {
            public UsingThread() {
                setPriority(Thread.MIN_PRIORITY);
            }

            protected void runInternal() throws Exception {
                for (int k = 0; k < repeatCountUsers; k++) {
                    Integer ref = lendableReference.take();
                    try {
                        sleepRandomMs(10);
                        ConcurrentTestUtil.someCalculation(randomInt(100000));
                    } finally {
                        //todo: add
                        lendableReference.takeback(ref);
                    }
                }
            }
        }
    }
}
