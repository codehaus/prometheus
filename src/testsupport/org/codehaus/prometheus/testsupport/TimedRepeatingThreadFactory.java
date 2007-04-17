/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.Vector;

/**
 * A ThreadFactory that creates TimedRepeatingThread.
 *
 * All created threads are stored in a collection, but at
 * the moment nothing is done with this information. 
 *
 * @author Peter Veentjer.
 */
public class TimedRepeatingThreadFactory implements ThreadFactory {
    private final long maxtime;
    private final TimeUnit unit;
    private final Vector<Thread> threadList = new Vector<Thread>();

    public TimedRepeatingThreadFactory(long maxtime, TimeUnit unit){
        this.maxtime = maxtime;
        this.unit = unit;
    }

    public TimedRepeatingThread newThread(Runnable r) {
        TimedRepeatingThread t =  new TimedRepeatingThread(maxtime,unit,r);
        threadList.add(t);
        return t;
    }
}
