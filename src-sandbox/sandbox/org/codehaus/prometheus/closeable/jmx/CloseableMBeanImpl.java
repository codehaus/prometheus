/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.closeable.jmx;

import org.codehaus.prometheus.closeable.Closeable;

/**
 * The default implementation of the {@link CloseableMBean} interface.
 *
 * @author Peter Veentjer.
 */
public class CloseableMBeanImpl implements CloseableMBean{

    private final Closeable closeable;

    /**
     * Creates a new CloseableMBeanImpl.
     *
     * @param closeable the Closeable this CloseableMBeanImpl exposes.
     * @throws NullPointerException if closeable is <tt>null</tt>.
     */
    public CloseableMBeanImpl(Closeable closeable){
        if(closeable == null)throw new NullPointerException();
        this.closeable = closeable;
    }

    /**
     * Returns the Closeable this CloseableMBeanImpl exposes.
     *
     * @return the Closeable this CloseableMBeanImpl exposes.
     */
    public Closeable getCloseable() {
        return closeable;
    }

    public boolean isOpen() {
        return closeable.isOpen();
    }

    public boolean isClosed() {
        return closeable.isClosed();
    }

    public void open() {
        closeable.open();
    }

    public void close() {
        closeable.close();
    }
}
