/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import java.util.concurrent.Callable;

/**
 * A dummy Callable that doesn't do anything and returns null when it is called. Useful
 * when a Callable is required.
 *
 * @author Peter Veentjer.
 */
///CLOVER:OFF
public class DummyCallable<E> implements Callable<E> {

    public E call() throws Exception {
        return null;
    }
}
