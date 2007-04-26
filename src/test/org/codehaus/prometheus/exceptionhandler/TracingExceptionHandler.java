package org.codehaus.prometheus.exceptionhandler;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Peter Veentjer.
 */
public class TracingExceptionHandler implements ExceptionHandler {

    private List<Exception> exceptionList = Collections.synchronizedList(new LinkedList());

    public int getCount(Class exceptionClass) {
        int count = 0;
        for (Throwable t : exceptionList) {
            if (exceptionClass.isInstance(t)) {
                count++;
            }
        }
        return count;
    }

    public int getCount() {
        return exceptionList.size();
    }

    public void assertCount(Class exceptionClass, int expectedCount) {
        TestCase.assertEquals(expectedCount, getCount(exceptionClass));
    }

    /**
     * Prints the tacktraces of all thrown exceptions.
     */
    public void printStacktraces() {
        if(exceptionList.isEmpty())
            return;

        System.out.println(String.format("==================== stacktraces: %d ==================",exceptionList.size()));
        for (Exception ex : exceptionList)
            ex.printStackTrace();
        System.out.println("================== end stacktraces ==================");
    }

    public void assertCount(int expectedCount) {
        TestCase.assertEquals(expectedCount, exceptionList.size());
    }

    public void assertNoErrors() {
        assertCount(0);
    }

    public void assertCountAndNoOthers(Class exceptionClass, int expectedCount) {
        assertCount(expectedCount);
        TestCase.assertEquals(expectedCount, getCount(exceptionClass));
    }

    public void handle(Exception ex) {
        exceptionList.add(ex);
    }
}
