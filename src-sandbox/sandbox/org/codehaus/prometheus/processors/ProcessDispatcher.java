package org.codehaus.prometheus.processors;

import java.lang.reflect.InvocationTargetException;

/**
 * ProcessDispatcher is responsible for calling a method on a process.
 *
 * @author Peter Veentjer.
 */
public interface ProcessDispatcher {

    /**
     * @param process the target object that has the method.
     * @param arg     the arguments to feed to process. If no argument is available, a VoidValue should be used
     * @return the value that was returned by the method. If the method has void as return type,
     *         an instanceof {@link VoidValue} will be returned.
     * @throws NoSuchMethodException     if no matching method is found.
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NullPointerException      if process or arg is null.
     */
    Object dispatch(Object process, Object arg)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;
}
