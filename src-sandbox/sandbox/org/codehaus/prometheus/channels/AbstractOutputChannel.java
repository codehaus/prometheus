/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.channels;

import java.util.concurrent.TimeoutException;

public abstract class AbstractOutputChannel<E> implements OutputChannel<E> {

    public void savePut(E item) throws InterruptedException, TimeoutException {
        throw new RuntimeException();
    }
}
