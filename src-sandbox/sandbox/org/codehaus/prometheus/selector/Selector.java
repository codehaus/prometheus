/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.selector;

import org.codehaus.prometheus.repeater.Repeater;
import org.codehaus.prometheus.repeater.RepeatableRunnable;

import java.util.concurrent.BlockingQueue;

/**
 * an executor already is a selector. n threads can serve m clients.
 * non blocking..
 *
 * non blocking channel. zonder queueing.
 * non blocking io.
 */
public class Selector {

    private BlockingQueue<Runnable> queue;
    private Repeater repeater;

    public void start() throws InterruptedException {
        repeater.repeat(new RepeatableRunnable(new Task()));
    }

    private class Task extends Thread {
        public void run() {
            try {
                while (true) {
                    Runnable r = queue.take();
                    try {
                        r.run();
                    } catch (RuntimeException re) {

                    }
                }
            } catch (InterruptedException e) {
               throw new RuntimeException();
            }
        }
    }
}
