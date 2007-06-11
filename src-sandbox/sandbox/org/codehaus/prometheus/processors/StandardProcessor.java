package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.channels.InputChannel;
import org.codehaus.prometheus.channels.OutputChannel;

import java.lang.reflect.InvocationTargetException;


/**
 * A {@link Processor} that executes a piped process. The input is taken from an {@link InputChannel}
 * and the output is placed on an {@Link OutputChannel}.
 * <p/>
 * parametrization is quite useless if you also want to transport events
 *
 * @author Peter Veentjer.
 */
public class StandardProcessor<P> implements Processor {
    private final P process;
    private final InputChannel input;
    private final OutputChannel output;
    private volatile Dispatcher dispatcher = new StandardDispatcher();

    /**
     * Creates a sink processor.
     *
     * @param input
     * @param process
     * @throws NullPointerException if process is null.
     */
    public StandardProcessor(InputChannel input, P process){
        this(process,input,null);
    }

    /**
     * Creates a source processor.
     * 
     * @param process
     * @param output
     * @throws NullPointerException if process is null.
     */
    public StandardProcessor(P process, OutputChannel output){
        this(process,null,output);
    }

    /**
     *
     * @param process
     * @param input
     * @param output
     * @throws NullPointerException if process is null.
     *
     * todo: process should be the argument in the middle
     */
    public StandardProcessor(P process, InputChannel input, OutputChannel output) {
        if (process == null) throw new NullPointerException();
        this.process = process;
        this.input = input;
        this.output = output;
    }

    public P getProcess() {
        return process;
    }

    public InputChannel getInput() {
        return input;
    }

    public OutputChannel getOutput() {
        return output;
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

        out = determineOutput(out,in);
        output(out);
        return !(out instanceof ProcessDeath);
    }

    private Object dispatch(Object... in) throws Exception {
        try {
            return dispatcher.dispatch(process, in);
        } catch (NoSuchMethodException ex) {
            return Void.INSTANCE;
        }catch(InvocationTargetException ex){
            if(ex.getCause() instanceof Exception)
                throw (Exception)ex.getCause();
            else//todo
                throw new RuntimeException(ex.getCause());
        } 
    }

    private Object determineOutput(Object out, Object in){
        //if out was void, we should use the in
        if (Void.INSTANCE.equals(out))
            out = in;

        return out;
    }

    private void output(Object out) throws InterruptedException {
        //if there is no output, we are finishes (the out is dropped)
        if (output == null)
            return;

        //if out is null we should not post a result
        if(out == null)
            return;

        output.put(out);
    }
}
