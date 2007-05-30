package org.codehaus.prometheus.processors;

/**
 * In Erlang a process is executed on a single thread, and isn't shared.
 *
 * a process is able to do calls with timeout.
 *
 * Process should not maintain references to data is processes.
 *
 * Process provides a way to decouple executing logic from the input and output.
 *
 * markerinterface (although it has no functionality itself at the moment).
 *
 * @author Peter Veentjer.
 */
public interface Process {

    
}
