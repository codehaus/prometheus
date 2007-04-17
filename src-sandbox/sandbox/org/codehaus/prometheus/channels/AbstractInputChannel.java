/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.channels;

import org.codehaus.prometheus.util.TimeoutThreadLocal;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 */
public abstract class AbstractInputChannel<E> implements InputChannel<E>{

    public E saveTake() throws InterruptedException, TimeoutException {
        long systemTimeNs = System.nanoTime();
        long timeoutNs = TimeoutThreadLocal.get();
        E item = poll(timeoutNs, TimeUnit.NANOSECONDS);
        long endTimeNs = System.nanoTime();
        long elapsedNs = endTimeNs-systemTimeNs;
        long remainingNs = timeoutNs - elapsedNs;
        TimeoutThreadLocal.set(remainingNs);
        return item;
    }
}
