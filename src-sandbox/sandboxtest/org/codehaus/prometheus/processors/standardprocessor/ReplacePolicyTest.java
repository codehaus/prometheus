package org.codehaus.prometheus.processors.standardprocessor;

import junit.framework.TestCase;
import org.codehaus.prometheus.processors.VoidValue;

/**
 * Unit tests {@link ReplacePolicy}.
 *
 * @author Peter Veentjer.
 */
public class ReplacePolicyTest extends TestCase {

    public void test_noArg() {
        //todo
    }

    public void test_singleArg() {
        assertReplaced(VoidValue.INSTANCE);
        assertReplaced("foo");
    }

    public void assertReplaced(Object... in) {
        Object replace = System.nanoTime();
        ReplacePolicy policy = new ReplacePolicy(replace);
        Object found = policy.handle(new Exception(), in);
        assertSame(replace, found);
    }

    public void test_multipleArgs() {
        //todo
    }
}
