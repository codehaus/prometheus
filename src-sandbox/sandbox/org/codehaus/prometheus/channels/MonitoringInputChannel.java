package org.codehaus.prometheus.channels;

import org.codehaus.prometheus.monitoring.Monitorable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * todo:
 * speed (messages per second/minute/hour etc)
 * waiting time
 * number of waiting threads
 * counting certain types of messages  (handy for errors)
 *
 * @author Peter Veentjer.
 */
public class MonitoringInputChannel<E> implements InputChannel<E>, Monitorable {

    public final static String KEY_COUNT = "count";

    private volatile boolean on;
    private final InputChannel<E> target;
    private final AtomicLong count = new AtomicLong();

    /**
     * @param target
     * @throws NullPointerException if target is null.
     */
    public MonitoringInputChannel(InputChannel<E> target) {
        this(target, true);
    }

    /**
     * @param target
     * @param on
     * @throws NullPointerException if target is null
     */
    public MonitoringInputChannel(InputChannel<E> target, boolean on) {
        if (target == null) throw new NullPointerException();
        this.target = target;
        this.on = on;
    }

    public InputChannel<E> getTarget() {
        return target;
    }

    public boolean isOn() {
        return on;
    }

    public void turnOn() {
        on = true;
    }

    public void turnOff() {
        on = false;
    }

    public void reset() {
        count.set(0);
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> map = new HashMap();
        map.put(KEY_COUNT, count.longValue());
        return map;
    }

    public E take() throws InterruptedException {
        if (on) {
            E item = target.take();
            count.incrementAndGet();
            return item;
        } else {
            return target.take();
        }
    }

    public E poll(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        if (on) {
            E item = target.poll(timeout, unit);
            if (item != null)
                count.incrementAndGet();
            return item;
        } else {
            return target.poll(timeout, unit);
        }
    }

    public E poll() {
        if (on) {
            E item = target.poll();
            if (item != null)
                count.incrementAndGet();
            return item;
        } else {
            return target.poll();
        }
    }

    public E peek() {
        return target.peek();
    }
}
