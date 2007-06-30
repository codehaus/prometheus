/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A synchronization structure that makes it easy to let threads wait until some condition is met.
 * Waitpoints can be used for all kinds of things. It could be used for making a structure closable
 * for example or control in different ways its blocking behaviour).
 * <p/>
 * A Waitpoint could be used as a strategy/policy: a way to customize the blocking behaviour,
 * without changing the overal structure. For some applications see:
 * <ol>
 * <li>{@link org.codehaus.prometheus.lendablereference.LendableReferenceWithWaitingTakes} </li>
 * </ol>
 * <p/>
 * todo:
 * idea: (needs better documentation)
 * A Waitpoint in some cases doesn't provide atomicity itself. In some cases it is better to let the
 * object that owns the Waitpoint do the locking and delegate the waitingpart to the Waitpoint.
 * <pre>
 * Item take()throws InterruptedException{
 *      mainLock.acquire();
 *      try{
 *          waitpoint.pass();
 *          size--;
 *          list.remove(0);
 *      }finally{
 *          mainLock.release();
 *      }
 * }
 * </pre>
 * The mainLock is shared between the structure that contains the waitpoint and the waitpoint itself.
 * <p/>
 * todo: stuff for documentation
 * If a Waitpoint implementation uses a condition (and this condition is create by the mainLock) the
 * waitpoint doesn't need to acquire the mainLock because the owner already did this. If the locks
 * passed to the WaitPoint are reentrant, a self deadlock won't occur.
 * <p/>
 * idea:
 * Waitpoint en aspecten.
 * <p/>
 * <td><b>Waitpoint vs Condition</b></td>
 * <dd>
 * The Waitpoint can be seen as the waiting part of a Condition.
 * </dd>
 * <p/>
 * <td><b>Waitpoint vs CountDownLatch/CyclicBarrier</b></td>
 * <dd>
 * A Waitpoint is essentially the waiting part of the CountDownLatch and the CyclicBarrier. A
 * WaitPointis not a latch meaning that it doesn't need to have a final state (unlike a latch).
 * </dd>
 * <p/>
 * <td><b>Waitpoint vs Future</b></td>
 * <dd>
 * A Future could be implemented by using a Waitpoint.
 * </dd>
 * <p/>
 * todo:
 * a waitpoint vs takable/puttable
 *
 * @author Peter Veentjer.
 */
public interface Waitpoint {

    /**
     * Pass this Waitpoint. It depends on the implementation how long the wait for passage is going
     * to take.
     * <p/>
     * This method is responsive to interrupts. If the interrupt flag is set, an
     * InterruptedException doesn't need to be thrown; it is up to the implementation to decide.
     *
     * @throws InterruptedException if the calling thread is interrupted while trying to pass this
     *                              Waitpoint.
     */
    void pass() throws InterruptedException;

    /**
     * Returns <tt>true</tt> if this Waitpoint is passible, <tt>false</tt> otherwise. The returned
     * value could be stale at the moment it is returned.
     *
     * @return <tt>true</tt> if this Waitpoint is passible, <tt>false</tt> otherwise.
     */
    boolean isPassible();

    /**
     * Tries to pass this Waitpoint without blocking.
     * <p/>
     * todo: remark about interrupts.
     *
     * @return <tt>true</tt> if the pass was successful, <tt>false</tt> otherwise.
     */
    boolean tryPass();

    /**
     * Tries to pass this Waitpoint using a timeout.
     * <p/>
     * This method is responsive to interrupts. If the interrupt flag is set, an
     * InterruptedException doesn't need to be thrown; it is up to the implementation to decide.
     *
     * @param timeout the amount of time this call is allowed to block before giving up.
     * @param unit    the time unit for the timeout argument.
     * @return the remaining timeout in nanoseconds. This value will always be equal or larger than
     *         zero.
     * @throws TimeoutException     if a timeout occurs.
     * @throws InterruptedException if the calling thread is interrupted while trying to pass this
     *                              Waitpoint.
     * @throws NullPointerException if unit is <tt>null</tt>.
     */
    long tryPass(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;
}
