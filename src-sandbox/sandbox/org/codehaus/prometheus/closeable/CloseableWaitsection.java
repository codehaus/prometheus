/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.closeable;

import org.codehaus.prometheus.waitpoint.Waitsection;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Waitsection that can be opened and closed.
 *
 * @author Peter Veentjer.
 */
public class CloseableWaitsection implements Closeable, Waitsection {

    public static Lock newDefaultMainLock() {
        return new ReentrantLock();
    }

    private final Lock mainLock;
    private final Condition isOpenCondition;
    private volatile boolean open;

    public CloseableWaitsection(boolean open) {
        this(newDefaultMainLock(),open);
    }

    public CloseableWaitsection(Lock mainLock, boolean open) {
        if (mainLock == null) throw new NullPointerException();
        this.open = open;
        this.mainLock = mainLock;
        this.isOpenCondition = mainLock.newCondition();
    }

    public Lock getMainLock() {
        return mainLock;
    }

    public Condition getOpenCondition() {
        return isOpenCondition;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isClosed() {
        return !open;
    }

    public void open() {
        mainLock.lock();
        try{
            open = true;
            isOpenCondition.signalAll();
        }finally{
            mainLock.unlock();
        }
    }

    public void close() {
        open = false;
    }

    public void enter() throws InterruptedException {
        if(open)
            return;

        mainLock.lockInterruptibly();
        try{
            while(!open)
                isOpenCondition.await();
        }finally{
            mainLock.unlock();
        }
    }

    public void enterUninterruptibly() {
        if(open)
            return;

        mainLock.lock();
        try{
            while(!open)
                isOpenCondition.awaitUninterruptibly();
        }finally{
            mainLock.unlock();
        }
    }

    public boolean isEnterable() {
        return open;
    }

    public boolean tryEnter() {
        throw new RuntimeException();
    }

    public long tryEnter(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        throw new RuntimeException();
    }

    public long tryEnterUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
        throw new RuntimeException();
    }

    public void exit() {
        //do nothing
    }
}
