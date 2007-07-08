/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

/**
 * A {@link CountingRunnable} that throws a RuntimeException when it is executed. If it is executed
 * multiple times, the same exception will be thrown each time.
 *
 * @author Peter Veentjer.
 */
public final class ThrowingRunnable extends CountingRunnable{

    private final RuntimeException exception;

    /**
     * Creates a ThrowingRunnable that throws a RuntimeException.
     */
    public ThrowingRunnable(){
        this(new RuntimeException());
    }

    /**
     * Creates a ThrowingRunnable with the given RuntimeException.
     *
     * @param exception the RuntimeException this ThrowingRunnable throws.
     * @throws NullPointerException if exception is null.
     */
    public ThrowingRunnable(RuntimeException exception){
        if(exception == null)throw new NullPointerException();
        this.exception = exception;
    }

    /**
     * Returns the exception that this ThrowingRunnable throws.
     *
     * @return the exception this ThrowingRunnable throws.
     */
    public RuntimeException getException(){
        return exception;
    }

    @Override
    public void runInternal() {
        throw exception;
    }

}
