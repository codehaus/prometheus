package org.codehaus.prometheus.processors.standardprocessor;

import junit.framework.TestCase;

import java.util.*;

/**
 * Unittests the {@link TypeBasedStopPolicy}.
 *
 * @author Peter Veentjer
 */
public class TypeBasedStopPolicyTest extends TestCase {
    private TypeBasedStopPolicy stopPolicy;

    public void testConstructor_set() {
        try {
            new TypeBasedStopPolicy((Set<Class>) null);
            fail();
        } catch (NullPointerException ex) {
        }

        Set<Class> classes = new HashSet<Class>();
        classes.add(Integer.class);
        classes.add(Vector.class);
        TypeBasedStopPolicy policy = new TypeBasedStopPolicy(classes);
        assertEquals(classes, policy.getClasses());
    }

    public void testConstructor_array(){
        try {
            new TypeBasedStopPolicy((Class[]) null);
            fail();
        } catch (NullPointerException ex) {
        }

        Set<Class> classes = new HashSet<Class>();
        classes.add(Integer.class);
        classes.add(Vector.class);
        TypeBasedStopPolicy policy = new TypeBasedStopPolicy(Integer.class,Vector.class,Integer.class);
        assertEquals(classes, policy.getClasses());
    }

    public void testNull() {
        stopPolicy = new TypeBasedStopPolicy();
        try {
            stopPolicy.shouldStop(null);
            fail();
        } catch (NullPointerException ex) {

        }
    }

    public void testNoMatchingClass() {
        stopPolicy = new TypeBasedStopPolicy();
        assertShouldNotStop("foo");
    }

    public void testEqualClass() {
        stopPolicy = new TypeBasedStopPolicy(String.class);
        assertShouldStop("foo");
    }

    private void assertShouldStop(Object value) {
        boolean stop = stopPolicy.shouldStop(value);
        assertTrue(stop);
    }

    private void assertShouldNotStop(Object value) {
        boolean stop = stopPolicy.shouldStop(value);
        assertFalse(stop);
    }

    public void testSubclass() {
        stopPolicy = new TypeBasedStopPolicy(List.class);
        assertShouldStop(new ArrayList());
    }
}
