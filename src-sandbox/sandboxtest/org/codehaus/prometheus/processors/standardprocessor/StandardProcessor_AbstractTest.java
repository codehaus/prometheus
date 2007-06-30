package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.channels.StandardBufferedChannel;
import org.codehaus.prometheus.processors.Processor_AbstractTest;
import org.codehaus.prometheus.testsupport.TestThread;
import static org.codehaus.prometheus.testsupport.TestUtil.randomInt;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class StandardProcessor_AbstractTest extends Processor_AbstractTest {

    protected volatile StandardProcessor standardProcessor;
    protected volatile StandardBufferedChannel inputChannel;
    protected volatile StandardBufferedChannel outputChannel;

    public List<Integer> generateRandomNumberList(int count) {
        final List<Integer> itemList = new LinkedList<Integer>();
        for (int k = 0; k < count; k++)
            itemList.add(randomInt());
        return itemList;
    }


    public void newProcessor(Object... process) {
        newProcessor(Integer.MAX_VALUE, Integer.MAX_VALUE, process);
    }

    public void newSourceProcessor(Object process) {
        newProcessor(-1, Integer.MAX_VALUE, process);
    }

    public void newSinkProcessor(Object process) {
        newProcessor(Integer.MAX_VALUE, -1, process);
    }

    public void newProcessor(int inputCapacity, int outputCapacity, Object[] processes) {
        inputChannel = inputCapacity < 0 ? null : new StandardBufferedChannel(inputCapacity);
        outputChannel = outputCapacity < 0 ? null : new StandardBufferedChannel(outputCapacity);
        standardProcessor = new StandardProcessor(inputChannel, processes, outputChannel);
    }

    public void newProcessor(int inputCapacity, int outputCapacity, Object process) {
        newProcessor(inputCapacity, outputCapacity, new Object[]{process});
    }

    public ProcessThread scheduleProcess() {
        ProcessThread t = new ProcessThread();
        t.start();
        return t;
    }

    public void spawned_assertOnceAndReturnTrue(int count){
        for(int k=0;k<count;k++)
            spawned_assertOnceAndReturnTrue();
    }

    public void spawned_assertOnceAndReturnTrue() {
        spawned_assertOnce(true);
    }

    public void spawned_assertOnceAndReturnFalse() {
        spawned_assertOnce(false);
    }

    public void spawned_assertOnce(boolean expectedResult) {
        ProcessThread processThread = scheduleProcess();
        joinAll(processThread);
        processThread.assertSuccess(expectedResult);
    }

    public void spawned_assertOnceThrowsException(Throwable ex) {
        ProcessThread processThread = scheduleProcess();
        joinAll(processThread);
        processThread.assertIsTerminatedWithThrowing(ex);
    }

    public PutThread schedulePut(Object item) {
        PutThread t = new PutThread(item);
        t.start();
        return t;
    }

    public void spawned_assertPut(Object item) {
        PutThread t = schedulePut(item);
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public TakeThread scheduleTake() {
        TakeThread t = new TakeThread();
        t.start();
        return t;
    }

    public void spawned_assertTake(Object expectedItem) {
        TakeThread t = scheduleTake();
        joinAll(t);
        t.assertSuccess(expectedItem);
    }

    public void spawned_assertTakeNotPossible() {
        TimedTakeThread t = new TimedTakeThread();
        t.start();
        joinAll(t);
        t.assertIsTimedOut();
    }

    class ProcessThread extends TestThread {
        private boolean result;

        @Override
        protected void runInternal() throws Exception {
            result = standardProcessor.once();
        }

        public void assertSuccess(boolean expectedResult) {
            assertIsTerminatedNormally();
            assertEquals(expectedResult, result);
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

    class TimedTakeThread extends TestThread {
        protected void runInternal() throws InterruptedException, TimeoutException {
            outputChannel.poll(DELAY_SMALL_MS, TimeUnit.MILLISECONDS);
        }
    }
}
