package org.codehaus.prometheus.repeater.benchmark;

import org.codehaus.prometheus.repeater.Repeatable;
import org.codehaus.prometheus.testsupport.ConcurrentTestUtil;

public class StressRepeatable implements Repeatable {
    private final int[] complexity;
    private int index = 0;

    public StressRepeatable(int[] complexity) {
        this.complexity = complexity;
    }

    public synchronized int nextIndex(){
        if(index >= complexity.length){
            return -1;
        }else{
            int result = index;
            index++;
            return result;
        }
    }

    public boolean execute() throws Exception {
        int nextIndex = nextIndex();
        if(nextIndex == -1)
            return false;

        ConcurrentTestUtil.someCalculation(complexity[nextIndex]);
        return true;
    }
}
