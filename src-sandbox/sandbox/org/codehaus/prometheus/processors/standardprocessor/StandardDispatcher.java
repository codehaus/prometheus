package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.Dispatcher;
import org.codehaus.prometheus.processors.VoidValue;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.*;

/**
 * Default implementation of a {@link org.codehaus.prometheus.processors.Dispatcher}.
 *
 * @author Peter Veentjer.
 */
public class StandardDispatcher implements Dispatcher {

    private static final String NAME_HANDLE_METHOD = "receive";

    public Object dispatch(Object process, Object... args) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (process == null) throw new NullPointerException();

        Method method = findMethod(process, args);
        return invoke(process, method, args);
    }

    private Object invoke(Object process, Method method, Object... args) throws IllegalAccessException, InvocationTargetException {
        Object result = method.invoke(process, args);
        return returns(method, result);
    }

    private Object returns(Method method, Object result) {
        return returnsVoid(method) ? VoidValue.INSTANCE : result;
    }

    private boolean returnsVoid(Method method) {        
        return method.getReturnType().equals(Void.TYPE);
    }

    /**
     * Tries to find the handler-method. If no handler method is found, a NoSuchMethodException is thrown.
     *
     * @param process
     * @param args
     * @return
     * @throws NoSuchMethodException if no usable method is found.
     */
    private Method findMethod(Object process, Object[] args) throws NoSuchMethodException {
        Class[] argTypes = toArgTypes(args);
        return process.getClass().getMethod(NAME_HANDLE_METHOD, argTypes);
    }

    private Class[] toArgTypes(Object[] args) {
        Class[] argTypes = new Class[args.length];
        for (int k = 0; k < args.length; k++) {
            Object arg = args[k];
            if (arg != null)
                argTypes[k] = arg.getClass();
        }
        return argTypes;
    }
}
