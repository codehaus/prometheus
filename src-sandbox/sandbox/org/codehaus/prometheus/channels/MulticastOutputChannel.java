package org.codehaus.prometheus.channels;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.List;

//problem with multicast output channel is that it isn't atomic.
public class MulticastOutputChannel<E> implements OutputChannel<E>{

    private final List<OutputChannel<E>> targets;

    public MulticastOutputChannel(List<OutputChannel<E>> targets) {
        if(targets == null)throw new NullPointerException();
        this.targets = targets;
    }

    public void put(E item) throws InterruptedException {
        for(OutputChannel<E> channel: targets)
            channel.put(item);
    }

    public long offer(E item, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
