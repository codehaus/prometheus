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
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long tryPassUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
