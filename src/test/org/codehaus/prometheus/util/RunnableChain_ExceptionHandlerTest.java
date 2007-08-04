/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.Log4JExceptionHandler;

public class RunnableChain_ExceptionHandlerTest extends RunnableChain_AbstractTest{

    public void testSet_nullValue(){
        runnableChain = new RunnableChain();
        ExceptionHandler old = runnableChain.getExceptionHandler();

        try{
            runnableChain.setExceptionHandler(null);
            fail();
        }catch(NullPointerException e){
            assertExceptionHandler(old);
        }
    }

    public void testSet_success(){
        runnableChain = new RunnableChain();
        Log4JExceptionHandler newhandler = new Log4JExceptionHandler();
        runnableChain.setExceptionHandler(newhandler);
        assertExceptionHandler(newhandler);
    }
}
