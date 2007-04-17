package org.codehaus.prometheus.exceptionhandler;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Peter Veentjer.
 */
public class TracingExceptionHandler implements ExceptionHandler{

    private List<Exception> list = Collections.synchronizedList(new LinkedList());

    public int getCount(Class exceptionClass) {
        int count = 0;
        for(Throwable t: list){
            if(exceptionClass.isInstance(t)){
                count++;
            }
        }
        return count;
    }

    public int getCount(){
        return list.size();
    }

    public void assertCount(Class exceptionClass, int expectedCount){
        TestCase.assertEquals(expectedCount,getCount(exceptionClass));
    }

    public void assertCount(int expectedCount){
        TestCase.assertEquals(expectedCount,list.size());
    }

    public void assertNoErrors(){
        assertCount(0);
    }

    public void assertCountAndNoOthers(Class exceptionClass, int expectedCount){
        assertCount(expectedCount);
        TestCase.assertEquals(expectedCount,getCount(exceptionClass));
    }

    public void handle(Exception ex) {
        list.add(ex);
    }
}
