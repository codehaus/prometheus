package org.codehaus.prometheus.processors;


/**
 *
 * @author Peter Veentjer.
 */
public final class VoidValue {

    public final static VoidValue INSTANCE = new VoidValue();

    @Override
    public int hashCode() {
        return 0;
    }

    public boolean equals(Object that) {
        return that instanceof VoidValue;
    }

    @Override
    public String toString() {
        return "void";
    }
}
