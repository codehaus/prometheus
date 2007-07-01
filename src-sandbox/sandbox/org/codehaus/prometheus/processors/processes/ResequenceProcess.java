package org.codehaus.prometheus.processors.processes;

import org.codehaus.prometheus.processors.VoidValue;
import org.codehaus.prometheus.resequencer.IllegalSequenceException;
import org.codehaus.prometheus.resequencer.OutOfSpaceException;
import org.codehaus.prometheus.resequencer.Sequenceable;

import static java.lang.String.format;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A Process that is responsible for reordering out of order messages. Messages that
 * can't be returned because a preceding message has not been received, are stored in
 * a buffer.
 * <p/>
 * The size of this buffer can be set when constructing a new instance, but not all
 * space will be reserved in the beginning. The buffer can grow (to the maxium
 * capacity) and shrink. If the buffer has reached its capacity, OutOfSpaceException
 * is thrown when a message is retrieved that isn't the one that is waited for. No
 * blocking is done at the moment. I'm thinking about adding this feature in the
 * future. The problem with blocking is that there is a chance for deadlocks: if
 * all processing threads are waiting, no message can be processed and the message
 * we are waiting for, can't arive. So a timed wait would be the safest solution. At
 * the moment it is best to use a large buffer.
 * <p/>
 * If an already returned message is received again (a corrupt sequence can also be
 * the cause), an IllegalSequenceException is thrown.
 * <p/>
 * See also the Resequencer in "Enterprise Integration Patterns".
 * <p/>
 * This object is threadsafe. But when multiple threads are using this process,
 * they are handed out messages in the correct order, but the order could get lost as
 * soon as the messages are processed. So be very careful using the ResequenceProcess
 * in a multithreaded environment because they behaviour is propably is not something
 * you want.
 * <p/>
 * idea: working with a maximum return size of the iterator. This prevents that a
 * single thread needs to deal with the complete iterator. Maybe it is even better
 * not to return an iterator at all and send back a single element at most.
 * <p/>
 * todo:
 * lastIndex should be something that can be customized by a constructor.
 * This makes it possible to start from a different value than 0.
 *
 * @author Peter Veentjer.
 */
public class ResequenceProcess {

    private volatile long lastIndex = -1;
    private final Map<Long, Object> buffer = new HashMap<Long, Object>();
    private final int capacity;

    /**
     * Creates a new ResequenceProcess with no bounds on the capacity of the
     * buffer.
     */
    public ResequenceProcess() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Creates a new ResequenceProcess with the given capacity.
     *
     * @param capacity the maximum capacity of the buffer.
     * @throws IllegalArgumentException if capacity smaller than 0.
     */
    public ResequenceProcess(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException();
        this.capacity = capacity;
    }

    /**
     * Returns the current amount of messages in the buffer. The value could
     * be stale as soon as it is received.
     *
     * @return the current amount of messages in the buffer.
     */
    public synchronized int getBufferSize() {
        return buffer.size();
    }

    /**
     * Returns the index of the last returned message. The value could be stale
     * as soon as it is received.
     *
     * @return the index of the last returned message.
     */
    public long getLastReturnedIndex() {
        return lastIndex;
    }

    /**
     * Returns the maxium capacity of the buffer. Integer.MAX_VALUE effectively means
     * that no capacity has been set.
     *
     * @return the capacity of the buffer.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * @param msg
     * @return
     * @throws IllegalSequenceException if a msg is received that already has been returned.
     * @throws OutOfSpaceException      if this ResequenceProcess doesn't have enough storage space
     *                                  to store this message.
     */
    public Object receive(Object msg) {
        //at the moment pattern matching on interfaces is not possible. Else the receive(Object)
        //could be replaced by receive(Sequenceable).
        if (!isSequenceable(msg))
            return VoidValue.INSTANCE;

        long index = ((Sequenceable) msg).getIndex();

        synchronized (this) {
            if (outOfSequence(index)) {
                String errormsg = format("msg %s with index %s already has been returned", msg, index);
                throw new IllegalSequenceException(errormsg);
            } else if (thisIsTheOne(index)) {
                //the item being added is the item we are waiting for. Now
                //check if there are more items that can be removed

                lastIndex = index;
                List objectList = new LinkedList();
                objectList.add(msg);
                for (index += 1; true; index++) {
                    Object value = buffer.remove(index);
                    if (value != null) {
                        objectList.add(value);
                    } else {
                        lastIndex = index - 1;
                        return objectList.iterator();
                    }
                }
            } else {
                //this message
                //add the msg to the cache
                if (buffer.size() == capacity)
                    throw new OutOfSpaceException();

                buffer.put(index, msg);
                return null;
            }
        }
    }

    private boolean thisIsTheOne(long index) {
        return index == lastIndex + 1;
    }

    private boolean outOfSequence(long index) {
        return index <= lastIndex;
    }

    private boolean isSequenceable(Object msg) {
        return (msg instanceof Sequenceable);
    }
}
