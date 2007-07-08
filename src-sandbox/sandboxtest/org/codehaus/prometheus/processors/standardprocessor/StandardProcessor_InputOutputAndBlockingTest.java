package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.TestPipedProcess;
import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

/**
 * Unittests input/output related functionality of the {@link StandardProcessor}.
 *
 * @author Peter Veentjer.
 */
public class StandardProcessor_InputOutputAndBlockingTest extends StandardProcessor_AbstractTest {

    public void testNullInput() {
        //todo
    }

    public void testNullOutput() {
        //todo
    }

    public void testInterruptedWhileWaiting() {
        //todo
    }

    public void testSomeWaitingNeeded() {
        Object item = new Object();
        TestPipedProcess process = new TestPipedProcess(item);
        newProcessor(process);

        //spawned_start the processing, this call should block because no work is available.
        ProcessThread processThread = scheduleProcess();
        giveOthersAChance();
        processThread.assertIsStarted();

        //now place an item and make sure that the process completes
        spawned_assertPut(item);
        joinAll(processThread);
        processThread.assertSuccess(true);

        //now do a take and make sure it succeeds.
        spawned_assertTake(item);
        process.assertSuccess(item);
    }

    public void testNoWaitingNeeded() {
        Object item = new Object();
        TestPipedProcess process = new TestPipedProcess(item);
        newProcessor(process);

        spawned_assertPut(item);

        spawned_assertOnceAndReturnTrue();

        spawned_assertTake(item);
        process.assertSuccess(item);
    }

    public void testPutOnOutputIsInterrupted() {
        //todo
    }

    public void testPutOnOutputThrowsException() {
        //todo
    }
}
