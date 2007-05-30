package org.codehaus.prometheus.processors;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Default implementation.
 */
public class StandardEventDispatcher implements EventDispatcher {

    public boolean dispatch(Process process, Event e) throws Exception {
        try {
            Method m = findMethod(process, e);
            m.invoke(process, e);
            return true;
        } catch (NoSuchMethodException e1) {
            return false;
        } catch (InvocationTargetException e1) {
            //throw e1.getTargetException();
            //todo: hack
            throw new Exception(e1);
        } catch (IllegalAccessException e1) {
            //todo: hack
            return false;
        }
    }

    /**
     * Tries to find the handler-method. If no handler method is found, a NoSuchMethodException is thrown.
     *
     * @param process
     * @param e
     * @return
     * @throws NoSuchMethodException if no handler method is found.
     */
    private Method findMethod(Process process, Event e) throws NoSuchMethodException {
        Class eventClass = e.getClass();

        for (; ;) {
            try {
                Method method = process.getClass().getMethod("handle", eventClass);
                if (!method.getReturnType().equals(Void.TYPE))
                    throw new NoSuchMethodException();
                return method;
            } catch (NoSuchMethodException ex) {
                eventClass = eventClass.getSuperclass();
                if (eventClass.equals(Object.class))
                    throw new NoSuchMethodException();
            }
        }
    }
}
