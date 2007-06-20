package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.TestPipedProcess;

/**
 * Unittests input/output related functionality of the {@link StandardProcessor}.
 *
 * @author Peter Veentjer.
 */
public class StandardProcessor_InputOutputAndBlockingTest extends StandardProcessor_AbstractTest {

    public void testNullInput(){
        //todo
    }

    public void testNullOutput(){
        //todo
    }

    public void testInterruptedWhileWaiting() {
        //todo
    }

    public void testSomeWaitingNeeded() {
        Object item = new Object();
        TestPipedProcess process = new TestPipedProcess(item);
        newProcessor(process);

        //start the processing, this call should block because no work is available.
        ProcessThread processThread = scheduleProcess();
        giveOthersAChance();
        processThread.assertIsStarted();

        //now place an item and make sure that the process completes
        spawnedPut(item);
        joinAll(processThread);
        processThread.assertSuccess(true);

        //now do a take and make sure it succeeds.
        spawnedTake(item);
        process.assertSuccess(item);
    }

    public void testNoWaitingNeeded() {
        Object item = new Object();
        TestPipedProcess process = new TestPipedProcess(item);
        newProcessor(process);

        spawnedPut(item);

        spawnedOnce(true);

        spawnedTake(item);
        process.assertSuccess(item);
    }

    public void testPutOnOutputIsInterrupted() {
        //todo
    }

    public void testPutOnOutputThrowsException() {
        //todo
    }
}
