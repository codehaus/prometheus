/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.uninterruptiblesection.TimedUninterruptibleSection;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An abstract implementation of the AwaitableReference that implements some methods that can
 * be derived from other methods.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public abstract class AbstractAwaitableReference<E> implements AwaitableReference<E> {

    public E tryTake() {
        try {
            TimedUninterruptibleSection<E> section = new TimedUninterruptibleSection<E>() {
                protected E originalsection(long timeoutNs)
                        throws InterruptedException, TimeoutException {
                    return tryTake(timeoutNs, TimeUnit.NANOSECONDS);
                }
            };

            return section.tryExecute();
        } catch (TimeoutException e) {
            return null;
        }
    }
}
