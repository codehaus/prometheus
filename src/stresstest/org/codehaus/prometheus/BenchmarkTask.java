package org.codehaus.prometheus;

public interface BenchmarkTask {
    void beforeRun()throws Exception;
    void run() throws Exception;
}
