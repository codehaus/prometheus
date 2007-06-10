package org.codehaus.prometheus.processors;

public final class Void {

    public final static Void INSTANCE = new Void();

    @Override
    public int hashCode() {
        return 0;
    }

    public boolean equals(Object that) {
        if (that == null) return false;
        return that instanceof Void;
    }

    @Override
    public String toString() {
        return "void";
    }
}
