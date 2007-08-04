/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.exceptionhandler;

import static junit.framework.Assert.assertEquals;

import static java.util.Collections.synchronizedList;
import java.util.LinkedList;
import java.util.List;

/**
 * An {@link ExceptionHandler} that keeps track of all thrown exceptions. 
 *
 * @author Peter Veentjer.
 */
///CLOVER:OFF
public class TracingExceptionHandler implements ExceptionHandler {

    private List<Exception> exceptionList = synchronizedList(new LinkedList());

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
        assertEquals(expectedCount, getCount(exceptionClass));
    }

    /**
     * Prints the tacktraces of all thrown exceptions.
     */
    public void printStacktraces() {
        if (exceptionList.isEmpty())
            return;

        System.out.println(String.format("==================== stacktraces: %d ==================", exceptionList.size()));
        for (Exception ex : exceptionList)
            ex.printStackTrace();
        System.out.println("================== end stacktraces ==================");
    }

    public void assertCount(int expectedCount) {
        assertEquals(expectedCount, exceptionList.size());
    }

    public void assertNoErrors() {
        assertCount(0);
    }

    public void assertErrorCountAndNoOthers(Class exceptionClass, int expectedCount) {
        assertCount(expectedCount);
        assertEquals(expectedCount, getCount(exceptionClass));
    }

    public void handle(Exception ex) {
        exceptionList.add(ex);
    }
}
