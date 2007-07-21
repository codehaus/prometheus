package org.codehaus.prometheus.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
* A Latch is a single shot waiting mechanism. Once it has been opened, it can't be closed again.
 * Threads can wait on the open state of the Latch. If the Latch already is open, the continue.
 * If the Latch isn't open, the call waits until the Latch opens (or until a timeout occurs or
 * the thread is interrupted). When the Latch already is open, subsequent open attempts are 
 * ignored.
 * </p>
 * <p>
 * Doug Lea his library also contained a
 * <a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/Latch.java">Latch"> but
 * it never made it in the java.util.concurrent package.
 * </p>
 *
 * @author Peter Veentjer.
 */
public interface Latch {

    /**
     * Returns <tt>true</tt> if this Latch is open, <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if this Latch is open, <tt>false</tt> otherwise.
     */
    boolean isOpen();

    /**
     * Opens the Latch. If the Latch already is open, nothing happens.
     */
    void open();

    /**
     * Awaits for this Latch to open. If the Latch already is open, nothing happens.
     *
     * This method allows an InterruptedException to be thrown, but it is up to the
     * implementation to decide if this is done.
     *
     * @throws InterruptedException if the calling thread is interrupted.
     */
    void await() throws InterruptedException;

    /**
     * Awaits for this Latch to open. If the Latch already is open, nothing happens.
     *
     * This method allows an InterruptedException to be thrown, but it is up to the
     * implementation to decide if this is done. If
     *
     * @param timeout how long to wait before giving up in units of <tt>unit</tt>.
     * @param unit    a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt>
     *                parameter.
     * @throws InterruptedException if the calling thread is interrupted
     * @throws TimeoutException     if the timeout expires.
     * @throws NullPointerException if unit is null
     */
    void tryAwait(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;
}
