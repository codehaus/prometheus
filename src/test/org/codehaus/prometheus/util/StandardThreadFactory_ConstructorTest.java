/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import junit.framework.TestCase;

public class StandardThreadFactory_ConstructorTest extends TestCase {

    public void testNoArg() {
        StandardThreadFactory factory = new StandardThreadFactory();
        assertEquals(Thread.NORM_PRIORITY, factory.getPriority());
        assertFalse(factory.isProducingDaemons());
    }

    public void test_String() {
        //null for threadgroupname is allowed
        new StandardThreadFactory(null);

        String name = "somename";
        StandardThreadFactory factory = new StandardThreadFactory(name);
        assertEquals(Thread.NORM_PRIORITY, factory.getPriority());
        assertNotNull(factory.getThreadGroup());
        assertEquals(name, factory.getThreadGroup().getName());
        assertEquals(Thread.currentThread().getThreadGroup(), factory.getThreadGroup().getParent());
        assertEquals(Thread.MAX_PRIORITY, factory.getThreadGroup().getMaxPriority());
        assertFalse(factory.isProducingDaemons());
    }

    public void test_int_String() {
        try{
            new StandardThreadFactory(Thread.MIN_PRIORITY-1,"foo");
            fail();
        }catch(IllegalArgumentException ex){            
        }

        try{
            new StandardThreadFactory(Thread.MAX_PRIORITY+1,"foo");
            fail();
        }catch(IllegalArgumentException ex){
        }

        //null for threadgroupname is allowed
        new StandardThreadFactory(Thread.MIN_PRIORITY,(String)null);

        String name = "somename";
        int priority = Thread.NORM_PRIORITY;
        StandardThreadFactory factory = new StandardThreadFactory(priority,name);
        assertEquals(priority, factory.getPriority());
        assertNotNull(factory.getThreadGroup());
        assertEquals(name, factory.getThreadGroup().getName());
        assertEquals(Thread.currentThread().getThreadGroup(), factory.getThreadGroup().getParent());
        assertEquals(Thread.MAX_PRIORITY, factory.getThreadGroup().getMaxPriority());
        assertFalse(factory.isProducingDaemons());
    }

    public void test_int_ThreadGroup() {
        try {
            new StandardThreadFactory(Thread.MAX_PRIORITY, (ThreadGroup) null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        try {
            new StandardThreadFactory(Thread.MAX_PRIORITY + 1, new ThreadGroup("somename"));
            fail("NullPointerException expected");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }

        //higher priority than threadgroup allows.
        try {
            ThreadGroup threadGroup = new ThreadGroup("somename");
            threadGroup.setMaxPriority(Thread.MIN_PRIORITY);
            new StandardThreadFactory(Thread.MIN_PRIORITY + 1, threadGroup);
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }

        ThreadGroup group = new ThreadGroup("somename");
        int priority = Thread.MAX_PRIORITY;
        StandardThreadFactory factory = new StandardThreadFactory(priority, group);
        assertSame(group, factory.getThreadGroup());
        assertEquals(priority, factory.getPriority());
        assertFalse(factory.isProducingDaemons());
    }
}
