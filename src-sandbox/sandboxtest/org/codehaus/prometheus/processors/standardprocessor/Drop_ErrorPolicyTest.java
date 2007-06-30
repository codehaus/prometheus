package org.codehaus.prometheus.processors.standardprocessor;

import junit.framework.TestCase;
import org.codehaus.prometheus.processors.VoidValue;

/**
 * Unit tests {@link Drop_ErrorPolicy}.
 *
 * @author Peter Veentjer.
 */
public class Drop_ErrorPolicyTest extends TestCase {

    public void test() {
        assertHandle(VoidValue.INSTANCE);
        assertHandle(1);
    }

    public void assertHandle(Object... in) {
        Drop_ErrorPolicy errorPolicy = new Drop_ErrorPolicy();
        Object result = errorPolicy.handleReceiveError(new Exception(), in);
        assertNull(result);
    }
}
