/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import static org.codehaus.prometheus.testsupport.TestUtil.sleepRandomMs;
import org.codehaus.prometheus.testsupport.TimedRepeatingThreadFactory;
import org.codehaus.prometheus.uninterruptiblesection.UninterruptibleSection;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * A stresstest for the DefaultAwaitableReference.
 *
 * @author Peer Veentjer.
 */
public class DefaultAwaitableReference_StressTest {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TheTest.class);
        return suite;
    }

    public static class TheTest extends TestCase {
        private final ThreadFactory factory = new TimedRepeatingThreadFactory(5, TimeUnit.SECONDS);

        public TheTest(String fixture) {
            super(fixture);
        }

        public void testMain() throws InterruptedException {
            DefaultAwaitableReference<Long> awaitableRef = new DefaultAwaitableReference<Long>();
            List<Thread> list = new LinkedList<Thread>();
            list.addAll(createTakers(awaitableRef));
            list.addAll(createPutters(awaitableRef));
            list.addAll(createSpuriousWakeupThreads(awaitableRef));

            join(list);
        }

        private void join(List<Thread> list) throws InterruptedException {
            for (Thread t : list)
                t.join();
        }

        private List<Thread> createSpuriousWakeupThreads(final DefaultAwaitableReference ref) {
            List<Thread> list = new LinkedList<Thread>();
            for (int k = 0; k < 1; k++) {
                Thread thread = factory.newThread(
                        new Runnable() {

                            public void run() {
                                sleepRandomMs(100);
                                ref.getMainLock().lock();
                                try {
                                    ref.getReferenceAvailableCondition().signalAll();
                                } finally {
                                    ref.getMainLock().unlock();
                                }
                            }
                        }
                );

                list.add(thread);
                thread.start();
            }
            return list;
        }

        private List<Thread> createPutters(final AwaitableReference<Long> awaitableRef) {
            List<Thread> list = new LinkedList<Thread>();
            for (int k = 0; k < 20; k++) {
                Thread thread = factory.newThread(
                        new Runnable() {
                            public void run() {
                                sleepRandomMs(100);
                                UninterruptibleSection section = new UninterruptibleSection() {

                                    protected Object originalsection() throws InterruptedException {
                                        awaitableRef.put(randomLong());
                                        return null;
                                    }
                                };
                                section.execute();
                            }
                        }
                );
                list.add(thread);
                thread.start();
            }
            return list;
        }

        private List<Thread> createTakers(final AwaitableReference<Long> awaitableRef) {
            List<Thread> threadList = new LinkedList<Thread>();
            for (int k = 0; k < 20; k++) {
                Thread thread = factory.newThread(
                        new Runnable() {
                            public void run() {
                                try {
                                    awaitableRef.take();
                                    sleepRandomMs(100);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }
                );

                threadList.add(thread);
                thread.start();
            }
            return threadList;
        }
    }

    public static Long randomLong() {
        Long l = Math.abs(new Random().nextLong());
        return l % 10 == 0 ? null : l;
    }
}
