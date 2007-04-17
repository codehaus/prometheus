/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.SleepingRunnable;

/**
 * Unittests the {@link ThreadPoolRepeater#setPoolSize(int)} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_SetPoolSizeTest extends ThreadPoolRepeater_AbstractTest {

    public void testArguments(){
        newRunningStrictRepeater();
        int poolsize = repeater.getPoolSize();

        try{
            repeater.setPoolSize(-1);
            fail();
        }catch(IllegalArgumentException ex){
            assertTrue(true);
        }

        assertIsRunning();
        assertPoolSize(poolsize);
        assertActualPoolSize(1);
    }

    public void testUnstarted(){
        newUnstartedStrictRepeater();
        int poolsize = 100;

        repeater.setPoolSize(poolsize);

        assertIsUnstarted();
        assertEquals(poolsize,repeater.getPoolSize());
        assertActualPoolSize(0);
    }

    public void testStarted_incPoolsize(){
        newRunningStrictRepeater(new RepeatableRunnable(new SleepingRunnable(DELAY_TINY_MS)));

        int[] sizes = new int[]{0,10,1,0,2,0,20,0};
        for(int size: sizes){
            assertActualPoolsizeChanges(size);
        }        
    }

    private void assertActualPoolsizeChanges(int poolsize) {
        int oldPoolsize = repeater.getPoolSize();

        repeater.setPoolSize(poolsize);
        assertPoolSize(poolsize);

        //give the workers enough time to terminate
        sleepMs((poolsize+oldPoolsize)*20+100);

        assertIsRunning();
        assertPoolSize(poolsize);        
        assertActualPoolSize(poolsize);
    }

    public void testShuttingdown(){
        newShuttingdownRepeater(DELAY_SMALL_MS);
        assertSetPoolSizeThrowsIllegalStateException();
    }

    public void testShutdown(){
        newShutdownRepeater();
        assertSetPoolSizeThrowsIllegalStateException();
    }

    private void assertSetPoolSizeThrowsIllegalStateException() {
        RepeaterServiceState oldState = repeater.getState();
        try{
            repeater.setPoolSize(100);
            fail();
        }catch(IllegalStateException ex){
            assertTrue(true);
        }
        assertEquals(oldState,repeater.getState());
    }
}
