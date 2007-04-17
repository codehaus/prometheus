/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import java.util.concurrent.TimeUnit;

public class TimeoutThreadLocal {

    private static final ThreadLocal<Long> timeoutThreadLocal =
            new ThreadLocal<Long>() {
                private long timeout;

                protected Long initialValue() {
                    return TimeUnit.SECONDS.toNanos(60);
                }

                public Long get() {
                    return timeout;
                }

                public void set(Long timeout) {
                    this.timeout = timeout;
                }
            };

    /**
     *
     * @param timeoutNs
     */
    public static void set(long timeoutNs){
        timeoutThreadLocal.set(timeoutNs);
    }

    /**
     *
     * @param amountNs
     */
    public static void decrease(long amountNs){
        
    }

    /**
     *
     * @param timeout
     * @param unit
     */
    public static void set(long timeout, TimeUnit unit){
        long timeoutNs = unit.toNanos(timeout);
        timeoutThreadLocal.set(timeoutNs);
    }

    /**
     * 
     * @return
     */
    public static long get() {
        return timeoutThreadLocal.get();
    }
}
