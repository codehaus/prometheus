package org.codehaus.prometheus.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A non-blocking lock.
 *
 * @author Peter Veentjer
 */
public class OptimisticLock {

    private final AtomicBoolean l = new AtomicBoolean(false);

    public boolean release() {
        return l.compareAndSet(true, false);
    }

    public boolean acquire() {
        return l.compareAndSet(false, true);
    }
}
