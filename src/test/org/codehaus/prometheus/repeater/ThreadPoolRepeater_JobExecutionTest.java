/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.CountingRunnable;
import org.codehaus.prometheus.testsupport.InterruptingAndDetectingRunnable;
import org.codehaus.prometheus.testsupport.ThrowingRunnable;

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

//    public void testExecutionSetsInterruptedStatus() {
 //       fail();
 //   }

    public void testFalseStopsRepeater(){
        int poolsize = 10;
        newRunningRepeater(true,poolsize);

        FooRepeatable repeatable = new FooRepeatable(poolsize);
        scheduleRepeat(repeatable);

        sleepMs(DELAY_MEDIUM_MS);
        repeatable.assertRunCount(poolsize,2*poolsize);
    }

    public class FooRepeatable implements Repeatable{

        private final AtomicInteger count = new AtomicInteger();
        private final int maxCount;

        public FooRepeatable(int maxCount){
            this.maxCount = maxCount;
        }

        public boolean execute() {
            int count = this.count.incrementAndGet();
            System.out.println("count: "+count);
            
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
        repeater.repeat(new RepeatableRunnable(task));

        sleepMs(DELAY_SMALL_MS);
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
    public void testRunningTaskCausesRuntimeException_strict() throws InterruptedException {
        testRunningTaskCausesRuntimeException(true);
    }

    public void testRunningTaskCausesRuntimeException_relaxed() throws InterruptedException {
        testRunningTaskCausesRuntimeException(false);
    }

    public void testRunningTaskCausesRuntimeException(boolean strict) throws InterruptedException {
        newRunningRepeater(strict);

        ThrowingRunnable task = new ThrowingRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);
        RepeatThread repeatThread = scheduleRepeat(repeatable);
        joinAll(repeatThread);
        repeatThread.assertIsTerminated();

        //give the task some time to execute 
        sleepMs(DELAY_SMALL_MS);
        task.assertExecutedOnceOrMore();
        assertHasRepeatable(repeatable);
        assertIsRunning();
    }
    //===============================

    public void testUnsetOfInterruptStatus_strict() throws InterruptedException {
        testUnsetOfInterruptStatus(true);
    }

    public void testUnsetOfInterruptStatus_relaxed() throws InterruptedException {
        testUnsetOfInterruptStatus(false);
    }

    /**
     * If a task sets the interrupt status on a thread, this interrupt status should
     * be removed before the next task.
     */
    public void testUnsetOfInterruptStatus(boolean strict) throws InterruptedException {
        InterruptingAndDetectingRunnable detector = new InterruptingAndDetectingRunnable();
        newRunningRepeater(strict, new RepeatableRunnable(detector));
        //give the runnable some time to runWork.
        sleepMs(DELAY_SMALL_MS);

        detector.assertNoInterruptFound();
    }

    //========================================================

}
