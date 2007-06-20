package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.channels.InputChannel;
import org.codehaus.prometheus.channels.OutputChannel;
import org.codehaus.prometheus.processors.Dispatcher;
import org.codehaus.prometheus.processors.Processor;
import org.codehaus.prometheus.processors.VoidValue;

import static java.lang.String.format;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link org.codehaus.prometheus.processors.Processor} that executes a piped process. The takeInput is
 * taken from an {@link InputChannel} and the sendOutput is placed on an {@Link OutputChannel}. If there
 * is no InputChannel, it is a source process. If there is no sendOutput, it is a sink process.
 * <p/>
 * <h1>Dispatching</h1>
 * <p/>
 * If a receive method returns a non null reference, this reference is send to the sendOutput (when sendOutput exists,
 * if no sendOutput exists, the item is dropped).
 * If a receive method returns null, nothing is send to sendOutput.
 * If a receive method returns void, the takeInput is send to the sendOutput (if there was takeInput, if
 * there is no takeInput, nothing happens).
 * When no matching receive method exists, the inputitem is send to the sendOutput. If there was no takeInput, or
 * there is no sendOutput, nothing happens.
 * <p/>
 * <p/>
 * parametrization is quite useless if you also want to transport events
 * <p/>
 * <h1>Stopping a processor</h1>
 * A process can signal the processor to processorWantsToStop processing. At the moment it can be done by letting the
 * receive method return an object that implements the ProcessDeath message. The message is send to the sendOutput
 * (if this is available) and then false is returned by the {@link #once()} method. I'm also thinking about
 * different ways to processorWantsToStop processors, an possible alternative would be throwing some sort of exception.
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
 * -how to get data (doesn't know the location, but also no blocking)
 * -how to get rid of it (doesn't know the location, but also no blocking).
 * blocking prevents testability.
 * why should the process not know about the channels?
 * -dispatching on type of data
 * -logging
 * -tracing
 * <p/>
 * If a process returns an iterator, this iterator could be seen as a lazy collection (a collection where the
 * elements don't need to exist from the start).
 *
 * @author Peter Veentjer.
 */
public class StandardProcessor implements Processor {
    private final Object[] processes;
    private final InputChannel input;
    private final OutputChannel output;
    private final Step[] steps;

    //todo: default another policy
    private volatile ErrorPolicy policy = new PropagatePolicy();
    private volatile Dispatcher dispatcher = new StandardDispatcher();
    private volatile StopStrategy stopStrategy = new StandardStopStrategy();

    /**
     * Creates a sink processor: a sink processor is processor
     * that runs a sink process (a process that consumes data).
     *
     * @param input
     * @param process
     * @throws NullPointerException if process is null.
     */
    public StandardProcessor(InputChannel input, Object process) {
        this(input, process, null);
    }

    /**
     * Creates a
     *
     * @param input
     * @param processes
     */
    public StandardProcessor(InputChannel input, Object[] processes) {
        this(input, processes, null);
    }

    /**
     * Creates a source processor: a processor that runs a
     * source process (a process that produces data).
     *
     * @param process
     * @param output
     * @throws NullPointerException if process is null.
     */
    public StandardProcessor(Object process, OutputChannel output) {
        this(null, process, output);
    }

    /**
     * @param processes
     * @param output
     */
    public StandardProcessor(Object[] processes, OutputChannel output) {
        this(null, processes, output);
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
    public StandardProcessor(InputChannel input, Object process, OutputChannel output) {
        this(input, new Object[]{process}, output);
    }

    /**
     * @param input
     * @param processes
     * @param output
     */
    public StandardProcessor(InputChannel input, Object[] processes, OutputChannel output) {
        if (processes == null) throw new NullPointerException();
        this.processes = processes;
        this.input = input;
        this.output = output;
        steps = createSteps();
    }

    private Step[] createSteps() {
        List<Step> stepLists = new LinkedList<Step>();
        for (Object process : processes)
            stepLists.add(new ProcessStep(process));
        if (output != null)
            stepLists.add(new OutputStep());
        return stepLists.toArray(new Step[stepLists.size()]);
    }

    /**
     * Returns the process this StandardProcessor is executing. The returned value
     * will always be a non null value.
     *
     * @return the process that is being executed.
     */
    public Object[] getProcesses() {
        return processes;
    }

    /**
     * Returns the InputChannel where this StandardProcessor receives its takeInput from.
     * The returned value is allowed to be null (indicating a source processor).
     *
     * @return
     */
    public InputChannel getInput() {
        return input;
    }

    /**
     * Returns the OutputChannel where this StandardProcessor sends its sendOutput to.
     * The returned value is allowed to be null (indicating a sink processor)
     *
     * @return
     */
    public OutputChannel getOutput() {
        return output;
    }

    /**
     * Returns the Dispatcher.
     *
     * @return
     */
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Sets the Dispatcher this Processor uses to dispach methods on the processes.
     *
     * @param dispatcher
     * @throws NullPointerException if dispatcher is null.
     */
    public void setDispatcher(Dispatcher dispatcher) {
        if (dispatcher == null) throw new NullPointerException();
        this.dispatcher = dispatcher;
    }

    /**
     * Returns the ErrorPolicy this StandardProcessor uses to deal with exceptions. The
     * returned value will always be a non null reference.
     *
     * @return the ErrorPolicy this StandardProcessor usess to deal with exceptions.
     */
    public ErrorPolicy getPolicy() {
        return policy;
    }

    /**
     * Sets the ErrorPolicy this StandardProcessor uses to deal with exceptions.
     *
     * @param policy the new ErrorPolicy
     * @throws NullPointerException if policy is null.
     */
    public void setPolicy(ErrorPolicy policy) {
        if (policy == null) throw new NullPointerException();
        this.policy = policy;
    }

    public boolean evaluateSteps(int stepIndex, Object arg) throws Exception {
        if (stepIndex == steps.length)
            return true;

        boolean again = true;
        Step step = steps[stepIndex];
        for (Iterator it = toIterator(arg); it.hasNext();) {
            arg = it.next();
            Object result = step.evaluate(arg);
            if (result != null) {
                if (stopStrategy.stop(result))
                    again = false;

                evaluateSteps(stepIndex + 1, result);
            }
        }
        return again;
    }

    public Iterator toIterator(Object arg) {
        if (arg instanceof Iterator)
            return (Iterator) arg;

        LinkedList<Object> l = new LinkedList<Object>();
        l.add(arg);
        return l.iterator();
    }

    public boolean once() throws Exception {
        Object in = takeInput();
        return evaluateSteps(0, in);
    }

    /**
     * Takes a message from the takeInput. If no takeInput is available, a VoidValue
     * is returned. This call blocks if input is not null and no input is
     * available.
     *
     * @return the taken message (always a not null value).
     * @throws InterruptedException if taking of the takeInput was interrupted.
     */
    private Object takeInput() throws InterruptedException {
        return input == null ? VoidValue.INSTANCE : input.take();
    }

    @Override
    public String toString() {
        return format("StandardProcessor(%s)", Arrays.asList(processes));
    }

    private interface Step {
        Object evaluate(Object arg) throws Exception;
    }

    private class ProcessStep implements Step {
        private final Object process;

        public ProcessStep(Object process) {
            if (process == null) throw new NullPointerException();
            this.process = process;
        }

        public Object evaluate(Object arg) throws Exception {
            assert arg != null;
            Object returned = dispatch(arg);
            return determineInputNextStep(returned, arg);
        }

        /**
         * Calls a method on the process.
         *
         * @param arg the actual arguments of the method to call.
         * @return the result of the method call on the process.
         * @throws Exception the exception the method call causes
         */
        private Object dispatch(Object arg) throws Exception {
            assert arg != null;
            try {
                //todo: hack, void value should be dealth with arg the dispatcher
                if (arg instanceof VoidValue)
                    return dispatcher.dispatch(process);
                else
                    return dispatcher.dispatch(process, arg);
            } catch (NoSuchMethodException ex) {
                //todo: this would also be an excellent place to add some logging

                //if no method was found, we can return void. This make a missing
                //method the same as a non missing method that returns void.
                return VoidValue.INSTANCE;
            } catch (InvocationTargetException ex) {
                Throwable target = ex.getTargetException();
                if (!(target instanceof Exception)) {
                    //we are not going to deal with it
                    //todo: needs to be cast to error, etc
                    throw new RuntimeException(target);
                }

                Exception targetException = (Exception) target;
                //todo: add logging?

                return policy.handle(targetException, arg);
            }
        }

        private Object determineInputNextStep(Object returned, Object arg) {
            //if returned was void, we should use the arg
            return returned instanceof VoidValue ? arg : returned;
        }
    }

    private class OutputStep implements Step {

        OutputStep() {
            //if no output is available, this step should not have been created
            if (output == null) throw new NullPointerException();
        }

        public Object evaluate(Object arg) throws InterruptedException {
            assert arg != null;
            sendOutput(arg);
            return null;
        }

        /**
         * Sends the sendOutput to the sendOutput. If no sendOutput is available, this
         * call is ignored. If the msg is null or void, this call is ignored.
         *
         * @param msg the message to place on the sendOutput.
         * @throws InterruptedException if placement is interrupted.
         */
        private void sendOutput(Object msg) throws InterruptedException {
            //if out is void, we should not output it
            if (msg instanceof VoidValue)
                return;

            output.put(msg);
        }
    }
}
