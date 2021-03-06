/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

/**
 * A RuntimeException thrown when attempting to take back an invalid reference to a
 * {@link LendableReference}.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public class IllegalTakebackException extends RuntimeException {

    public IllegalTakebackException(String msg) {
        super(msg);
    }
}
