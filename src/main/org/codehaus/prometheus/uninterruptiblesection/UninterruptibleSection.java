/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.uninterruptiblesection;

/**
 * A section of code (0 or more statements) that should not be interrupted, even though the
 * statements themselves are interruptible. The UninterruptibleSection makes it possible to use a
 * section of interruptible code on a location where you can't deal with interrupts.
 * <p/>
 * example of an interruptible put on a BlockingQueue that is transformed to a uninterruptible put.
 * <pre>
 * BlockingQueue queue = new BlockingQueue();
 * <p/>
 * public void putUninterruptibly(final Object item){
 *      UninterruptibleSection section = new UninterruptibleSection(){
 *          protected Object interruptiblesection()throws InterruptedException{
 *              queue.put(item);
 *              return null;
 *          }
 *      };
 *      section.execute();
 * }
 * </pre>
 * <br/>
 * When a section throws an InterruptedException while being executed, the exception is caught, and
 * the section is retried. That is why it is important that the system is left in an invalid state.
 * <p/>
 * Example that violates this requirement:
 * <pre>
 * </pre>
 * <p/>
 * <td><b>Use with care</b></td>
 * <dd>
 * Uninterruptible calls should be used with a <b>lot of care</b>. If a call is not interruptible,
 * and it blocks for a indetermined amount of time, the calling thread can't be interrupted and this
 * could lead to problems. For example, if a ThreadPoolExecutor shuts down by using the
 * {@link java.util.concurrent.ThreadPoolExecutor#shutdownNow()} method, a worker thread can't be
 * interrupted when it is blocked in a non interruptible call and this can lead to the executor that
 * can't won't shut down. The same goes for the ThreadPoolRepeater. Doing uninterruptible calls only
 * should be done when you can't make sure that the system is left in consistent state when the
 * thread is interrupted. And although the checked nature of the InterruptibleException can be a
 * pain, making a call interruptible in most cases is a better solution.
 * <p/>
 * <p/>
 * Example of non uninterruptible thread.
 * <pre>
 * public class UninterruptibleThread implements Thread{
 *      public void runWork(){
 *          UninterruptibleSection section = new UninterruptibleSection(){
 *              protected Object interruptiblesection()throws InterruptedException{
 *                  blockingQueue.put("foo");
 *                  return null;
 *              }
 *          };
 *          section.execute();
 *      }
 * }
 * </pre>
 * This thread can't be interrupted and only completes when it places that item on the
 * blockingQueue.
 * </dd>
 * <p/>
 * <td><b>Dealing with RuntimeExceptions</b></td>
 * <dd>
 * RuntimeException can be thrown and are propagated through the {@link #execute()} method without
 * leading to problems inside the UninterruptleSection because it doesn't maintain any state between
 * requests.
 * <p/>
 * At the moment there is no support for checked exceptions. So the only way is to catch the checked
 * exception inside the uninterruptiblesection, wrap it in an unchecked exception, and extract it
 * from the runtimeexception that can caught by an exception handler around the execute method. This
 * is not a very pretty solution, but I'm thinking about providing support for this aswel (add an
 * extra execute method that throws a generic checked exception).
 * </dd>
 * todo: explain inner workings.<p/>
 * todo: explain return value<p/>
 * todo: explain what happens when the calling thread is interrupted, or
 * already was interrupted when it running executing the execute method.<p/>
 * todo: closures and syntax improvement<p/>
 * <p>
 * In java 7 a closure could be added to make it more attractive to use.
 * @author Peter Veentjer.
 * @see TimedUninterruptibleSection
 * @since 0.1
 */
public abstract class UninterruptibleSection<E> {

    /**
     * The original section that contains the interruptible calls and is protected by this
     * UninterruptibleSection.
     *
     * @return a section should return a value (can be ignored)
     * @throws InterruptedException the execution of the section is interrupted.
     */
    protected abstract E interruptibleSection() throws InterruptedException;

    /**
     * Executes the interruptible interruptiblesection.
     * <p/>
     * If the {@link #interruptibleSection()} throws a RuntimeException, this exception is
     * propagated (nothing bad happens inside this UninterruptibleSection).
     *
     * @return the return value from {@link # interruptibleSection ()}
     */
    public final E execute() {
        boolean restoreInterrupt = Thread.interrupted();
        try {
            while (true) {
                try {
                    return interruptibleSection();
                } catch (InterruptedException ex) {
                    restoreInterrupt = true;
                }
            }
        } finally {
            if (restoreInterrupt)
                Thread.currentThread().interrupt();
        }
    }
}
