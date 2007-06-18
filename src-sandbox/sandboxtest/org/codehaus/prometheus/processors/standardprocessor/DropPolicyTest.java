package org.codehaus.prometheus.processors.standardprocessor;

import junit.framework.TestCase;
import org.codehaus.prometheus.processors.VoidValue;

/**
 * Unit tests {@link DropPolicy}.
 *
 * @author Peter Veentjer.
 */
public class DropPolicyTest extends TestCase {

    public void test_noArg(){
                
    }

    public void test_oneArg(){
        assertHandle(VoidValue.INSTANCE);
        assertHandle(1);
    }

    public void test_multipleArgs(){

    }

    public void assertHandle(Object... in){
        DropPolicy policy = new DropPolicy();
        Object result = policy.handle(new Exception(),in);
        assertNull(result);
    }
}
