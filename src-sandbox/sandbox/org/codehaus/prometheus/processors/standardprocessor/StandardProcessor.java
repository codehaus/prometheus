package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.channels.InputChannel;
import org.codehaus.prometheus.channels.OutputChannel;
import org.codehaus.prometheus.processors.Dispatcher;
import org.codehaus.prometheus.processors.Processor;
import org.codehaus.prometheus.processors.StandardDispatcher;
import org.codehaus.prometheus.processors.VoidValue;
import static org.codehaus.prometheus.processors.VoidValue.isVoid;

import static java.lang.String.format;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p/>
 * A {@link org.codehaus.prometheus.processors.Processor} that executes a piped process. The
 * takeInput is taken from an {@link InputChannel} and the sendOutput is placed on an
 * {@Link OutputChannel}. If there is no InputChannel, it is a source process. If there is no
 * sendOutput, it is a sink process.
 * </p>
 * <h1>Dispatching</h1>
 * <p/>
 * Dispatching can be customized by injecting a custom dispatcher. The StandardDispatcher in
 * the current state still has some limitation (it isn't able to match on interfaces for
 * example).
 * </p>
 * <p/>
 * Bahaviour (that is fixed):
 * </p>
 * <ol>
 * <li>
 * If a receive method returns a non null reference, this reference is send to the sendOutput
 * (when sendOutput exists or send to the next process. If both don't exist, the message is
 * dropped.
 * </li>
 * <li>
 * If a receive method returns null, this indicates that the message can be seen as lost. This
 * means that the following process is not called (if such a process exist) and that nothing
 * is send to the output (if an output is available).
 * </li>
 * <li>
 * If a receive method returns null, this indicates that the message can be seen as lost. This
 * means that the following process is not called (if such a process exist) and that nothing
 * is send to the output (if an output is available).
 * </li>
 * <li>
 * When no matching receive method exists, the inputitem is send to the sendOutput. If there was
 * no takeInput, or there is no sendOutput, nothing happens.
 * </li>
 * </ol>
 * <h1>Why nothing is parametrized</h1>
 * <p/>
 * parametrization is quite useless if you also want to transport events
 * </p>
 * <h1>Stopping a processor</h1>
 * <p/>
 * A process can signal the processor to processorWantsToStop processing. This can be done by
 * injecting a custom {@link StopPolicy}. I'm currently still thinking about the exact behaviour.
 * When should the stop
 * policy be asked? If there are multiple processes:
 * -should it ask in between every process?
 * -should it ask only after the last process?
 * And what about iterators? If a process returns an iterator, should the last returned value
 * be used? Or should it be checked on all returned values?
 * <p/>
 * There are 2 things to think about:
 * 1) when to check if there is a sequence of processes
 * 2) what to do when an one items in an interator contains. the problem at the moment with an
 * iterator is that it doesn't stop iterating. Maybe this is desired behaviour? If an iterator
 * has problems, it already has a way to prevent being used for other tasks, because it could
 * return false on the 'next'.
 * <p/>
 * </p>
 * <h1>Variable number or processes</h1>
 * <p/>
 * It is possible to chain an arbitrary number of process in the processor. The output of the
 * previousPosition process will be used as input for the next process (so the dispatcher for more
 * information). The minimum number of processes is 0. Using a chain of processes can be seen
 * as the 'classic' single threaded sequential chain of calls. Unless multiple threads are
 * calling the evaluate method, in that case the same chain will be executed concurrently. A single
 * chain of execution will always be executed by a single thread that remains constant for all
 * steps in that execution. So taking of input, running the chain of processes and output will
 * be done by the same thread.
 * </p>
 * <h1>Stateless vs statefull processes</h1>
 * <p/>
 * A process is allowed to have state. If such a stateful process is executed by multiple
 * threads, synchronization needs to be added. In most cases it is better to let a stateful
 * process be executed by a single thread. In other environments (like erlang) you normally
 * have a process per thread. Maybe this functionality is going to be added in the future.
 * </p>
 * <h1>Ordering constraint</h1>
 * <p/>
 * In some cases you want your messages to be ordered. If only a single thread is used, and
 * standard piped processes, message will remain in the correct order. If multiple threads are
 * used, or custom processes are used, messages could get out of order. This can be prevented
 * by using a {@link org.codehaus.prometheus.processors.processes.ResequenceProcess}.
 * </p>
 * <h1>Exception handling</h1>
 * <p/>
 * All exceptions are tracked when a receive method is called. The behaviour can be influenced
 * by using one of the predefined ErrorPolicies, or by creating a custom one.
 * </p>
 * <h1>Performance</h1>
 * <p/>
 * No performance numbers of the overhead of the processor (dequeueing, execution logic,
 * dispatching, queueing) functionality are available. The current focus is for course
 * grained processes (so processes with a 'long' execution time) and not on very fine grained
 * processes (processes with a very short execution time). I don't have any numbers on the
 * length of this period.
 * </p>
 * main goal: only core responsibily:
 * -transformation of a message (piped process)
 * -----------tranforming the message itself
 * -----------replacing the message by a different one
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
 * If a process returns an iterator, this iterator could be seen as a lazy collection (a collection
 * where the elements don't need to exist from the spawned_start).
 * <h2>Continuations</h2>
 * <p>
 * The StandardProcessor keeps a 'continuation' of the current process and element in the iterator
 * in memory. A single continuation can be executed by multiple threads of a period of time, but will
 * be executed by at most a single thread at any moment in time. Continuations are stored in a queue
 * between processing, and so will be selected fair (so starvation).
 * </p>
 * <p>
 * The advantage of storing the continuations on a queue instead of a threadlocal is that the execution
 * is not bound to a single thread. If that thread doesn't call the once method anymore, you want a
 * different thread to take over. It can happen that threads stop calling: when the poolsize of the
 * ThreadPoolRepeater is decreased for example.
 * </p>
 * todo:
 * improve error handling:
 * -iterator is not protected very well
 * -take of item from input is not protected
 * -placement of item on outtake is not well protected
 * -calls to the error handler are without protection
 *
 * @author Peter Veentjer.
 */
public class StandardProcessor implements Processor {
    private final List processes;
    private final InputChannel input;
    private final OutputChannel output;
    private final ChainElement[] chainElements;
    private final BlockingQueue<Stack<ChainFrame>> callstackqueue = new LinkedBlockingQueue<Stack<ChainFrame>>();
        
    //todo: default another errorPolicy
    private volatile ErrorPolicy errorPolicy = new Propagate_ErrorPolicy();
    private volatile Dispatcher dispatcher = new StandardDispatcher();
    private volatile StopPolicy stopPolicy = new DefaultStopPolicy();


    /**
     * Creates a sink processor: a sink processor is processor that runs a sink process
     * (a process that consumes data).
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
     * Creates a source processor: a processor that runs a source process (a process
     * that produces data).
     *
     * @param process
     * @param output
     * @throws NullPointerException if process is null.
     */
    public StandardProcessor(Object process, OutputChannel output) {
        this(null, process, output);
    }

    /**
     * Creates a source processor.
     *
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
     * Creates a sink process
     *
     * @param input     the InputChannel where the messages are retrieved from
     * @param processes a list of processes.
     * @throws NullPointerException if processes is null
     */
    public StandardProcessor(InputChannel input, List processes) {
        this(input, processes, null);
    }

    /**
     * Creates a source process
     *
     * @param processes
     * @param output
     * @throws NullPointerException if processes is null
     */
    public StandardProcessor(List processes, OutputChannel output) {
        this(null, processes, output);
    }

    /**
     * Creates a piped process.
     *
     * @param input
     * @param processes
     * @param output
     * @throws NullPointerException if processes is null
     */
    public StandardProcessor(InputChannel input, List processes, OutputChannel output) {
        this(input, processes.toArray(new Object[processes.size()]), output);
    }

    /**
     * Creates a piped process.
     *
     * @param input
     * @param processes
     * @param output
     */
    public StandardProcessor(InputChannel input, Object[] processes, OutputChannel output) {
        if (processes == null) throw new NullPointerException();
        this.processes = unmodifiableList(asList(processes));
        this.input = input;
        this.output = output;
        chainElements = createSteps();
    }

    private ChainElement[] createSteps() {
        List<ChainElement> chainElementLists = new LinkedList<ChainElement>();
        for (Object process : processes)
            chainElementLists.add(new ProcessChainElement(process));
        if (output != null)
            chainElementLists.add(new OutputChainElement());
        return chainElementLists.toArray(new ChainElement[chainElementLists.size()]);
    }

    /**
     * Returns the processes this StandardProcessor is executing. The returned value
     * will always be a non <tt>null</tt> value, but it could be empty. The returned list is
     * not modifiable.
     *
     * @return the process that is being executed.
     */
    public List getProcesses() {
        return processes;
    }

    /**
     * Returns the InputChannel where this StandardProcessor receives its takeInput from.
     * The returned value is allowed to be <tt>null</tt> (indicating a source processor).
     *
     * @return the InputChannel this StandardProcessor uses.
     */
    public InputChannel getInput() {
        return input;
    }

    /**
     * Returns the OutputChannel where this StandardProcessor sends its sendOutput to.
     * The returned value is allowed to be <tt>null</tt> (indicating a sink processor)
     *
     * @return the OutputChannel this StandardProcessor uses.
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
     * returned value will always be a non <tt>null</tt> reference.
     *
     * @return the ErrorPolicy this StandardProcessor usess to deal with exceptions.
     */
    public ErrorPolicy getErrorPolicy() {
        return errorPolicy;
    }

    /**
     * Sets the ErrorPolicy this StandardProcessor uses to deal with exceptions.
     *
     * @param errorPolicy the new ErrorPolicy
     * @throws NullPointerException if errorPolicy is <tt>null</tt>.
     */
    public void setErrorPolicy(ErrorPolicy errorPolicy) {
        if (errorPolicy == null) throw new NullPointerException();
        this.errorPolicy = errorPolicy;
    }

    /**
     * @return
     */
    public StopPolicy getStopPolicy() {
        return stopPolicy;
    }

    /**
     * Sets the policy used for stopping this Processor.
     *
     * @param stopPolicy
     * @throws NullPointerException if stopPolicy is <tt>null</tt>.
     */
    public void setStopPolicy(StopPolicy stopPolicy) {
        if (stopPolicy == null) throw new NullPointerException();
        this.stopPolicy = stopPolicy;
    }

    /**
     * Calls the next on the iterator and adds exception handling when this fails.
     *
     * @param it the Iterator that used
     * @return the value retrieved from the next, or when an error occurs, the value returned
     *         from the errorPolicy
     * @throws Exception the errorPolicy could decide to throw the exception that is caused
     */
    private Object protectedNext(Iterator it) throws Exception {
        try {
            if (!it.hasNext())
                return null;

            return it.next();
        } catch (Exception ex) {
            //todo: is this the correct call to the errorhandler? What about input arguments?
            return errorPolicy.handleReceiveError(ex, VoidValue.INSTANCE);
        }
    }


    /**
     * Transforms an argument to an iterator. If the argument already is an iterator,
     * the argument is returned. If the argument is not an iterator, it will be wrapped
     * in an iterator containing that argument. This method is used for the behavior
     * with single values and iterators more consistent because you are always working
     * with an iterator.
     *
     * @param arg
     * @return an Iterator
     */
    private Iterator asIterator(Object arg) {
        if (arg instanceof Iterator)
            return (Iterator) arg;

        //in the future a more lightweight iterator could be used
        LinkedList<Object> l = new LinkedList<Object>();
        l.add(arg);
        return l.iterator();
    }

    //public boolean evaluate() throws Exception {
    //    Object in = takeInput();
    //    return evaluateSteps(0, in);
    //}

    public boolean once() throws Exception {

        Stack<ChainFrame> framestack = callstackqueue.poll();
        if (framestack == null) {
            framestack = new Stack<ChainFrame>();
        }

        boolean solutionFound = false;
        try {
            if (once(framestack)) {
                solutionFound = true;
                return true;
            }

            return true;
        } finally {
            if (solutionFound)
                callstackqueue.put(framestack);
        }
    }

    private boolean once(Stack<ChainFrame> framestack) throws Exception {
        if (framestack.isEmpty()) {
            Object in = takeInput();
            if (chainElements.length == 0)
                return true;

            ChainFrame frame = new ChainFrame(chainElements[0], asIterator(in));
            framestack.push(frame);
        }

        do {
            ChainFrame frame = framestack.peek();
            Object result = frame.evaluate();

            if (result == null) {
                framestack.pop();
            } else {
                int framecount = framestack.size();
                if (framecount == chainElements.length)
                    return true;

                ChainFrame newFrame = new ChainFrame(chainElements[framecount], asIterator(result));
                framestack.push(newFrame);

            }
        } while (!framestack.isEmpty());

        return false;
    }

    /**
     * Takes a message from the takeInput. If no takeInput is available, a VoidValue
     * is returned. This call blocks if input is not null and no input is
     * available.
     *
     * @return the taken message (always a not null value).
     * @throws InterruptedException if blocking for input was interrupted.
     */
    private Object takeInput() throws InterruptedException {
        //todo: what about error handling around the input.take()
        return input == null ? VoidValue.INSTANCE : input.take();
    }

    @Override
    public String toString() {
        return format("StandardProcessor(%s)", Arrays.asList(processes));
    }

    //object is stored in a threadlocal.
    private class ChainFrame {
        private final Iterator iterator;
        private final ChainElement chainElement;

        public ChainFrame(ChainElement chainElement, Iterator iterator) {
            this.chainElement = chainElement;
            this.iterator = iterator;
        }

        //true indicates that the complete chain execution was successful, false otherwise.
        Object evaluate() throws Exception {
            Object arg = protectedNext(iterator);
            if (arg == null)
                return null;

            return chainElement.evaluate(arg);
        }

        public String toString() {
            return format("chainframe(%s)", chainElement);
        }
    }

    private interface ChainElement {
        Object evaluate(Object arg) throws Exception;
    }

    private class ProcessChainElement implements ChainElement {
        private final Object process;

        public ProcessChainElement(Object process) {
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
         * @return the Result of the method call on the process.
         * @throws Exception the exception the method call causes
         */
        private Object dispatch(Object arg) throws Exception {
            assert arg != null;
            try {
                return dispatcher.dispatch(process, arg);
            } catch (NoSuchMethodException ex) {
                //todo: this would also be an excellent place to add some logging

                //if no method was found, we can return void. This makes the behaviour
                //of a missing method the same as a method that returns void.
                return VoidValue.INSTANCE;
            } catch (InvocationTargetException ex) {
                Throwable causeThrowable = ex.getCause();
                //errors should not be caught
                if (causeThrowable instanceof Error)
                    throw (Error) causeThrowable;

                //the following is very unlikely to happen because throwable's normally are
                //exceptions or errors.
                if (!(causeThrowable instanceof Exception))
                    throw new RuntimeException("causeThrowable is not an Error or Exception", causeThrowable);

                Exception targetException = (Exception) causeThrowable;
                return errorPolicy.handleReceiveError(targetException, arg);
            }
        }

        private Object determineInputNextStep(Object returned, Object arg) {
            //if returned was void, we should use the arg
            return isVoid(returned) ? arg : returned;
        }

        @Override
        public String toString() {
            return "processchainelement";
        }
    }

    private class OutputChainElement implements ChainElement {

        OutputChainElement() {
            //if no output is available, this step should not have been created
            assert output != null;
        }

        public Object evaluate(Object arg) throws InterruptedException {
            assert arg != null;

            //if out is void, we should not output it
            if (!isVoid(arg))
                sendOutput(arg);

            return VoidValue.INSTANCE;
        }

        /**
         * Sends the sendOutput to the sendOutput. If no sendOutput is available, this
         * call is ignored. If the msg is null or void, this call is ignored.
         *
         * @param msg the message to place on the sendOutput.
         * @throws InterruptedException if placement is interrupted.
         */
        private void sendOutput(Object msg) throws InterruptedException {
            //should this call be protected? If the output fails, there is no
            //reason why an error message can be outputted.
            output.put(msg);
        }

        @Override
        public String toString() {
            return "outputchainelement";
        }
    }
}
