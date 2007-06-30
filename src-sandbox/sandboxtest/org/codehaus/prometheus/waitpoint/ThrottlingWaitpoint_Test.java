/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import junit.framework.TestCase;

public class ThrottlingWaitpoint_Test extends TestCase {
    private volatile ThrottlingWaitpoint waitpoint;

    public void test() throws InterruptedException {
        waitpoint = new ThrottlingWaitpoint(20);
        RunThread t = new RunThread();
        t.start();
        t.join();
    }


    public class RunThread extends Thread {
        private int count;

        public void run() {
            while (count < 100) {
                waitpoint.passUninterruptibly();
                count++;
            }
        }
    }
}
