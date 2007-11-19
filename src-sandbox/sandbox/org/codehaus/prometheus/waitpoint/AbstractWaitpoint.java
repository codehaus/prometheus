/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import org.codehaus.prometheus.uninterruptiblesection.TimedUninterruptibleSection;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The AbstractWaitpoint contains some default implementations for functions that can be derived
 * from other functions.
 *
 * @author Peter Veentjer.
 */
public abstract class AbstractWaitpoint implements Waitpoint {

    public boolean tryPass() {
        try {
            TimedUninterruptibleSection section = new TimedUninterruptibleSection() {
                protected Object interruptibleSection(long timeoutNs)
                        throws InterruptedException, TimeoutException {
                    tryPass(timeoutNs, TimeUnit.NANOSECONDS);
                    return null;
                }
            };
            section.tryExecute();
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
