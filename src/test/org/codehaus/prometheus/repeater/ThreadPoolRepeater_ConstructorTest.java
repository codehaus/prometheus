/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.util.StandardThreadFactory;
import org.codehaus.prometheus.lendablereference.LendableReference;
import org.codehaus.prometheus.lendablereference.RelaxedLendableReference;
import org.codehaus.prometheus.lendablereference.StrictLendableReference;

import java.util.concurrent.ThreadFactory;

public class ThreadPoolRepeater_ConstructorTest extends ThreadPoolRepeater_AbstractTest {

    public void test_int() {
        try {
            new ThreadPoolRepeater(-1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }

        int poolsize = 10;
        repeater = new ThreadPoolRepeater(poolsize);

        assertEquals(RepeaterServiceState.Unstarted, repeater.getState());
        assertEquals(poolsize, repeater.getDesiredPoolSize());
        assertEquals(0,repeater.getActualPoolSize());
        assertEquals(0, repeater.getActualPoolSize());
        assertHasDefaultLendableReference(repeater);
        assertHasDefaultThreadFactory(repeater);
        assertIsStrict(true);
        assertHasRepeatable(null);
    }

    public void assertHasDefaultThreadFactory(ThreadPoolRepeater repeater) {
        //assertNotNull(repeater.getThreadPool());
        //assertNotNull(repeater.getThreadPool().get);
        //assertTrue(repeater.getThreadFactory() instanceof StandardThreadFactory);
    }

    public void test_Runnable_int() {
        try {
            new ThreadPoolRepeater(new DummyRepeatable(), -1);
            fail();
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }

        DummyRepeatable task = new DummyRepeatable();
        int poolsize = 10;
        repeater = new ThreadPoolRepeater(task, poolsize);

        assertIsUnstarted();
        assertDesiredPoolSize(poolsize);
        assertActualPoolSize(0);
        assertHasRepeatable(task);
        assertIsStrict(true);
        assertHasDefaultLendableReference(repeater);
    }

    public void test_boolean_Repeatable_int_ThreadFactory() {
        //try {
         //   new ThreadPoolRepeater(true, new DummyRepeatable(), -1, new StandardThreadFactory());
         //   fail();
        //} catch (IllegalArgumentException ex) {
        //    assertTrue(true);
        //}

        //try {
         //   new ThreadPoolRepeater(true, new DummyRepeatable(), 1, null);
        //    fail();
        //} catch (NullPointerException ex) {
        //    assertTrue(true);
        //}

        boolean strict = true;
        DummyRepeatable task = new DummyRepeatable();
        int poolsize = 10;
        ThreadFactory threadFactory = new StandardThreadFactory();
        construct(strict, task, poolsize, threadFactory);

        strict = false;
        task = null;
        poolsize = 20;
        threadFactory = new StandardThreadFactory();
        construct(strict, task, poolsize, threadFactory);
    }

    private void construct(boolean strict, DummyRepeatable task, int poolsize, ThreadFactory threadFactory) {
        //repeater = new ThreadPoolRepeater(strict, task, poolsize, threadFactory);

        //assertIsUnstarted();
        //assertDesiredPoolSize(poolsize);
        //assertActualPoolSize(0);
        //assertHasRepeatable(task);
        //assertSame(threadFactory, repeater.getThreadFactory());
        //assertIsStrict(strict);
    }

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


    private void assertHasDefaultLendableReference(ThreadPoolRepeater repeater) {
        LendableReference ref = repeater.getLendableRef();
        assertNotNull(ref);
        assertTrue(ref instanceof StrictLendableReference);
    }

    public void test_ThreadFactory_LendableRefeference_Runnable_int() {
        //todo
        //try {
        //    new ThreadPoolRepeater(null,new RelaxedLendableReference(), null, 1);
        //    fail("NullPointerException expected");
        //} catch (NullPointerException foundThrowable) {
        //    assertTrue(true);
        //       }
//
        //      try {
        //        new ThreadPoolRepeater(new StandardThreadFactory(), null, null, 1);
        //      fail("NullPointerException expected");
        //  } catch (NullPointerException foundThrowable) {
        //      assertTrue(true);
        //  }
//
        //    try {
        //          new ThreadPoolRepeater(new StandardThreadFactory(), new StrictLendableReference(), null, -1);
        //      fail("IllegalArgumentException expected");
        //} catch (IllegalArgumentException foundThrowable) {
        //     assertTrue(true);
        //}

        //ThreadFactory factory = new StandardThreadFactory();
        //LendableReference lendableRef = new StrictLendableReference();
        //int poolsize = 100;
        //Lock lock = new ReentrantLock();
        //repeater = new ThreadPoolRepeater(factory, lock,lendableRef, poolsize);

        //assertIsUnstarted();
        //assertDesiredPoolSize(poolsize);
        //assertActualPoolSize(0);
        //assertSame(lendableRef, repeater.getLendableRef());
    }
}