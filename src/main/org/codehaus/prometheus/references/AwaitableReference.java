/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A synchronization stone that stores a reference. It allows a master thread to send references to
 * zero or more slave threads. If a slave thread tries to take a reference but no reference is
 * available, the slave blocks.
 * <p/>
 * Depending on the implementation, the following things are possible:
 * <ol>
 * <li>multiple slaves have taken the same reference</li>
 * <li>different references are lend concurrently to multiple slaves</li>
 * </ol>
 * <p/>
 * todo:
 * example
 * <p/>
 * <dt><b>AwaitableReference vs LendableReference</b></dt>
 * <dd>
 * The AwaitableReference has no concept of taking values back (a value that is taken can't to be
 * taken back). See the {@link LendableReference} if this is
 * required.
 * </dd>
 * <p/>
 * <dt><b>AwaitableReference vs Condition</b></dt>
 * <dd>
 * </dd>
 * <p/>
 * <dt><b>AwaitableReference vs BlockingQueue</b></dt>
 * <dd>
 * The difference between a {@link java.util.concurrent.BlockingQueue} and an AwaitableReference,
 * is that items normally are removed from the BlockingQueue when it is taken. With an
 * AwaitableReference the item is allowed to stay in the AwaitableReference when it is taken. An
 * AwaitableReference has a capacity of one at most, the BlockingQueue has an unlimited no maximum
 * capacity. One could modify a BlockingQueue so it behaves like an AwaitableReference, but this
 * doesn't make the usage much clearer. That is why I decides to create the AwaitableReference.
 * </dd>
 * <p/>
 * <dt><b>AwaitableReference vs SynchronousQueue</b></dt>
 * <dd>
 * The difference between a {@link java.util.concurrent.SynchronousQueue} and an AwaitableReference,
 * is the item is removed from the BlockingQueue and that a put can only take place when an take
 * is waiting. With the AwaitableReference, a put can take place even if there are no takes. The
 * AwaitableReference has an internal capacity one at most, the SynchronousQueue has no internal
 * capacity at all.
 * </dd>
 * <p/>
 * <dt><b>AwaitableReference vs Exchanger</b></dt>
 * <dd>
 * The difference between an {@link java.util.concurrent.Exchanger} and an AwaitableReference is
 * that an Exchanger exchanges references between threads. A reference can be exchanged from thread1
 * to thread2 only if thread2 has another reference to be transfer (this reference is transfered
 * from thread2 to thread1).With the AwaitableReference a reference only exchanged from the master
 * thread to zero or more slave threads. A put without waiting takes doesn't block unlike the
 * Exchanger. So an Exchanger is bidirectional and an AwaitableReference is unidirectional.
 * </dd>
 * <p/>
 * <dt><b>AwaitableReference vs Future</b></dt>
 * <dd>
 * The difference between a {@link java.util.concurrent.Future} and an AwaitableReference is that a
 * Future is a single shot mechanism (aka latch) that gets in a final state as soon as a reference
 * (the result of a asynchronous action) is available. The AwaitableReference has no final state,
 * and the reference it contains, can change multiple times. Another difference is that a Future
 * only contains functionality for waiting for completion, and not for actions to complete the
 * Future.
 * </dd>
 * <p/>
 * <dt><b>Save handoff</b></dt>
 * <dd>
 * The AwaitableReference can be used as a save handoff structure; this means that objects with
 * visibility problems can safely be exchanged by threads.
 * </dd>
 *
 * @author Peter Veentjer
 */
public interface AwaitableReference<E> {

    /**
     * Returns <tt>true</tt> if a take is possible, <tt>false</tt> otherwise. This value could be
     * stale at the moment it is received because the reference could have been changed.
     *
     * @return <tt>true</tt> if a take is possible, <tt>false</tt> otherwise.
     */
    boolean isTakePossible();

    /**
     * Takes the reference this AwaitableReference contains. If the current reference is
     * <tt>null</tt>, this method blocks until:
     * <ol>
     * <li>a non <tt>null</tt> reference comes available</li>
     * <li>the calling thread is interrupted</li>
     * </ol>
     *
     * @return the current reference. This value will never be <tt>null</tt>.
     * @throws InterruptedException if the calling thread is interrupted while waiting.
     */
    E take() throws InterruptedException;

    /**
     * Tries to take the current reference. If no reference is available, <tt>null</tt> is returned.
     * <p/>
     * This call could do some locking to prevent isolation problems, but the locks are not hold for
     * a long time.
     * <p/>
     * The interrupt status is not influenced by this method.
     *
     * @return the current reference, or <tt>null</tt> if no reference is available.
     */
    E tryTake();

    /**
     * Tries to take the reference this AwaitableReference contains. If the current reference is
     * <tt>null</tt>, this method blocks until:
     * <ol>
     * <li>a non <tt>null</tt> reference comes available</li>
     * <li>the calling thread is interrupted</li>
     * <li>a timeout occurs</li>
     * </ol>
     * <p/>
     * If the timeout is smaller than zero, a TimeoutException is
     * thrown.
     * todo: nul timeouts
     *
     * @param timeout how long to wait before giving up in units of <tt>unit</tt>.
     * @param unit    a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt>
     *                parameter.
     * @return the current reference. This will never be <tt>null</tt>.
     * @throws NullPointerException if unit is <tt>null</tt>.
     * @throws InterruptedException if the calling thread is interrupted
     * @throws TimeoutException     if the timeout expires.
     */
    E tryTake(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

    /**
     * Puts a new reference in this AwaitableReference.
     *
     * @param newRef the new reference. This new reference is allowed to be <tt>null</tt>.
     * @return the old reference, could be <tt>null</tt> if no old reference was available.
     * @throws InterruptedException if the calling thread is interrupted
     */
    E put(E newRef) throws InterruptedException;
   
    /**
     * Tries to put a new reference in this AwaitableReference.
     * <p/>
     * todo: nul timeouts.
     *
     * @param newRef  the new reference. This new reference is allowed to be <tt>null</tt>.
     * @param timeout how long to wait before giving up in units of <tt>unit</tt>.
     * @param unit    a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt>
     *                parameter.
     * @return the old reference, could be <tt>null</tt>.
     * @throws InterruptedException if the calling thread is interrupted.
     * @throws TimeoutException     if a timeout occurrs.
     */
    E tryPut(E newRef, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

    /**
     * Returns the current reference.
     * <p/>
     * This call could do some locking to prevent isolation problems, but the locks are not hold for
     * a long time.
     * <p/>
     * The interrupt status is not influenced by this method.
     *
     * @return the current reference, this could be <tt>null</tt>.
     */
    E peek();
}