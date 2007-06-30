/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.channels;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * todo:
 * RuntimeException on all methods
 *
 * @author Peter Veentjer.
 */
public interface InputChannel<I> {

    /**
     * @return
     * @throws InterruptedException
     */
    I take() throws InterruptedException;

    /**
     * @param timeout
     * @param unit
     * @return the taken item (never null)
     * @throws TimeoutException     if a timeout occurred
     * @throws InterruptedException
     * @throws NullPointerException if unit is null.
     */
    I poll(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;

    /**
     * @return
     */
    I poll();

    /**
     * @return
     */
    I peek();
}
