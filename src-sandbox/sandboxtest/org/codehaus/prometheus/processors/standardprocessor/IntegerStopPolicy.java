package org.codehaus.prometheus.processors.standardprocessor;

import static java.util.Arrays.asList;
import java.util.HashSet;
import java.util.Set;

/**
 * A StopPolicy that can be used for testing. It returns true if the msg to check
 * is in the set of rejects, false otherwise.
 *
 * @author Peter Veentjer.
 */
public class IntegerStopPolicy implements StopPolicy {

    private final Set<Integer> rejects = new HashSet<Integer>();

    public IntegerStopPolicy(Integer... rejects) {
        this.rejects.addAll(asList(rejects));
    }

    public boolean shouldStop(Object msg) {
        return rejects.contains(msg);
    }
}
