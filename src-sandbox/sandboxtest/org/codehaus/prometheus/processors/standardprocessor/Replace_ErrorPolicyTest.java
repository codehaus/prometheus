package org.codehaus.prometheus.processors.standardprocessor;

import junit.framework.TestCase;
import org.codehaus.prometheus.processors.VoidValue;
import static org.codehaus.prometheus.testsupport.TestUtil.randomInt;

/**
 * Unit tests {@link Replace_ErrorPolicy}.
 *
 * @author Peter Veentjer.
 */
public class Replace_ErrorPolicyTest extends TestCase {

    public void test_singleArg() {
        assertReplaced(VoidValue.INSTANCE);
        assertReplaced("foo");
    }

    public void assertReplaced(Object in) {
        Object replacement = randomInt();
        Replace_ErrorPolicy policy = new Replace_ErrorPolicy(replacement);
        Object found = policy.handleReceiveError(new Exception(), in);
        assertSame(replacement, found);
    }
}
