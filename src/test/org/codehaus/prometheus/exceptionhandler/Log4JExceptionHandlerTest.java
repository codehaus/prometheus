package org.codehaus.prometheus.exceptionhandler;

import junit.framework.TestCase;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import static org.easymock.EasyMock.*;

public class Log4JExceptionHandlerTest extends TestCase {

    public void testConstructor_noArg(){
        Log4JExceptionHandler handler = new Log4JExceptionHandler();
        assertNotNull(handler.getLogger());
        assertEquals(Level.ERROR,handler.getPriority());
    }

    public void testConstructor_Logger(){
        try{
            new Log4JExceptionHandler(null);
            fail();
        }catch(NullPointerException ex){
        }

        Logger logger = LogManager.getLogger(Log4JExceptionHandlerTest.class);
        Log4JExceptionHandler handler = new Log4JExceptionHandler(logger);
        assertSame(logger,handler.getLogger());
        assertEquals(Level.ERROR,handler.getPriority());
    }

    public void testSetPriority(){
        Log4JExceptionHandler handler = new Log4JExceptionHandler();
        try{
            handler.setPriority(null);
            fail();
        }catch(NullPointerException ex){
        }

        handler.setPriority(Level.WARN);
        assertEquals(Level.WARN,handler.getPriority());
    }

    public void testSetLogger(){
        Log4JExceptionHandler handler = new Log4JExceptionHandler();
        try{
            handler.setLogger(null);
            fail();
        }catch(NullPointerException ex){
        }

        Logger logger = LogManager.getLogger(Log4JExceptionHandlerTest.class);
        handler.setLogger(logger);
        assertEquals(logger,handler.getLogger());
    }

    public void testHandle(){
        Appender appender = createMock(Appender.class);

        Logger logger = LogManager.getLogger(Log4JExceptionHandlerTest.class);
        logger.removeAllAppenders();
        logger.addAppender(appender);

        Log4JExceptionHandler handler = new Log4JExceptionHandler(logger);
        Exception ex = new Exception("some error");

        appender.doAppend((LoggingEvent)anyObject());

        replay(appender);
        handler.handle(ex);
        verify(appender);

        //todo: log level, message and exception need to be checked
    }

    public void testHandle_UnwantedLoggingLevel(){
        //todo
    }
}
