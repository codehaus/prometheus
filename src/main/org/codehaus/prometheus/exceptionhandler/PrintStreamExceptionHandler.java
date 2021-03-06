/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.exceptionhandler;

import java.io.PrintStream;

/**
 * An ExceptionHandler that uses a PrintStream to log to. There is a no arg constructor that uses
 * the System.out as PrintStream.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public class PrintStreamExceptionHandler implements ExceptionHandler {

    /**
     * An instanceof the PrintStreamExceptionHandler that writes to the System.out
     */
    public final static PrintStreamExceptionHandler SYS_OUT = new PrintStreamExceptionHandler();

    private final PrintStream out;

    /**
     * Creates a new PrintStreamExceptionHandler that uses System.out as PrintStream to
     * log to.
     */
    public PrintStreamExceptionHandler() {
        this(System.out);
    }

    /**
     * Creates a new PrintStreamExceptionHandler with the given PrintStream to log to.
     *
     * @param out the PrintStream this handler logs to.
     * @throws NullPointerException if out is <tt>null</tt>.
     */
    public PrintStreamExceptionHandler(PrintStream out) {
        if (out == null) throw new NullPointerException();
        this.out = out;
    }

    /**
     * Returns the PrintStream this handler logs to.
     *
     * @return the PrintStream this handler logs to.
     */
    public PrintStream getOut() {
        return out;
    }

    public void handle(Exception exception) {
        exception.printStackTrace(out);
    }
}
