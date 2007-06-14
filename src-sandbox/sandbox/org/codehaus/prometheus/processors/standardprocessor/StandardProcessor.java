package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.channels.InputChannel;
import org.codehaus.prometheus.channels.OutputChannel;
import org.codehaus.prometheus.processors.standardprocessor.Policy;
import org.codehaus.prometheus.processors.standardprocessor.ThrowPolicy;
import org.codehaus.prometheus.processors.*;

import static java.lang.String.format;
import java.lang.reflect.InvocationTargetException;

/**
 * A {@link org.codehaus.prometheus.processors.Processor} that executes a piped process. The input is taken from an {@link InputChannel}
 * and the output is placed on an {@Link OutputChannel}. If there is no InputChannel, it is a source
 * process. If there is no output, it is a sink process.
 * <p/>
 * <h1>Dispatching</h1>
 * <p/>
 * If a receive method returns a non null reference, this reference is send to the output (when output exists,
 * if no output exists, the item is dropped).
 * If a receive method returns null, nothing is send to output.
 * If a receive method returns void, the input is send to the output (if there was input, if there is no input,
 * nothing happens).
 * When no matching receive method exists, the inputitem is send to the output. If there was no input, or
 * there is no output, nothing happens.
 * <p/>
 * <p/>
 * parametrization is quite useless if you also want to transport events
 * <p/>
 * <h1>Stopping a processor</h1>
 * A process can signal the processor to stop processing. At the moment it can be done by letting the
 * receive method return an object that implements the ProcessDeath message. The message is send to the output
 * (if this is available) and then false is returned by the {@link #once()} method. I'm also thinking about
 * different ways to stop processors, an possible alternative would be throwing some sort of exception.
 * <p/>
 * <h1>Exception handling</h1>
 * All exceptions are tracked when a receive method
 * <p/>
 * main goal: only core responsibily:
 * -transformation of data (piped process)
 * -generation of data (source process)
 * -consuming data (sink process)
 * <p/>
 * not responsibilities process:
 * -overall exception handling
 * -threading
 * -how to get data
 * -how to get rid of it
 * -dispatching on type of data
 * -logging
 * -tracing
 *
 * @author Peter Veentjer.
 */
public class StandardProcessor<P> implements Processor {
    private final P process;
    private final InputChannel input;
    private final OutputChannel output;
    private volatile Dispatcher dispatcher = new StandardDispatcher();

    /**
     * Creates a sink processor: a sink processor is processor
     * that runs a sink process (a process that consumes data).
     *
     * @param input
     * @param process
     * @throws NullPointerException if process is null.
     */
    public StandardProcessor(InputChannel input, P process) {
        this(input, process, null);
    }

    /**
     * Creates a source processor: a processor that runs a
     * source process (a process that produces data).
     *
     * @param process
     * @param output
     * @throws NullPointerException if process is null.
     */
    public StandardProcessor(P process, OutputChannel output) {
        this(null, process, output);
    }

    /**
     * Creates a piped processor: a processor that runs a
     * piped process (a process that transforms data).
     *
     * @param input
     * @param process
     * @param output
     * @throws NullPointerException if process is null.
     */
    public StandardProcessor(InputChannel input, P process, OutputChannel output) {
        if (process == null) throw new NullPointerException();
        this.process = process;
        this.input = input;
        this.output = output;
    }

    /**
     * Returns the process this StandardProcessor is executing. The returned value
     * will always be a non null value.
     *
     * @return the process that is being executed.
     */
    public P getProcess() {
        return process;
    }

    /**
     * Returns the InputChannel where this StandardProcessor receives its input from.
     * The returned value is allowed to be null (indicating a source processor).
     *
     * @return
     */
    public InputChannel getInput() {
        return input;
    }

    /**
     * Returns the OutputChannel where this StandardProcessor sends its output to.
     * The returned value is allowed to be null (indicating a sink processor)
     * @return
     */
    public OutputChannel getOutput() {
        return output;
    }

    public boolean isSourceProcessor(){
        throw new RuntimeException();
    }

    public boolean isSinkProcessor(){
        throw new RuntimeException();
    }

    public boolean isPipedProcessor(){
        throw new RuntimeException();
    }

    public boolean once() throws Exception {
        Object out;
        Object in;
        if (input == null) {
            out = dispatch();
            in = null;
        } else {
            in = input.take();
            out = dispatch(in);
        }

        out = determineOutput(out, in);
        output(out);
        return !(out instanceof ProcessDeath);
    }

    private volatile Policy policy = new ThrowPolicy();


    //todo:
    //at the moment 0..1 in argument is possible.. so why the varargs.
    //null indicates no argument, non null indicates argumnent
    /**
     * Calls a method on the process.
     *
     * @param in the actual arguments of the method to call.
     * @return the result of the method call on the process.
     * @throws Exception the exception the method call causes
     */
    private Object dispatch(Object... in) throws Exception {
        try {
            return dispatcher.dispatch(process, in);
        } catch (NoSuchMethodException ex) {
            return org.codehaus.prometheus.processors.VoidValue.INSTANCE;
        } catch (InvocationTargetException ex) {
            Throwable target = ex.getTargetException();
            if (!(target instanceof Exception)) {
                //we are not going to deal with it
                //todo: needs to be cast to error, etc
                throw new RuntimeException(target);
            }

            Exception targetException = (Exception) target;
            //todo: add logging?

            return policy.handle(targetException, in);
        }
    }

    private Object determineOutput(Object out, Object in) {
        //if out was void, we should use the in
        if (VoidValue.INSTANCE.equals(out))
            out = in;

        return out;
    }

    private void output(Object out) throws InterruptedException {
        //if there is no output, we are finishes (the out is dropped)
        if (output == null)
            return;

        //if out is null we should not post a result
        if (out == null)
            return;

        output.put(out);
    }

    @Override
    public String toString() {
        return format("StandardProcessor(%s)", process);
    }
}
