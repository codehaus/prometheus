/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.exceptionhandler;

/**
 * A dummy ExceptionHandler that doesn't do anything. It can be used as in instance if no
 * other instance is provided.
 *
 * @author Peyer Veentjer.
 * @since 0.1
 */
public class NoOpExceptionHandler implements ExceptionHandler {

    public static final NoOpExceptionHandler INSTANCE = new NoOpExceptionHandler();

    public void handle(Exception exception) {
        //do nothing.
    }
}
