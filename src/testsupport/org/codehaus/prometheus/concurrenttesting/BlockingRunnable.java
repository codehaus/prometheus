/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.concurrenttesting;

import static junit.framework.Assert.*;

import java.util.concurrent.TimeoutException;

/**
 * A BaseTestRunnable that adds support for blocking calls.
 *
 * todo: there is a lot of overlap in this runnable and the testthread.
 *
 * @author Peter Veentjer.
 * @since 1.0
 */
public abstract class BlockingRunnable extends TestRunnable {

    protected volatile BlockingState state;

    protected abstract void runBlockingInternal() throws TimeoutException, InterruptedException;

    @Override
    public final void runInternal() {
        state = BlockingState.waiting;
        try {
            runBlockingInternal();
            state = BlockingState.finished;
        } catch (TimeoutException e) {
            state = BlockingState.timeout;
        } catch (InterruptedException e) {
            state = BlockingState.interrupted;
        }
    }

    public void assertIsUnstarted(){
        assertNull(state);
    }

    /**
     * Asserts the the call is waiting (is blocked).
     */
    public void assertIsStarted() {
        assertState(BlockingState.waiting);
    }

    /**
     * Asserts that the call has finished successfully
     * (so without a RuntimeException).
     */
    public void assertIsFinised() {
        assertState(BlockingState.finished);
    }

    /**
     * Asserts that the call was interrupted.
     */
    public void assertIsInterrupted() {
        assertState(BlockingState.interrupted);
    }

    /**
     * Asserts that the call received a timeout.
     */
    public void assertIsTimedout() {
        assertState(BlockingState.timeout);
    }

    /**
     * Asserts that no runtimeexception is thrown, and that that the state is the expected one. 
     *
     * @param expectedState the expected state.
     */
    public void assertState(BlockingState expectedState) {
        assertNoRuntimeException();
        assertNotNull("blocking state has not been set, has the task run?", state);
        assertEquals(expectedState, state);
    }

    /**
     * Returns the current blocking state. Could be null, if the call
     * isn't waiting yet.
     *
     * @return the current blocking state.
     */
    public BlockingState getState() {
        return state;
    }
}
