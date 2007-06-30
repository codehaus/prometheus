package org.codehaus.prometheus.channels;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An {@link org.codehaus.prometheus.channels.OutputChannel} that can be used as a drain.
 *
 * @author Peter Veentjer.
 */
public class NullOutputChannel<E> implements OutputChannel<E> {

    public final static NullInputChannel instance = new NullInputChannel();

    public void put(E item) throws InterruptedException {
        if (item == null) throw new NullPointerException();
    }

    public long offer(E item, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (item == null || unit == null) throw new NullPointerException();
        return unit.toNanos(timeout);
    }
}
