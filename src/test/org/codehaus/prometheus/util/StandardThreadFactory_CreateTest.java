/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import junit.framework.TestCase;

public class StandardThreadFactory_CreateTest extends TestCase {

    public void testArguments() {
        StandardThreadFactory factory = new StandardThreadFactory();

        try {
            factory.newThread(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testPriorityIsHigherThanThreadgroupAllows() {
        ThreadGroup threadGroup = new ThreadGroup("somename");
        threadGroup.setMaxPriority(Thread.MAX_PRIORITY);
        StandardThreadFactory factory = new StandardThreadFactory(Thread.MAX_PRIORITY, threadGroup);
        threadGroup.setMaxPriority(Thread.MAX_PRIORITY - 1);

        Runnable task = new Runnable() {
            public void run() {
            }
        };

        //todo: extra checks needed        
        factory.newThread(task);
    }

    public void testSuccess() {
        ThreadGroup group = new ThreadGroup("somename");
        int priority = Thread.MAX_PRIORITY;
        StandardThreadFactory factory = new StandardThreadFactory(priority, group);
        Runnable task = new Runnable() {
            public void run() {
            }
        };

        Thread t = factory.newThread(task);
        assertNotNull(t);
        assertEquals(priority, t.getPriority());
        assertSame(group, t.getThreadGroup());
        assertEquals(Thread.State.NEW, t.getState());
        assertFalse(t.isDaemon());
    }
}
