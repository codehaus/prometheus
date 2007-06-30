package org.codehaus.prometheus.processors.standardprocessor;

import junit.framework.TestCase;
import org.codehaus.prometheus.processors.VoidValue;

/**
 * Unit tests {@link Ignore_ErrorPolicy}.
 *
 * @author Peter Veentjer.
 */
public class Ignore_ErrorPolicyTest extends TestCase {

    public void testHandle_oneArgument() {
        assertHandleReturnsVoid(10);
        assertHandleReturnsVoid(VoidValue.INSTANCE);
    }

    public void assertHandleReturnsVoid(Object in) {
        Ignore_ErrorPolicy ignorePolicy = new Ignore_ErrorPolicy();
        Object out = ignorePolicy.handleReceiveError(new Exception(), in);
        assertEquals(VoidValue.INSTANCE, out);
    }
}
