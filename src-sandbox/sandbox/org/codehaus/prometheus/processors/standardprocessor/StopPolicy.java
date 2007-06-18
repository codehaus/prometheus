package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.ProcessDeath;
import org.codehaus.prometheus.processors.ProcessDeathValue;

/**
 * The StopPolicy passes the ProcessDeath value to the next process/output and stops running
 * itself.
 *
 * @author Peter Veentjer.
 */
public class StopPolicy implements ErrorPolicy {
    private final ProcessDeath value;

    public StopPolicy() {
        this(ProcessDeathValue.INSTANCE);
    }

    public StopPolicy(ProcessDeath value) {
        if (value == null) throw new NullPointerException();
        this.value = value;
    }

    public ProcessDeath getValue() {
        return value;
    }

    public Object handle(Exception ex, Object... in) {
        return value;
    }
}