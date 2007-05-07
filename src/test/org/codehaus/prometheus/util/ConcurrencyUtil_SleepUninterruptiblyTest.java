/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConcurrencyUtil_SleepUninterruptiblyTest extends ConcurrentTestCase {

    public void testArguments(){
        try{
            ConcurrencyUtil.sleepUninterruptibly(1,null);
            fail();
        }catch(NullPointerException ex){
            assertTrue(true);
        }
    }

    public void testNegativeTimeout(){
        ConcurrencyUtil.sleepUninterruptibly(-1, TimeUnit.MILLISECONDS);
    }

    //=====================================================

    public void testSleep_startInterrupted(){
        testSleep(true);
    }

    public void testSleep_startUninterrupted(){
        testSleep(false);
    }

    public void testSleep(boolean startInterrupted){
        stopwatch.start();
        SleepUninterruptiblyThread t = scheduleSleepUninterruptibly(1000,startInterrupted);
        joinAll(t);
        stopwatch.stop();

        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
        stopwatch.assertElapsedBiggerOrEqualThanMs(1000);
    }


    //=====================================================

    public void testInterruptedWhileSleeping_startInterrupted(){
        testInterruptedWhileSleeping(true);
    }

    public void testInterruptedWhileSleeping_startUninterrupted(){
        testInterruptedWhileSleeping(false);
    }

    public void testInterruptedWhileSleeping(boolean startInterrupted){
        stopwatch.start();
        SleepUninterruptiblyThread t = scheduleSleepUninterruptibly(DELAY_LONG_MS,startInterrupted);
        sleepMs(DELAY_TINY_MS);

        t.assertIsStarted();
        t.interrupt();

        joinAll(t);
        stopwatch.stop();

        t.assertIsTerminatedWithInterruptStatus(true);
        stopwatch.assertElapsedBiggerOrEqualThanMs(DELAY_LONG_MS);
    }


    //=====================================================

    public SleepUninterruptiblyThread scheduleSleepUninterruptibly(long sleepMs,boolean startInterrupted){
        SleepUninterruptiblyThread t = new SleepUninterruptiblyThread(sleepMs,TimeUnit.MILLISECONDS);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    class SleepUninterruptiblyThread extends TestThread {
        private final long sleep;
        private final TimeUnit sleepUnit;

        public SleepUninterruptiblyThread(long sleep, TimeUnit sleepUnit) {
            this.sleep = sleep;
            this.sleepUnit = sleepUnit;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            ConcurrencyUtil.sleepUninterruptibly(sleep,sleepUnit);
        }
    }
}
