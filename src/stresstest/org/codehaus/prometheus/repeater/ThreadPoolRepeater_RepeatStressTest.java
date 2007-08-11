package org.codehaus.prometheus.repeater;

import static junit.framework.Assert.assertEquals;
import junit.framework.TestSuite;
import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.ConcurrentTestUtil;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.*;
import org.codehaus.prometheus.threadpool.StandardThreadPool;
import org.codehaus.prometheus.threadpool.ThreadPool;
import org.codehaus.prometheus.util.StandardThreadFactory;

public class ThreadPoolRepeater_RepeatStressTest {

    public static TestSuite suite(){
        TestSuite suite = new TestSuite();
        int maximumPoolsize = 100*Runtime.getRuntime().availableProcessors();
        for(int k=0;k<maximumPoolsize;k++){
            int executionCount = Integer.MAX_VALUE;
            suite.addTest(new RepeatingSameJobTest(k,executionCount, true));
            suite.addTest(new RepeatingSameJobTest(k,executionCount, false));
        }
        return suite;
    }

    public static class RepeatingSameJobTest extends ConcurrentTestCase {
        private final int poolsize;
        private final boolean strict;
        private final int executionCount;
        private volatile ThreadPoolRepeater repeater;
        private volatile TracingExceptionHandler exceptionHandler;
        private volatile Task task;

        public RepeatingSameJobTest(int poolsize, int executionCount,  boolean strict){
            super(String.format("poolsize: %d strict: %b",poolsize,strict));
            this.poolsize = poolsize;
            this.strict = strict;
            this.executionCount = executionCount;
        }

        public void setUp() throws Exception {
            super.setUp();
            
            ThreadPool pool = new StandardThreadPool(poolsize,new StandardThreadFactory(Thread.MIN_PRIORITY));
            repeater = new ThreadPoolRepeater(
                    pool,
                    ThreadPoolRepeater.createDefaultLendableReference(strict,null));
            exceptionHandler = new TracingExceptionHandler();
            repeater.setExceptionHandler(exceptionHandler);
            task = new Task(executionCount);
        }

        public void runTest() throws InterruptedException {
            repeater.start();
            repeater.repeat(task);
            sleepRandomMs(10000);

            repeater.shutdown();
            repeater.awaitShutdown();
            exceptionHandler.printStacktraces();
            exceptionHandler.assertNoErrors();
            //task.assertCount(executionCount);
        }
    }

    public static class Task implements Repeatable{
        private final int executionCount;
        private volatile long currentExecutionCount = 0;

        public Task(int executionCount){
            this.executionCount = executionCount;
        }

        public long getCurrentExecutionCount() {
            return currentExecutionCount;
        }

        public void assertCount(long expectedExecutionCount){
            assertEquals(expectedExecutionCount, currentExecutionCount);
        }

        public boolean execute() throws Exception {
            synchronized(this){
                if(currentExecutionCount == executionCount)
                    return false;

                currentExecutionCount++;
            }
            
            sleepRandomMs(50);
            ConcurrentTestUtil.someCalculation(randomInt(100000));
            Thread.yield();
            return true;
        }
    }
}
