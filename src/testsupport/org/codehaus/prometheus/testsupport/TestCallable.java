/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import java.util.concurrent.Callable;

/**
 *
 * @author Peter Veentjer.
 */
public class TestCallable<E> extends RunSupport implements Callable<E> {

    private final E result;
    private final Exception throwingException;

    public TestCallable(E result) {
        this.result = result;
        this.throwingException = null;
    }

    public TestCallable(Exception throwingException){
        if(throwingException == null)throw new NullPointerException();
        this.throwingException = throwingException;
        this.result = null;
    }

    public E getResult() {
        return result;
    }

    public E call() throws Exception {
        if(throwingException != null)
            throw throwingException;

        return result;
    }
}
