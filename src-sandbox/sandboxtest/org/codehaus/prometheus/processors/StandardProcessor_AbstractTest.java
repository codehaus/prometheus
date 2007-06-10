package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.channels.BufferedChannel;
import org.codehaus.prometheus.testsupport.TestThread;

public abstract class StandardProcessor_AbstractTest extends Processor_AbstractTest {

    protected volatile StandardProcessor standardProcessor;
    protected volatile BufferedChannel inputChannel;
    protected volatile BufferedChannel outputChannel;

    public void newProcessor(Object process) {
        newProcessor(Integer.MAX_VALUE, Integer.MAX_VALUE, process);
    }

    public void newProcessor(int inputCapacity, int outputCapacity, Object process) {
        inputChannel = new BufferedChannel(inputCapacity);
        outputChannel = new BufferedChannel(outputCapacity);
        standardProcessor = new StandardProcessor(process, inputChannel, outputChannel);
    }

    public ProcessThread scheduleProcess() {
        ProcessThread t = new ProcessThread();
        t.start();
        return t;
    }

    public void spawnedOnce(boolean expectedResult) {
        ProcessThread processThread = scheduleProcess();
        joinAll(processThread);
        processThread.assertSuccess(expectedResult);
    }

    public void spawnedOnceThrowsException(Exception ex) {
        ProcessThread processThread = scheduleProcess();
        joinAll(processThread);
        processThread.assertIsTerminatedWithThrowing(ex);
    }

    public PutThread schedulePut(Object item) {
        PutThread t = new PutThread(item);
        t.start();
        return t;
    }

    public void spawnedPut(Object item) {
        PutThread t = schedulePut(item);
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public TakeThread scheduleTake() {
        TakeThread t = new TakeThread();
        t.start();
        return t;
    }

    public void spawnedTake(Object expectedItem) {
        TakeThread t = scheduleTake();
        joinAll(t);
        t.assertSuccess(expectedItem);
    }

    class ProcessThread extends TestThread {
        private boolean result;

        @Override
        protected void runInternal() throws Exception {
            result = standardProcessor.once();
        }

        public void assertSuccess(boolean expectedResult) {
            assertIsTerminatedNormally();
            assertEquals(expectedResult, expectedResult);
        }
    }

    class PutThread extends TestThread {
        private final Object item;

        public PutThread(Object item) {
            this.item = item;
        }

        @Override
        protected void runInternal() throws InterruptedException {
            inputChannel.put(item);
        }
    }

    class TakeThread extends TestThread {
        private volatile Object item;

        protected void runInternal() throws InterruptedException {
            item = outputChannel.take();
        }

        public void assertSuccess(Object expectedItem) {
            if (expectedItem == null) throw new IllegalArgumentException();
            assertSame(expectedItem, item);
        }
    }

}
