package org.codehaus.prometheus.futureset;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface ExecutorFutureSet {

    Future execute(Runnable task);

    <E> Future<E> execute(Callable<E> callable);

    void await()throws InterruptedException;
    
    long tryAwait(long timeout, TimeUnit unit)throws InterruptedException, TimeoutException;
}
