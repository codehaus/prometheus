package org.codehaus.prometheus.processors;

import java.lang.reflect.InvocationTargetException;

/**
 * Dispatcher is responsible for calling a method on a process.
 *
 * @author Peter Veentjer.
 */
public interface Dispatcher {

    /**
     *
     *
     * @param process the target object that has the method.
     * @param args the arguments to feed to process
     * @return the value that was returned by the method. If the method has void as return type,
     *         an instanceof {@link VoidValue} will be returned.
     * @throws NoSuchMethodException if no matching method is found.
     */
    Object dispatch(Object process, Object... args)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;
}
