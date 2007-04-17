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
public interface InputChannel<I> {

    I take()throws InterruptedException;

    I poll(long timeout, TimeUnit unit)throws TimeoutException,InterruptedException;

    I poll();

    I peek();
}
