/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import java.util.concurrent.Callable;

/**
 * A {@link Callable} that always throws an Exception. Useful for testing exception handling
 * around callables.
 *
 * @author Peter Veentjer.
 *
 * todo:
 * should this class extend from CountingCallable, just like the ThrowingRunnable/CountingRunnable.
 */
public final class ThrowingCallable implements Callable {
    private final Exception ex;

    /**
     * Creates a ThrowingCallable that throws a RuntimeException.
     */
    public ThrowingCallable(){
        this(new RuntimeException());
    }

    /**
     * Creates a ThrowingCallable that throws the given Exception.
     *
     * @param ex the Exception to throw
     * @throws NullPointerException if ex is null.
     */
    public ThrowingCallable(Exception ex){
        if(ex == null)throw new NullPointerException();
        this.ex = ex;
    }

    /**
     * Returns the Exception this ThrowingCallable throws.
     *
     * @return the Exception this ThrowingCallable throws.
     */
    public Exception getEx() {
        return ex;
    }

    public Object call() throws Exception {
        throw ex;
    }
}
