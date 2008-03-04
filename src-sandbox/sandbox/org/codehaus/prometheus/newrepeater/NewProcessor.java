package org.codehaus.prometheus.newrepeater;

public interface NewProcessor {

    void run(RepeatValue value) throws InterruptedException;
}
