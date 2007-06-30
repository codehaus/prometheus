package org.codehaus.prometheus.processors;

public class NoArgProcess extends TestProcess {
    private final Object returned;

    public NoArgProcess() {
        this(VoidValue.INSTANCE);
    }

    public NoArgProcess(Object returned) {
        this.returned = returned;
    }

    public Object receive() {
        signalCalled();
        return returned;
    }
}
