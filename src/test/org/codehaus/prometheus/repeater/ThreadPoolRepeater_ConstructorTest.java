/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.references.LendableReference;
import org.codehaus.prometheus.references.RelaxedLendableReference;
import org.codehaus.prometheus.references.StrictLendableReference;
import org.codehaus.prometheus.testsupport.TracingThreadFactory;
import org.codehaus.prometheus.threadpool.StandardThreadPool;
import org.codehaus.prometheus.threadpool.ThreadPool;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.concurrent.ThreadFactory;

public class ThreadPoolRepeater_ConstructorTest extends ThreadPoolRepeater_AbstractTest {

    //================= ThreadPoolRepeater(int) ===========================

    public void test_int() {
        try {
            new ThreadPoolRepeater(-1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }

        int poolsize = 10;
        repeater = new ThreadPoolRepeater(poolsize);

        assertIsUnstarted();
        assertActualPoolSize(0);
        assertDesiredPoolSize(poolsize);
        assertHasDefaultLendableReference();
        assertHasDefaultThreadPool();
        assertIsStrict(true);
        assertHasRepeatable(null);
    }

    //================== ThreadPoolRepeater(runnable,int) ====================

    public void test_Runnable_int() {
        try {
            new ThreadPoolRepeater(new DummyRepeatable(), -1);
            fail();
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }

        test_Runnable_int(0, new DummyRepeatable());
        test_Runnable_int(0, null);
        test_Runnable_int(10, new DummyRepeatable());
        test_Runnable_int(10, null);
    }

    private void test_Runnable_int(int poolsize, DummyRepeatable task) {
        repeater = new ThreadPoolRepeater(task, poolsize);

        assertIsUnstarted();
        assertDesiredPoolSize(poolsize);
        assertActualPoolSize(0);
        assertHasRepeatable(task);
        assertIsStrict(true);
        assertHasDefaultLendableReference();
        assertHasDefaultThreadPool();
    }

    //============= ThreadPoolRepeater(boolean,Repeatable,int,ThreadFactory =========

    public void test_boolean_Repeatable_int_ThreadFactory() {
        try {
            new ThreadPoolRepeater(true, new DummyRepeatable(), -1, new StandardThreadFactory());
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            new ThreadPoolRepeater(true, new DummyRepeatable(), 1, null);
            fail();
        } catch (NullPointerException ex) {
        }

        test_boolean_Repeatable_int_ThreadFactory(true, new DummyRepeatable(), 0);
        test_boolean_Repeatable_int_ThreadFactory(false, null, 10);
    }

    private void test_boolean_Repeatable_int_ThreadFactory(boolean strict, DummyRepeatable task, int poolsize) {
        TracingThreadFactory threadFactory = new TracingThreadFactory();
        repeater = new ThreadPoolRepeater(strict, task, poolsize, threadFactory);

        assertIsUnstarted();
        assertDesiredPoolSize(poolsize);
        assertActualPoolSize(0);
        assertHasRepeatable(task);
        assertIsStrict(strict);

        assertTrue(repeater.getThreadPool() instanceof StandardThreadPool);
        StandardThreadPool threadpool = (StandardThreadPool) repeater.getThreadPool();
        assertSame(threadFactory, threadpool.getThreadFactory());
        threadFactory.assertNoThreadsCreated();
    }

    //================ ThreadPoolRepeater(ThreadPool, LendableReference)  ==========

    public void test_ThreadFactory_LendableRefeference_Runnable_int() {
        try {
            new ThreadPoolRepeater(new StandardThreadPool(), null);
            fail("NullPointerException expected");
        } catch (NullPointerException foundThrowable) {
        }

        try {
            new ThreadPoolRepeater(null, new StrictLendableReference<Repeatable>());
            fail("NullPointerException expected");
        } catch (NullPointerException foundThrowable) {
        }

        LendableReference<Repeatable> lendableRef = new StrictLendableReference<Repeatable>();
        int poolsize = 10;
        ThreadPool threadPool = new StandardThreadPool(poolsize);
        repeater = new ThreadPoolRepeater(threadPool,lendableRef);

        assertIsUnstarted();
        assertDesiredPoolSize(poolsize);
        assertActualPoolSize(0);
        assertSame(lendableRef, repeater.getLendableRef());
        assertSame(threadPool,repeater.getThreadPool());
    }

    //========================= asserts =======================

    private void assertIsStrict(boolean strict) {
        LendableReference ref = repeater.getLendableRef();
        if (ref instanceof StrictLendableReference) {
            assertTrue(strict);
        } else if (ref instanceof RelaxedLendableReference) {
            assertFalse(strict);
        } else {
            fail();
        }
    }

    private void assertHasDefaultLendableReference() {
        LendableReference ref = repeater.getLendableRef();
        assertNotNull(ref);
        assertTrue(ref instanceof StrictLendableReference);
    }


    public void assertHasDefaultThreadPool() {
        assertTrue(repeater.getThreadPool() instanceof StandardThreadPool);
        StandardThreadPool stdThreadPool = (StandardThreadPool) repeater.getThreadPool();
        assertHasDefaultThreadFactory(stdThreadPool);
    }

    private void assertHasDefaultThreadFactory(StandardThreadPool stdThreadPool) {
        ThreadFactory factory = stdThreadPool.getThreadFactory();
        assertTrue(factory instanceof StandardThreadFactory);
        StandardThreadFactory stdThreadFactory = (StandardThreadFactory) factory;
        assertEquals(Thread.NORM_PRIORITY, stdThreadFactory.getPriority());
        assertFalse(stdThreadFactory.isProducingDaemons());
    }
}