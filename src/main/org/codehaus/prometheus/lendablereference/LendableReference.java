/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

import org.codehaus.prometheus.awaitablereference.AwaitableReference;

/**
 * An {@link org.codehaus.prometheus.awaitablereference.AwaitableReference} that adds the concept of
 * taking back the lend reference.
 * <p/>
 * todo:synchronization stone that makes it easy to exchange a reference from one thread to zero or
 * more threads. A LendableReference stores a reference: if a thread wants to take that reference,
 * it takes that reference, if no reference is available, it blocks until a reference comes
 * available. After it is finished with the reference, the reference needs to be taken back.
 * <p/>
 * Idiom:
 * <pre>
 * Integer someref = lendableRef.take();
 * try{
 *     ..do something with the reference
 * }finally{
 *    someRef.takeback(someref);
 * }
 * </pre>
 * <p/>
 * Depending on the implementation, it could be that multiple Threads are lending the same reference
 * at any moment. For more information see the {@link StrictLendableReference} and the
 * {@link RelaxedLendableReference}.
 * <p/>
 * Depending on the implementation, a new value can be set under certain conditions. In case of the
 * {@link StrictLendableReference} a new reference can only be set, if all lending threads have
 * returned the lend reference.
 * <p/>
 * The reference check for takeback is done based on equals method. So you can send back a different
 * object, as long as the equals says they are equal.
 * <p/>
 * A LendableReference allows that a lend reference is taken back by a different thread than took
 * the reference.
 * <p/>
 * A LendableReference allows a reference to be taken multiple times by the same thread. For each
 * take, there has to be a takeback (no matter who does the takes and who did the take backs). This
 * is the same behaviour as with the {@link java.util.concurrent.Semaphore}. This also explains
 * why the LendableReference is not using a similar structure as the
 * {@link org.codehaus.prometheus.uninterruptiblesection.UninterruptibleSection}. A single thread could
 * lend the same reference, these references need to be returned as well (other threads are
 * allowed).
 * <p/>
 * <dt><b>Save handoff</b></dt>
 * <dd>
 * A LendableReference can be used as a save handof structure. This means that a LendableReference
 * can be used to pass objects that have visibility problems, between threads in a save way. For
 * more information about visibility problems check JSR-133 or
 * <a href="">Java Concurrency in Practice</a>.
 * </dd>
 * <p/>
 * </dd>
 * <dt><b>LendableReference vs AwaitableReference</b></dt>
 * <dd>
 * The difference between a LendableReference and an
 * {@link org.codehaus.prometheus.awaitablereference.AwaitableReference} is that a reference doesn't
 * need to be taken back with the AwaitableReference.
 * </dd>
 * <p/>
 * <dt><b>LendableReference vs ResourcePool</b></dt>
 * <dd>
 * The difference between a ResourcePool and a LendableReference is that a ResourcePool typically
 * hands out a different resource for every request (a database connection for example). With a
 * LendableReference, you get the same reference for every request (unless a new reference is set
 * between requests). But both structures require that the resource needs to be taken back.
 * </dd>
 * <p/>
 * For other comparisons between the LendableReference and the synchronization stones from
 * java.util.prometheus, see {@link AwaitableReference}.
 *
 * @author Peter Veentjer.
 */
public interface LendableReference<E> extends AwaitableReference<E> {

    /**
     * Takes back a lend reference back to this LendableReference. If an incorrect reference is
     * returned, a IncorrectReferenceTakenBackException could be thrown. The check on correctness is
     * done based on the equal method. So you are allowed to send back a different object, as long
     * as it is equal. It is up to the implementation to decide if a IllegalTakebackException needs
     * or can be thrown.
     * <p/>
     * <p/>
     * A different thread is allowed to tryTake the reference back.
     * <p/>
     * <p/>
     * todo:
     * what happens if a bogus takeback is done? So a value is taken back
     * although nothing is lend.
     *
     * @param ref the reference taken back.
     * @throws NullPointerException     if ref is <tt>null</tt>. Because a <tt>null</tt> will
     *                                  never be lend, it can't be returned.
     * @throws IllegalTakebackException if a incorrect reference is returned. It
     *                                  depends on the implementation if this
     *                                  exception is thrown. todo
     */
    void takeback(E ref);

    /**
     * Takes back a lend reference to this LendableReference and resets the reference to null.
     *
     * A different thread is allowed to tryTake the reference back. 
     *
     * @param ref
     * @throws NullPointerException if ref is null.
     * @throws IllegalTakebackException
     */
    void takebackAndReset(E ref);
}
