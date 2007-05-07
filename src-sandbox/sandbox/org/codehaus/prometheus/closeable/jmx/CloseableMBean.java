/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.closeable.jmx;

/**
 * The CloseableMBean is an MBean that exposes a {@link org.codehaus.prometheus.closeable.Closeable}.
 *
 * @author Peter Veentjer.
 */
public interface CloseableMBean {

    /**
     * Returns <tt>true</tt> if the Closeable is open, <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if the Closeable is open, <tt>false</tt> otherwise.
     */
    boolean isOpen();

    /**
     * Returns <tt>true</tt> if the Closeable is closed, <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if the Closeable is closed, <tt>false</tt> otherwise.
     */
    boolean isClosed();

    /**
     * Opens the Closeable. If the Closeable already is open, this call is ignored.
     */
    void open();

    /**
     * Closes the Closeable. If the Closeable already is closed, this call is ignored.
     */
    void close();
}
