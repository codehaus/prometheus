package org.codehaus.prometheus.util;

import junit.framework.TestCase;
import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.PrintStreamExceptionHandler;

import static java.util.Arrays.asList;
import java.util.List;

public abstract class RunnableChain_AbstractTest extends TestCase {

    protected RunnableChain runnableChain;
    protected TracingExceptionHandler exceptionHandler;

    public void newUnbreakableChain(Runnable... tasks){
        newRunnableChain(true, tasks);
    }

    public void newBreakableChain(Runnable... tasks){
        newRunnableChain(false,tasks);
    }

    public void newRunnableChain(boolean unbreakable, Runnable... tasks){
        exceptionHandler = new TracingExceptionHandler();
        runnableChain = new RunnableChain(tasks);
        runnableChain.setUnbreakable(unbreakable);
        runnableChain.setExceptionHandler(exceptionHandler);
    }

    public void assertExceptionHandler(ExceptionHandler expected) {
        assertSame(expected, runnableChain.getExceptionHandler());
    }

    public void assertHasDefaultExceptionHandler(){
        assertSame(PrintStreamExceptionHandler.SYS_OUT,runnableChain.getExceptionHandler());
    }

    public void assertIsBreakable() {
        assertFalse(runnableChain.isUnbreakable());
    }

    public void assertIsUnbreakable() {
        assertTrue(runnableChain.isUnbreakable());
    }

    public void assertChain(Runnable... chain) {
        List<Runnable> expectedList = asList(chain);
        assertEquals(expectedList, runnableChain.getChain());
    }
}
