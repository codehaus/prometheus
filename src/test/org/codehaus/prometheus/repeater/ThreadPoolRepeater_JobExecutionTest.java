/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.CountingRunnable;
import org.codehaus.prometheus.testsupport.DetectingAndInterruptingRunnable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * All logic for task execution has to be placed in this Test. All the repeat methods tests
 * should check if the task is running (so the workers have received the event
 * there is a job available), but for more deeper problems like runtimexception
 * handling this class should be used.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_JobExecutionTest extends ThreadPoolRepeater_AbstractTest {

    public void testFalseStopsRepeater(){
        int poolsize = 10;
        newRunningRepeater(true,poolsize);

        CountingRepeatable repeatable = new CountingRepeatable(poolsize);
        tested_repeat(repeatable);

        giveOthersAChance();
        repeatable.assertRunCount(poolsize,2*poolsize);
    }

    //todo: name stinks: it also is not clear what this repeatable should do and should test
    public class CountingRepeatable implements Repeatable{

        private final AtomicInteger count = new AtomicInteger();
        private final int maxCount;

        public CountingRepeatable(int maxCount){
            this.maxCount = maxCount;
        }

        public boolean execute() {
            int count = this.count.incrementAndGet();
            return count<maxCount;
        }

        public void assertRunCount(int minRunCount, int maxRunCount){
            assertTrue(minRunCount<=count.get());
            assertTrue(count.intValue()<=maxRunCount);
        }
    }

    public void testSuccess() throws InterruptedException {
        newRunningRepeater(false,10);

        CountingRunnable task = new CountingRunnable();
        tested_repeat(task);

        sleepMs(DELAY_LONG_MS);
        task.assertExecutedMoreThanOnce();
    }

    //todo: testen dat de task herhaald blijft.

  //  public void testStrictness() {
  //      fail();
  //  }
/*
    public void testRelaxedness() {
        newRunningRepeater(false, 10);

        TestThread t = new TestThread() {
            public void runInternal() {
                for (int k = 0; k < 10; k++) {
                    //Thread.sleepMs();
                    try {
                        repeater.repeat(new SleepingRunnable(1));
                    } catch (InterruptedException e) {
                        //todo
                    }
                }
            }
        };
    }
*/
    //=============== testRunningTaskCausesRuntimeException =============================

 
    //============ unsetofinterruptstatus ======================================

    //with a strict repeater
    public void testUnsetOfInterruptStatus_strict() throws InterruptedException {
        testUnsetOfInterruptStatus(true);
    }

    //with a relaxed repeater
    public void testUnsetOfInterruptStatus_relaxed() throws InterruptedException {
        testUnsetOfInterruptStatus(false);
    }

    /**
     * If a task sets the interrupt status on a thread, this interrupt status should
     * be removed before the next task.
     */
    public void testUnsetOfInterruptStatus(boolean strict) throws InterruptedException {
        DetectingAndInterruptingRunnable detector = new DetectingAndInterruptingRunnable();

        newRunningRepeater(strict, new RepeatableRunnable(detector));

        //give the runnable some time to runWork.
        giveOthersAChance();

        //this test could fail because the interrupt status could be set from the outside.
        //But till so far I haven't had any problems. 
        detector.assertNoInterruptFound();
    }

    //========================================================

}
