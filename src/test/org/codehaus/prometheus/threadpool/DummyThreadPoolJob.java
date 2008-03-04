/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import junit.framework.Assert;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A ThreadPoolJob specific for testing purposes.
 *
 * @author Peter Veentjer.
 */
public class DummyThreadPoolJob implements ThreadPoolJob {

    private final AtomicInteger takeCount = new AtomicInteger();
    private final AtomicInteger executeWorkCount = new AtomicInteger();
    private final ThreadPool threadpool;

    public DummyThreadPoolJob(ThreadPool threadpool){
        this.threadpool = threadpool;
    }

    public void assertNoTakeWork(){
        Assert.assertEquals(0,takeCount.intValue());
    }

    public void assertNoExecuteWork(){
        Assert.assertEquals(0,executeWorkCount.intValue());
    }

    public void assertHasMultipleTakeWork(){
        Assert.assertTrue(takeCount.intValue()>1);
    }

    public void assertHasMultipleExecuteWork(){
        Assert.assertTrue(executeWorkCount.intValue()>1);
    }

    public Object takeWork() throws InterruptedException {
        takeCount.incrementAndGet();
        return "";
    }

    public boolean executeWork(Object task) throws Exception {
        if(threadpool.getState()!=ThreadPoolState.running){
            return false;
        }

        executeWorkCount.incrementAndGet();
        return true;
    }
}
