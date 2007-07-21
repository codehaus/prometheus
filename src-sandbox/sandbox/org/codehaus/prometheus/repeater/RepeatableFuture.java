package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.util.JucLatch;
import org.codehaus.prometheus.util.Latch;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A {@link Future} for repeaters. Future 'completes' when the internal repeatable returns false.
 *
 * @author Peter Veentjer.
 */
public final class RepeatableFuture implements Repeatable, Future {
    private final Repeatable repeatable;
    private final Latch latch = new JucLatch();
    private volatile boolean cancelled = false;

    public RepeatableFuture(Repeatable repeatable) {
        if (repeatable == null) throw new NullPointerException();
        this.repeatable = repeatable;
    }

    public boolean execute() throws Exception {
        boolean success = repeatable.execute();
        if (!success)
            latch.open();
        return success;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isDone() {
        return latch.isOpen();
    }

    public Object get() throws InterruptedException, ExecutionException {
        //latch.tryAwait();
        return null;
    }

    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        latch.tryAwait(timeout, unit);
        return null;
    }
}
