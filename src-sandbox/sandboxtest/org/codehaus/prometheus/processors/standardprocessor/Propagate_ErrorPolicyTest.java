package org.codehaus.prometheus.processors.standardprocessor;

import junit.framework.TestCase;

/**
 * Unit tests {@link Propagate_ErrorPolicy}.
 *
 * @author Peter Veentjer.
 */
public class Propagate_ErrorPolicyTest extends TestCase {

    public void testHandle() throws Exception {
        Propagate_ErrorPolicy policy = new Propagate_ErrorPolicy();
        Exception ex = new Exception();

        try {
            policy.handleReceiveError(ex, 1);
            fail();
        } catch (Exception foundEx) {
            assertSame(ex, foundEx);
        }
    }
}
