package org.codehaus.prometheus.exceptionhandler;

import junit.framework.TestCase;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import static org.easymock.classextension.EasyMock.*;

/**
 * Unittests {@link Log4JExceptionHandler}.
 *
 * @author Peter Veentjer.
 */
public class Log4JExceptionHandlerTest extends TestCase {

    public void testConstructor_noArg() {
        Log4JExceptionHandler handler = new Log4JExceptionHandler();
        assertNotNull(handler.getLogger());
        assertEquals(Level.ERROR, handler.getPriority());
    }

    public void testConstructor_Logger() {
        try {
            new Log4JExceptionHandler(null);
            fail();
        } catch (NullPointerException ex) {
        }

        Logger logger = LogManager.getLogger(Log4JExceptionHandlerTest.class);
        Log4JExceptionHandler handler = new Log4JExceptionHandler(logger);
        assertSame(logger, handler.getLogger());
        assertEquals(Level.ERROR, handler.getPriority());
    }

    public void testSetPriority() {
        Log4JExceptionHandler handler = new Log4JExceptionHandler();
        try {
            handler.setPriority(null);
            fail();
        } catch (NullPointerException ex) {
        }

        handler.setPriority(Level.WARN);
        assertEquals(Level.WARN, handler.getPriority());
    }

    public void testSetLogger() {
        Log4JExceptionHandler handler = new Log4JExceptionHandler();
        try {
            handler.setLogger(null);
            fail();
        } catch (NullPointerException ex) {
        }

        Logger logger = LogManager.getLogger(Log4JExceptionHandlerTest.class);
        handler.setLogger(logger);
        assertEquals(logger, handler.getLogger());
    }

    public void testHandle() {
        Appender appender = createMock(Appender.class);

        Level priority = Level.DEBUG;
        Logger logger = createMock(Logger.class);
        expect(logger.isEnabledFor(priority)).andReturn(true);

        Log4JExceptionHandler handler = new Log4JExceptionHandler(logger);
        handler.setPriority(priority);

        Exception ex = new Exception("some error");

        logger.log(priority, ex.getMessage(), ex);

        replay(appender, logger);
        handler.handle(ex);
        verify(appender, logger);
    }

    public void testHandle_UnwantedLoggingLevel() {
        Appender appender = createMock(Appender.class);

        Level priority = Level.DEBUG;

        Logger logger = createMock(Logger.class);
        expect(logger.isEnabledFor(priority)).andReturn(false);

        Log4JExceptionHandler handler = new Log4JExceptionHandler(logger);
        handler.setPriority(priority);

        Exception ex = new Exception("some error");

        replay(appender, logger);
        handler.handle(ex);
        verify(appender, logger);
    }
}
