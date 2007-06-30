/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The ComposedWaitpoint is a Waitpoint that is composed of a chain
 * of Waitpoints. It can be used to combine the behaviour of
 * other Waitpoints.
 * <p/>
 * idea:
 * The ComposedWaitpoint should be able to execute all Waitpoints
 * under once single atomic action (using a lock). At the moment
 * all Waitpoint acquire their own lock, unless specified otherwise.
 *
 * @author Peter Veentjer.
 */
public class ComposedWaitpoint implements Waitpoint {
    private final Waitpoint[] waitpoints;

    public ComposedWaitpoint(Waitpoint... waitpoints) {
        if (waitpoints == null) throw new NullPointerException();
        this.waitpoints = waitpoints;
    }

    public void pass() throws InterruptedException {
        for (Waitpoint waitpoint : waitpoints)
            waitpoint.pass();
    }

    public boolean isPassible() {
        //if once of the waitpoints is not passible, this Waitpoint is not passible.
        for (Waitpoint waitpoint : waitpoints) {
            if (!waitpoint.isPassible())
                return false;
        }

        //they were all passible (or there were no waitpoints), so this
        //waitpoint is passible.
        return true;
    }

    public boolean tryPass() {
        for (Waitpoint waitpoint : waitpoints) {
            //if once of the waitpoints is not passible, this waitpoint is not passible.
            if (!waitpoint.tryPass())
                return false;
        }
        //they were all passible (or there were no waitpoints), so this
        //waitpoint is passible.
        return true;
    }

    public long tryPass(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        if (unit == null) throw new NullPointerException();
        throw new NullPointerException();
    }
}
