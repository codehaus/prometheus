/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.NoOpExceptionHandler;
import org.codehaus.prometheus.concurrenttesting.TracingThreadFactory;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * Unittests the constructors of {@link StandardThreadPool}.
 *
 * @author Peter Veentjer
 */
public class StandardThreadPool_ConstructorTest extends StandardThreadPool_AbstractTest {

    //================== StandardThreadPool(int) ========================

    public void test_noArg() {
        threadpool = new StandardThreadPool();
        assertIsUnstarted();
        assertActualPoolsize(0);
        assertDesiredPoolsize(0);
        assertStandardThreadFactory();
        assertHasNullExceptionHandler();
    }

    //================== StandardThreadPool(ThreadFactory) ========================

    public void test_ThreadFactory() {
        try {
            new StandardThreadPool(null);
            fail();
        } catch (NullPointerException ex) {
        }

        TracingThreadFactory factory = new TracingThreadFactory();
        threadpool = new StandardThreadPool(factory);
        assertIsUnstarted();
        assertSame(factory, threadpool.getThreadFactory());
        assertActualPoolsize(0);
        assertDesiredPoolsize(0);
        assertHasNullExceptionHandler();
        factory.assertNoneCreated();
    }


    //======================== asserts =========================

    private void assertHasNullExceptionHandler() {
        assertTrue(threadpool.getExceptionHandler() instanceof NoOpExceptionHandler);
    }

    private void assertStandardThreadFactory() {
        ThreadFactory factory = threadpool.getThreadFactory();
        assertTrue(factory instanceof StandardThreadFactory);
        StandardThreadFactory stdFactory = (StandardThreadFactory) factory;
        assertEquals(Thread.NORM_PRIORITY, stdFactory.getPriority());
        assertFalse(stdFactory.isProducingDaemons());
    }
}
