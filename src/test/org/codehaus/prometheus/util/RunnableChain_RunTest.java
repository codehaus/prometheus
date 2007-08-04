/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.testsupport.CountingRunnable;
import org.codehaus.prometheus.testsupport.TestRunnable;
import org.codehaus.prometheus.testsupport.ThrowingRunnable;

import javax.swing.undo.CannotRedoException;

public class RunnableChain_RunTest extends RunnableChain_AbstractTest {

    public void testNoTasks() {
        newBreakableChain();

        runnableChain.run();
        exceptionHandler.assertNoErrors();
    }

    public void testBreakableChain_noFailures() {
        CountingRunnable r1 = new CountingRunnable();
        CountingRunnable r2 = new CountingRunnable();
        CountingRunnable r3 = new CountingRunnable();

        newBreakableChain(r1, r2, r3);

        runnableChain.run();

        r1.assertExecutedOnce();
        r2.assertExecutedOnce();
        r3.assertExecutedOnce();
        exceptionHandler.assertNoErrors();
    }

    public void testBreakableChain_oneFailure() {
        RuntimeException ex = new CannotRedoException();
        CountingRunnable r1 = new ThrowingRunnable(ex);
        CountingRunnable r2 = new CountingRunnable();
        CountingRunnable r3 = new CountingRunnable();

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
        CountingRunnable r1 = new CountingRunnable();
        CountingRunnable r2 = new ThrowingRunnable(ex);

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
        RuntimeException ex = new CannotRedoException();
        CountingRunnable r1 = new ThrowingRunnable(ex);
        CountingRunnable r2 = new ThrowingRunnable(ex);
        CountingRunnable r3 = new CountingRunnable();

        newUnbreakableChain(r1, r2, r3);

        runnableChain.run();

        r1.assertExecutedOnce();
        r2.assertExecutedOnce();
        r3.assertExecutedOnce();
        exceptionHandler.assertErrorCountAndNoOthers(CannotRedoException.class, 2);
    }

    public void testUnbreakableChain_lastOneFails() {
        RuntimeException ex = new CannotRedoException();
        CountingRunnable r1 = new CountingRunnable();
        CountingRunnable r2 = new ThrowingRunnable(ex);
        
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
