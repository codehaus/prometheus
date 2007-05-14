package org.codehaus.prometheus.references;

import junit.framework.TestSuite;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.TestUtil;

import java.util.List;
import java.util.LinkedList;

public class RelaxedLendableReference_StressTest {

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
        private volatile RelaxedLendableReference<Integer> lendableReference;
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

        @Override
        public void setUp() throws Exception {
            super.setUp();
            lendableReference = new RelaxedLendableReference<Integer>();
            threads = new LinkedList<TestThread>();
        }

        public void runTest() {
            startUsers();
            startPutters();

            TestThread[] threads = (TestThread[]) this.threads.toArray(new TestThread[this.threads.size()]);
            joinAll(100 * DELAY_LONG_MS, threads);

            assertIsTerminatedWithoutThrowing(threads);
        }

        private void assertIsTerminatedWithoutThrowing(TestThread... threads) {
            for (TestThread t : threads)
                t.assertIsTerminatedWithoutThrowing();
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

            public void runInternal() {
                for (int k = 0; k < repeatCountPutters; k++) {
                    lendableReference.put(TestUtil.randomInt(100000));
                    TestUtil.sleepRandomMs(10);
                    TestUtil.someCalculation(TestUtil.randomInt(100000));
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
                        TestUtil.sleepRandomMs(10);
                        TestUtil.someCalculation(TestUtil.randomInt(100000));
                    } finally {
                        //todo: add 
                        lendableReference.takeback(ref);
                    }
                }
            }
        }
    }
}
