/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import junit.framework.TestCase;

public class StandardThreadFactory_SetPriorityTest extends TestCase {

    public void testIllegalPriority() {
        try {
            new StandardThreadFactory().setPriority(Thread.MAX_PRIORITY + 1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }

        try {
            new StandardThreadFactory().setPriority(Thread.MIN_PRIORITY - 1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
    }

    public void testIllegalPriorityForThroudGroup() {
        int priority = Thread.MIN_PRIORITY;
        ThreadGroup group = new ThreadGroup("");
        group.setMaxPriority(Thread.MIN_PRIORITY);
        StandardThreadFactory factory = new StandardThreadFactory(priority, group);

        try {
            factory.setPriority(Thread.MIN_PRIORITY + 1);
            fail("IllegalArgumentExeption expected");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
    }

    public void testSuccess() {
        int priority = Thread.MIN_PRIORITY;
        ThreadGroup group = new ThreadGroup("");
        group.setMaxPriority(Thread.MAX_PRIORITY);
        StandardThreadFactory factory = new StandardThreadFactory(priority, group);

        factory.setPriority(Thread.MAX_PRIORITY);
        assertEquals(Thread.MAX_PRIORITY, factory.getPriority());
    }
}
