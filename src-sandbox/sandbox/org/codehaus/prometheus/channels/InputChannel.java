/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.channels;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A InputChannel is responsible for providing access for retrieving items.
 *
 * Every method in the InputChannel is allowed to throw RuntimeExceptions. Implementation of the
 * InputChannel could make use of environment (for example Javaspaces) to retrieve items from. It could
 * happen that something goes wrong while access the environment.
 *
 * @author Peter Veentjer.
 * @see OutputChannel
 * @see java.util.concurrent.BlockingQueue
 */
public interface InputChannel<I> {

    /**
     * @return the item that was taken. The value will never be null.
     * @throws InterruptedException
     * @throws RuntimeException if the take fails.
     */
    I take() throws InterruptedException;

    /**
     * @param timeout
     * @param unit
     * @return the taken item (never null)
     * @throws TimeoutException     if a timeout occurred
     * @throws InterruptedException
     * @throws NullPointerException if unit is null.
     * @throws RuntimeException if the poll fails.
     */
    I poll(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;

    /**
     * @return
     * @throws RuntimeException if the poll fails.
     */
    I poll();

    /**
     * @return
     * @throws RuntimeException if the peek fails.
     */
    I peek();
}
                                                                     