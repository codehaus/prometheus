/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import java.util.concurrent.Callable;

/**
 * A dummy Callable that doesn't do much except returning the given value.
 *
 * @author Peter Veentjer.
 */
///CLOVER:OFF
public class DummyCallable<E> implements Callable<E> {
    private final E item;

    public DummyCallable(){
        this(null);
    }

    public DummyCallable(E item){
        this.item = item;
    }

    public E call() throws Exception {
        return item;
    }
}
