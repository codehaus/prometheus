package org.codehaus.prometheus;

public interface InterruptibleRunnable {

    void run()throws InterruptedException;
}
