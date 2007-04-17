/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.channels;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Peter Veentjer.
 */
public interface OutputChannel<O>{

    /**
     *
     * @param item
     * @throws InterruptedException
     * @throws NullPointerException if item is null.
     */
    void put(O item)throws InterruptedException;

    /**
     *
     * @param item
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws NullPointerException if item or unit is null.
     */
    long offer(O item, long timeout, TimeUnit unit)throws InterruptedException, TimeoutException;
}
