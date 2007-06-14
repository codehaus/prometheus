package org.codehaus.prometheus.processors;

/**
 * Unittests the {@link org.codehaus.prometheus.processors.standardprocessor.StandardProcessor}.
 */
public class StandardProcessorTest extends StandardProcessor_AbstractTest {

    public void testConstructor_Object_InputChannel_OutputChannel() {

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

    public void testProcessHasNoMatchingReceive() {
        Integer arg = 10;

        TestProcess process = new TestProcess() {
            public void receive(String s) {
                fail();
            }
        };
        newProcessor(process);

        spawnedPut(arg);
        spawnedOnce(true);
        spawnedTake(arg);
        process.assertNotCalled();
    }

    public void testArgProcessReturnsVoid() {
        final Integer arg = 10;

        TestProcess process = new TestProcess() {
            public void receive(Integer i) {
                assertSame(arg, i);
                called = true;
            }
        };
        newProcessor(process);

        spawnedPut(arg);
        spawnedOnce(true);
        spawnedTake(arg);
        process.assertCalled();
    }

    public void testNoArgProcessReturnsValue() {
        final Integer value = 10;

        TestProcess process = new TestProcess() {
            public Integer receive() {
                called = true;
                return value;
            }
        };
        newSourceProcessor(process);

        spawnedOnce(true);
        spawnedTake(value);
        process.assertCalled();
    }

    public void testReceiveReturnsNull() {
        final Integer arg = 10;

        TestProcess process = new TestProcess() {
            public Object receive(Integer i) {
                assertSame(arg, i);
                called = true;
                return null;
            }
        };
        newProcessor(process);

        spawnedPut(arg);
        spawnedOnce(true);

        //todo: zou een take nu niet moeten mislukken?
        spawnedTake(arg);
        process.assertCalled();
    }

    public void testPutOnOutputIsInterrupted() {
        //todo
    }

    public void testPutOnOutputThrowsException() {
        //todo
    }

    public void testReceiveThrowsUncheckedException() {
        final Integer arg = 10;
        final RuntimeException ex = new RuntimeException() {
        };

        TestProcess process = new TestProcess() {
            public Object receive(Integer i) {
                assertSame(arg, i);
                called = true;
                throw ex;
            }
        };
        newProcessor(process);

        spawnedPut(arg);
        spawnedOnceThrowsException(ex);
        process.assertCalled();
    }

    public void testReceiveThrowsCheckedException() {
        //todo
    }
}
