package org.codehaus.prometheus.channels;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

//problem with multicast output channel is that it isn't atomic.
public class MulticastOutputChannel<E> implements OutputChannel<E>{

    

    public void put(E item) throws InterruptedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void savePut(E item) throws InterruptedException, TimeoutException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public long offer(E item, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
