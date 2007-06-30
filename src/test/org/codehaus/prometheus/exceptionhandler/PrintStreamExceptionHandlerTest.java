package org.codehaus.prometheus.exceptionhandler;

import junit.framework.TestCase;
import static org.easymock.classextension.EasyMock.*;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Unittests the {@link PrintStreamExceptionHandler}.
 *
 * @author Peter Veentjer.
 */
public class PrintStreamExceptionHandlerTest extends TestCase {

    public void testConstructor_noArg() {
        PrintStreamExceptionHandler handler = new PrintStreamExceptionHandler();
        assertSame(System.out, handler.getOut());
    }

    public void testConstructor_PrintStream() throws FileNotFoundException {
        try {
            new PrintStreamExceptionHandler(null);
            fail();
        } catch (NullPointerException ex) {
        }

        PrintStream out = createMock(PrintStream.class);

        replay(out);
        PrintStreamExceptionHandler handler = new PrintStreamExceptionHandler(out);
        verify(out);

        assertSame(out, handler.getOut());
    }

    public void testHandle() {
        PrintStream out = createMock(PrintStream.class);

        Exception ex = createMock(Exception.class);
        ex.printStackTrace(out);

        replay(out, ex);
        PrintStreamExceptionHandler handler = new PrintStreamExceptionHandler(out);
        handler.handle(ex);
        verify(out, ex);
    }
}
