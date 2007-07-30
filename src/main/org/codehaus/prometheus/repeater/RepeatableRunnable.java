/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

/**
 * A {@link Repeatable} that adapts a {@link Runnable} so it can be used as a Repeatable.
 * There are two different ways to use it:
 * <ol>
 * <li>inject a Runnable by using the constructor {@link #RepeatableRunnable(Runnable)} </li>
 * <li>subclass this RepeatableRunnable and override the {@link #run()} method</li>
 * </ol>
 * The {@link #execute()} method always returns <tt>true</tt> unless a RuntimeException occurs.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public class RepeatableRunnable implements Repeatable, Runnable {

    private final Runnable runnable;

    /**
     * Creates a new RepeatableRunnable. This constructor needs to be called when subclassing
     * is used (the {@link #run} method should be overridden).
     */
    public RepeatableRunnable() {
        runnable = this;
    }

    /**
     * Creates a new RepeatableRunnable with the given task. This constructor needs to be called
     * when runnable injection is used.
     *
     * @param runnable the task to execute.
     * @throws NullPointerException if runnable is null.
     */
    public RepeatableRunnable(Runnable runnable) {
        if (runnable == null) throw new NullPointerException();
        this.runnable = runnable;
    }

    /**
     * Returns the injected Runnable. If no runnable is injected, the RepeatableRunnable is returned
     * because it also implements the Runnable interface.
     *
     * @return the injected Runnable.
     */
    public final Runnable getRunnable() {
        return runnable;
    }

    public final boolean execute() {
        runnable.run();
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if no runnable is provided, and this method is not overriden.
     */
    public void run() {
        throw new IllegalStateException("if no runnable is provided, this method should be overriden");
    }
}
