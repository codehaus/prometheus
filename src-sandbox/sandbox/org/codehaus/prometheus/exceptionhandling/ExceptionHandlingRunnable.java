package org.codehaus.prometheus.exceptionhandling;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;

/**
 * A Runnable that is able to deal with exceptions.
 * usage:
 * subclassing or
 * <p/>
 * idea: let certain exceptions fall through. maybe they are needed on a lower level.
 *
 * @author Peter Veentjer.
 */
public final class ExceptionHandlingRunnable implements Runnable, ExceptionHandler {

    private final ExceptionHandler handler;
    private final Runnable task;

    public ExceptionHandlingRunnable() {
        handler = null;
        task = null;
    }

    public ExceptionHandlingRunnable(ExceptionHandler handler, Runnable task) {
        if (handler == null || task == null) throw new NullPointerException();
        this.handler = handler;
        this.task = task;
    }


    public ExceptionHandler getHandler() {
        return handler;
    }

    public Runnable getTask() {
        return task;
    }

    public void run() {
        try {
            protectedRun();
        } catch (Exception ex) {
            handle(ex);
        }
    }

    public void protectedRun() throws Exception {
        if (task == null) throw new IllegalStateException();
        task.run();
    }

    public void handle(Exception ex) {
        if (handler == null) throw new IllegalStateException();
        handler.handle(ex);
    }
}
