package org.codehaus.prometheus.util;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.PrintStreamExceptionHandler;

import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.List;

/**
 * A RunnableChain is a composition of other Runnables. It is handy if you have
 * a sequence of Runnable's to be run.
 * <p/>
 * The unbreakable nature of the chain only applies to Exceptions. Other forms of
 * Throwables like Errors are not caught and could break unbreakable chain. This behavior
 * is in sync with the standard approach for dealing with non exceptions.
 * <p/>
 * When an exception occurrs and the chain is unbreakable, the errorhandler is called
 * and then the exception is dropt. The execution of an unbreakable chain will always
 * succeed. If a chain is breakable, the exceptionhandler is not used and exceptions
 * are propagated up the callstack.
 *
 * @author Peter Veentjer.
 */
public class RunnableChain implements Runnable {
    private final List<Runnable> chain;
    private volatile boolean unbreakable = false;
    private volatile ExceptionHandler exceptionHandler = PrintStreamExceptionHandler.SYS_OUT;

    /**
     * Creates a new RunnableChain that is not unbreakable with the given chain of
     * Runnables.
     *
     * @param chain an array of runnables 
     */
    public RunnableChain(Runnable... chain) {
        this(asList(chain));
    }

    /**
     * Creates a new RunnableChain that is not unbreakable with the given list of
     * Runnables.
     *
     * @param chain a List containing the chain of runnables.
     */
    public RunnableChain(List<Runnable> chain) {
        if (chain == null) throw new NullPointerException();
        this.chain = Collections.unmodifiableList(chain);
    }

    /**
     * Checks if the chain is unbreakable.
     *
     * @return true if the chain is unbreakable, false otherwise.
     */
    public boolean isUnbreakable() {
        return unbreakable;
    }

    /**
     * Sets the unbreakable property of thie RunnableChain.
     *
     * @param unbreakable if the chain is unbreakable or not
     */
    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    /**
     * Returns the ExceptionHandled used to deal with exceptions.
     *
     * @return the ExceptionHandler used to deal with exceptions. The returned value
     *         will never be null.
     */
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * Sets the ExceptionHandler.
     *
     * @param exceptionHandler the new ExceptionHandler.
     * @throws NullPointerException if exceptionHandler is null.
     */
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        if (exceptionHandler == null) throw new NullPointerException();
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Returns the tasks this RunnableChain executes.
     *
     * @return an immutable list containing the tasks.
     */
    public List<Runnable> getChain() {
        return chain;
    }

    public void run() {
        if (unbreakable) {
            for (Runnable task : chain) {
                try {
                    task.run();
                } catch (Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }
        } else {
            for (Runnable task : chain)
                task.run();
        }
    }
}
