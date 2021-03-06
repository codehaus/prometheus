/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.concurrenttesting.ConcurrentTestCase;
import org.codehaus.prometheus.concurrenttesting.TestRunnable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unittests the {@link RepeatableRunnable}.
 *
 * @author Peter Veentjer.
 */
public class RepeatableRunnableTest extends ConcurrentTestCase {

    public void testConstructor_Runnable() {
        try {
            new RepeatableRunnable(null);
            fail();
        } catch (NullPointerException ex) {
        }

        TestRunnable task = new TestRunnable();
        RepeatableRunnable repeatable = new RepeatableRunnable(task);
        assertSame(task, repeatable.getRunnable());
    }

    public void testConstructor_noArg() {
        RepeatableRunnable repeatable = new RepeatableRunnable();
        assertSame(repeatable, repeatable.getRunnable());
    }

    public void testNoOverrideNoInjection() throws Exception {
        Repeatable repeatable = new RepeatableRunnable();
        try {
            repeatable.execute();
            fail();
        } catch (IllegalStateException ex) {
        }
    }

    public void testExecutionWithInjection() {
        TestRunnable task = new TestRunnable();
        RepeatableRunnable repeatable = new RepeatableRunnable(task);
        repeatable.execute();
        task.assertExecutedOnce();
    }

    public void testExecutionWithOverride() {
        final AtomicBoolean hasRun = new AtomicBoolean(false);
        RepeatableRunnable repeatable = new RepeatableRunnable() {
            public void run() {
                hasRun.set(true);
            }
        };

        repeatable.execute();
        assertTrue(hasRun.get());
    }
}
