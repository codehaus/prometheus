/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import static junit.framework.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A CountingCallable is a Callable that maintains a count and
 * increases it everytime it is runWork. When the call is executed, it
 * returns null.
 *
 * @author Peter Veentjer.
 */
///CLOVER:OFF
public class CountingCallable<E> implements Callable<E> {

    private final AtomicInteger count = new AtomicInteger();
    private final E result;

    public CountingCallable(){
        this(null);
    }

    public CountingCallable(E result){
        this.result = result;
    }

    public E call() throws Exception {
        count.incrementAndGet();
        return result;
    }

    public int getCount(){
        return count.intValue();
    }

    public void assertNotExecuted(){
        assertEquals(0,count.intValue());
    }

    public void assertExecutedOnce() {
        assertEquals(1,count.intValue());
    }
}
