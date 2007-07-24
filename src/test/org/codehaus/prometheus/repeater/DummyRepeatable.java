package org.codehaus.prometheus.repeater;

public class DummyRepeatable implements Repeatable {
    private final boolean returnValue;

    public DummyRepeatable(){
        this(true);
    }

    public DummyRepeatable(boolean returnValue){
        this.returnValue = returnValue;
    }

    public boolean execute() {
        return returnValue;
    }
}
