package org.codehaus.prometheus.processors;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Default implementation. 
 */
public class StandardEventDispatcher implements EventDispatcher{

    public boolean dispatch(Process process, Event e) throws Exception{
        try {
            Method m = findMethod(process,e);
            m.invoke(process,e);
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

    private Method findMethod(Process process, Event e) throws NoSuchMethodException {
        Class eventClass = e.getClass();

        for(;;){
            try{
               return process.getClass().getMethod("handle", eventClass);
            }catch(NoSuchMethodException ex){
                eventClass = eventClass.getSuperclass();
                if(eventClass.getClass().equals(Object.class))
                    throw new NoSuchMethodException();
            }
        }        
    }
}
