package org.codehaus.prometheus.processors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Default implementation of a {@link org.codehaus.prometheus.processors.Dispatcher}.
 * <p/>
 * todo: zoeken op interfaces/super interfaces
 * todo: ambiguous methods.
 *
 * @author Peter Veentjer.
 */
public class StandardDispatcher implements Dispatcher {

    private static final String NAME_HANDLE_METHOD = "receive";

    public Object dispatch(Object process, Object arg) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (process == null || arg == null) throw new NullPointerException();

        if (arg instanceof VoidValue) {
            Method method = process.getClass().getMethod(NAME_HANDLE_METHOD);
            return invoke(process, method);
        } else {
            Method method = findMethod(process, arg);
            return invoke(process, method, arg);
        }
    }

    private Object invoke(Object process, Method method, Object... args) throws IllegalAccessException, InvocationTargetException {
        Object result = method.invoke(process, args);
        return determineReturnValue(method, result);
    }

    private Object determineReturnValue(Method method, Object result) {
        return returnsVoid(method) ? VoidValue.INSTANCE : result;
    }

    private boolean returnsVoid(Method method) {
        return method.getReturnType().equals(Void.TYPE);
    }

    /**
     * Tries to find the handler-method. If no handler method is found, a NoSuchMethodException is thrown.
     *
     * @param process
     * @param arg
     * @return
     * @throws NoSuchMethodException if no usable method is found.
     */
    private Method findMethod(Object process, Object arg) throws NoSuchMethodException {
        Class processClass = process.getClass();
        Class paramType = arg.getClass();
        for (; ;) {
            try {
                return processClass.getMethod(NAME_HANDLE_METHOD, paramType);
            } catch (NoSuchMethodException ex) {
                paramType = paramType.getSuperclass();
                if (paramType == null)
                    throw new NoSuchMethodException();
            }
        }
    }
}
