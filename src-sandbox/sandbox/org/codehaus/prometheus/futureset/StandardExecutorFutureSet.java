package org.codehaus.prometheus.futureset;

import java.util.LinkedList;
import java.util.List;
import static java.util.Collections.synchronizedList;
import java.util.concurrent.*;

public class StandardExecutorFutureSet implements ExecutorFutureSet {

    private final Executor executor;
    private final List<Future> futures = synchronizedList(new LinkedList<Future>());

    public StandardExecutorFutureSet(Executor executor) {
        if (executor == null) throw new NullPointerException();
        this.executor = executor;
    }

    public Future execute(Runnable task) {
        FutureTask futureTask = new FutureTask(task, null);
        executor.execute(task);
        futures.add(futureTask);
        return futureTask;
    }

    public <E> Future<E> execute(Callable<E> task) {
        FutureTask futureTask = new FutureTask(task);
        executor.execute(futureTask);
        futures.add(futureTask);
        return futureTask;
    }

    public void await() throws InterruptedException {

        while(!futures.isEmpty()){
            Future future = futures.iterator().next();
            try {
                future.get();
                futures.remove(future);
            } catch (ExecutionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public long tryAwait(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
