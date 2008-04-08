/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.exceptionhandler;

/**
 * A handler for exceptions.
 *
 * todo: idea to let handle method return a value? In case of void, the caller knows that
 * nothing interesting is returned. And in the other cases, the caller knows what to
 * expect
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public interface ExceptionHandler {

    /**
     * Handles the exception.
     *
     * @param exception the exception to handle. This will never be <tt>null</tt>.
     * @throws NullPointerException if ex is null, it is up the the implementation to decide
     *                              if any checking should be done.
     */
    void handle(Exception exception);
}
