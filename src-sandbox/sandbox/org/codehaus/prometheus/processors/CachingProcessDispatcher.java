package org.codehaus.prometheus.processors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Peter Veentjer.
 */
public class CachingProcessDispatcher implements ProcessDispatcher {

    private final ProcessDispatcher dispather;
    private final Lock lock = new ReentrantLock();
    private final Map<Key, Method> methodCache = new HashMap<Key, Method>();

    public CachingProcessDispatcher() {
        this(new StandardProcessDispatcher());
    }

    public CachingProcessDispatcher(ProcessDispatcher dispatcher) {
        if (dispatcher == null) throw new NullPointerException();
        this.dispather = dispatcher;
    }

    public ProcessDispatcher getDispather() {
        return dispather;
    }

    public Object dispatch(Object process, Object arg) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (process == null || arg == null) throw new NullPointerException();
        //Key key = new Key(process.getClass(),arg.getClass());
        //lock.lock();
        //try{
        //    Method method =
        //}finally{
        //    lock.unlock();
        //}
        throw new RuntimeException();
    }

    static class Key {
        private final Class targetClass;
        private final Class argumentClass;

        public Key(Class targetClass, Class argumentClass) {
            this.targetClass = targetClass;
            this.argumentClass = argumentClass;
        }

        public boolean equals(Object o) {
            throw new RuntimeException();
        }

        public int hashCode() {
            return targetClass.hashCode() * 31 + argumentClass.hashCode();
        }
    }
}
