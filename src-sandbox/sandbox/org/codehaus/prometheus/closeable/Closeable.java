/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.closeable;

/**
 * A Closeable represents a structure that can be opened and closed. A possible usage would be to
 * open/close a {@link java.util.concurrent.BlockingQueue} so that no items can be added or removed.
 * It also could be used to open/close a {@link org.codehaus.prometheus.lendablereference.LendableReference}
 * so no references can be lend.
 * <p/>
 * todo: what about open/close failures?<p/>
 * todo: what about blocking?
 *
 * @author Peter Veentjer.
 */
public interface Closeable {

    /**
     * Checks if this DirectCloseable is open.
     *
     * @return <tt>true</tt> if this DirectCloseable is open, <tt>false</tt> otherwise.
     * @see #isClosed()
     */
    boolean isOpen();

    /**
     * Checks if this DirectCloseable is closed.
     *
     * @return <tt>true</tt> if this DirectCloseable is closed, <tt>false</tt> otherwise.
     * @see #isOpen()
     */
    boolean isClosed();

    /**
     * Opens this DirectCloseable. If it already is open, this call is ignored.
     *
     * @see #isOpen()
     * @see #close()
     */
    void open();

    /**
     * Closes this DirectCloseable. If it already is closed, this call is ignored.
     *
     * @see #isClosed()
     * @see #open()
     */
    void close();
}
