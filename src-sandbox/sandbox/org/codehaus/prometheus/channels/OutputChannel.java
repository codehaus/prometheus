/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.channels;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * RuntimeException on all methods
 *
 * @author Peter Veentjer.
 */
public interface OutputChannel<O> {

    /**
     * @param item the item to offer
     * @throws InterruptedException
     * @throws NullPointerException if item is null.
     * @throws RuntimeException if the put of the item fails.
     */
    void put(O item) throws InterruptedException;

    /**
     * @param item  the item to offer
     * @param timeout
     * @param unit
     * @return the remaining timeout (always a value equal or larger to zero).
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws NullPointerException if item or unit is null.
     * @throws RuntimeException if the offer of the item fails.
     */
    long offer(O item, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
}
