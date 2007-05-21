/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

/**
 * A RuntimeException thrown when attempting to take back an invalid reference to a
 * {@link org.codehaus.prometheus.references.LendableReference}.
 *
 * @author Peter Veentjer.
 */
public class IllegalTakebackException extends RuntimeException{

    public IllegalTakebackException(String msg){
        super(msg);
    }
}
