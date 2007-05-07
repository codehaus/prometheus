/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Waitpoint that doesn't do anything. It can be used on placed where
 * a Waitpoint is needed, but you always want to pass.
 *
 * @author Peter Veentjer.
 */
public class NoOpWaitpoint implements Waitpoint{

    public final static NoOpWaitpoint INSTANCE = new NoOpWaitpoint();

    public void pass(){
    }

    public void passUninterruptibly() {
    }

    public boolean isPassible() {
        return true;
    }

    public boolean tryPass() {
        return true;
    }

    public long tryPass(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        throw new RuntimeException("not implemented yet");
    }

    public long tryPassUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
        throw new RuntimeException("not implemented yet");
    }
}
