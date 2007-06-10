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
            in = null;
            out = dispatch();
        } else {
            in = input.take();
            out = dispatch(in);
        }

        output(out, in);
        return true;
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

    private void output(Object out, Object in) throws InterruptedException {
        //if there is no output, we are finishes (the out is dropped)
        if (output == null)
            return;

        //if out was void, we should use the in
        if (Void.INSTANCE.equals(out))
            out = in;

        //a null indicates that no value is placed
        if (out == null)
            return;

        output.put(out);
    }
}
