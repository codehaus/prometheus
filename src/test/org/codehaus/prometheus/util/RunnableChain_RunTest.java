/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.concurrenttesting.TestRunnable;

import javax.swing.undo.CannotRedoException;

public class RunnableChain_RunTest extends RunnableChain_AbstractTest {

    public void testNoTasks() {
        newBreakableChain();

        runnableChain.run();
        exceptionHandler.assertNoErrors();
    }

    public void testBreakableChain_noFailures() {
        TestRunnable r1 = new TestRunnable();
        TestRunnable r2 = new TestRunnable();
        TestRunnable r3 = new TestRunnable();

        newBreakableChain(r1, r2, r3);

        runnableChain.run();

        r1.assertExecutedOnce();
        r2.assertExecutedOnce();
        r3.assertExecutedOnce();
        exceptionHandler.assertNoErrors();
    }

    public void testBreakableChain_oneFailure() {
        RuntimeException ex = new CannotRedoException();
        TestRunnable r1 = new TestRunnable(ex);
        TestRunnable r2 = new TestRunnable();
        TestRunnable r3 = new TestRunnable();

        newBreakableChain(r1, r2, r3);

        try {
            runnableChain.run();
            fail();
        } catch (CannotRedoException e) {

        }

        r1.assertExecutedOnce();
        r2.assertNotExecuted();
        r3.assertNotExecuted();
        exceptionHandler.assertNoErrors();
    }

    public void testBreakableChain_lastOneFails() {
        RuntimeException ex = new CannotRedoException();
        TestRunnable r1 = new TestRunnable();
        TestRunnable r2 = new TestRunnable(ex);

        newBreakableChain(r1, r2);

        try {
            runnableChain.run();
            fail();
        } catch (CannotRedoException e) {
        }

        r1.assertExecutedOnce();
        r2.assertExecutedOnce();
        exceptionHandler.assertNoErrors();
    }

    public void testBreakableChain_errorBreaks() {

    }

    public void testUnbreakableChain_noFailures() {
        TestRunnable r1 = new TestRunnable();
        TestRunnable r2 = new TestRunnable();
        TestRunnable r3 = new TestRunnable();

        newUnbreakableChain(r1, r2, r3);

        runnableChain.run();

        r1.assertExecutedOnce();
        r2.assertExecutedOnce();
        r3.assertExecutedOnce();
        exceptionHandler.assertNoErrors();
    }

    public void testUnbreakableChain_multipleFailures() {
        TestRunnable r1 = new TestRunnable(new CannotRedoException());
        TestRunnable r2 = new TestRunnable(new CannotRedoException());
        TestRunnable r3 = new TestRunnable();

        newUnbreakableChain(r1, r2, r3);

        runnableChain.run();

        r1.assertExecutedOnce();
        r2.assertExecutedOnce();
        r3.assertExecutedOnce();
        exceptionHandler.assertErrorCountAndNoOthers(CannotRedoException.class, 2);
    }

    public void testUnbreakableChain_lastOneFails() {
        RuntimeException ex = new CannotRedoException();
        TestRunnable r1 = new TestRunnable();
        TestRunnable r2 = new TestRunnable(ex);

        newUnbreakableChain(r1, r2);

        runnableChain.run();

        r1.assertExecutedOnce();
        r2.assertExecutedOnce();
        exceptionHandler.assertErrorCountAndNoOthers(CannotRedoException.class, 1);
    }

    public void testUnbreakableChain_errorBreaks() {
        //todo
    }
}
