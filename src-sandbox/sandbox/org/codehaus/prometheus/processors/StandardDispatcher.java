package org.codehaus.prometheus.processors;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.*;

/**
 * Default implementation.
 */
public class StandardDispatcher implements Dispatcher {

    private static final String NAME_HANDLE_METHOD = "receive";

    public Object dispatch(Object process, Object... args) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Method method = findMethod(process, args);
        Object result = method.invoke(process, args);
        return returnsVoid(method) ? Void.INSTANCE : result;
    }

    private boolean returnsVoid(Method method) {
        return method.getReturnType().equals(java.lang.Void.TYPE);
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
