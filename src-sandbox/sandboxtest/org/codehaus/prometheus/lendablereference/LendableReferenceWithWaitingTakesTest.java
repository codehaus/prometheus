/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import org.codehaus.prometheus.waitpoint.Waitpoint;
import org.codehaus.prometheus.waitpoint.CloseableWaitpoint;
import org.codehaus.prometheus.references.RelaxedLendableReference;
import org.codehaus.prometheus.references.LendableReference;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Because the LendableReferenceWithWaitingTakes doesn't have much logic,
 * I think the mocking approach for testing is acceptable.
 *
 * @author Peter Veentjer.
 */
public class LendableReferenceWithWaitingTakesTest extends TestCase {
    private LendableReference<Integer> targetMock;
    private Waitpoint waitpointMock;
    private LendableReferenceWithWaitingTakes<Integer, Waitpoint> lendeableRef;

    public void setUp() {
        targetMock = createMock(LendableReference.class);
        waitpointMock = createMock(Waitpoint.class);
        lendeableRef = new LendableReferenceWithWaitingTakes<Integer, Waitpoint>(targetMock, waitpointMock);
    }

    public void verifyMocks() {
        verify(targetMock);
        verify(waitpointMock);
    }

    public void replayMocks() {
        replay(targetMock);
        replay(waitpointMock);
    }

    public void testConstructor(){
        try{
            new LendableReferenceWithWaitingTakes(null,new CloseableWaitpoint());
            fail();
        }catch(NullPointerException ex){
            assertTrue(true);
        }

        try{
            new LendableReferenceWithWaitingTakes(new RelaxedLendableReference(),null);
            fail();
        }catch(NullPointerException ex){
            assertTrue(true);
        }

        LendableReference target = new RelaxedLendableReference();
        CloseableWaitpoint waitpoint = new CloseableWaitpoint();
        LendableReferenceWithWaitingTakes ref = new LendableReferenceWithWaitingTakes(target,waitpoint);
        assertSame(target,ref.getTarget());
        assertSame(waitpoint,ref.getWaitpoint());
    }

    //==================tryTake==============

    public void testTake_success() throws InterruptedException {
        Integer ref = 20;
        waitpointMock.pass();
        expect(targetMock.take()).andReturn(ref);

        replayMocks();
        Integer foundRef = lendeableRef.take();
        verifyMocks();

        assertSame(ref, foundRef);
    }

    public void testTake_waitInterrupts() throws InterruptedException {
        waitpointMock.pass();
        expectLastCall().andThrow(new InterruptedException());

        replayMocks();
        try {
            lendeableRef.take();
            fail();
        } catch (InterruptedException ex) {
            assertTrue(true);
        }
        verifyMocks();
    }

    public void testTake_targetTakeInterrupts() throws InterruptedException {
        waitpointMock.pass();
        expect(targetMock.take()).andThrow(new InterruptedException());

        replayMocks();
        try {
            lendeableRef.take();
            fail();
        } catch (InterruptedException ex) {
            assertTrue(true);
        }
        verifyMocks();
    }

    //==================takeWithTimeout==============

    public void testTakeWithTimeout_success() throws TimeoutException, InterruptedException {
        long timeout = 10;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        long remainingTimeoutNs = 5;
        Integer ref = 20;
        expect(waitpointMock.tryPass(timeout, unit)).andReturn(remainingTimeoutNs);
        expect(targetMock.tryTake(eq(remainingTimeoutNs), eq(TimeUnit.NANOSECONDS))).andReturn(ref);

        replayMocks();
        Integer foundRef = lendeableRef.tryTake(timeout, unit);
        verifyMocks();

        assertSame(ref, foundRef);
    }

    public void testTakeWithTimeout_timeoutOnWaitpoint() throws TimeoutException, InterruptedException {
        long timeout = 10;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        expect(waitpointMock.tryPass(timeout, unit)).andThrow(new TimeoutException());

        replayMocks();
        try {
            lendeableRef.tryTake(timeout, unit);
            fail();
        } catch (TimeoutException e) {
            assertTrue(true);
        }
        verifyMocks();
    }

    public void testTakeWithTimeout_timeoutOnTarget() throws TimeoutException, InterruptedException {
        long timeout = 10;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        long remainingTimeoutNs = 5;
        expect(waitpointMock.tryPass(timeout, unit)).andReturn(remainingTimeoutNs);
        expect(targetMock.tryTake(eq(remainingTimeoutNs), eq(TimeUnit.NANOSECONDS))).andThrow(new TimeoutException());

        replayMocks();
        try {
            lendeableRef.tryTake(timeout, unit);
            fail();
        } catch (TimeoutException e) {
            assertTrue(true);
        }
        verifyMocks();
    }

    public void testTakeWithTimeout_waitInterrupted() throws TimeoutException, InterruptedException {
        long timeout = 10;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        expect(waitpointMock.tryPass(timeout, unit)).andThrow(new InterruptedException());

        replayMocks();
        try {
            lendeableRef.tryTake(timeout, unit);
            fail();
        } catch (InterruptedException ex) {
            assertTrue(true);
        }
        verifyMocks();
    }

    public void testTakeWithTimeout_targetTakeInterruped() throws TimeoutException, InterruptedException {
        long timeout = 10;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        long remainingTimeoutNs = 5;
        expect(waitpointMock.tryPass(timeout, unit)).andReturn(remainingTimeoutNs);
        expect(targetMock.tryTake(eq(remainingTimeoutNs), eq(TimeUnit.NANOSECONDS))).andThrow(new InterruptedException());

        replayMocks();
        try {
            lendeableRef.tryTake(timeout, unit);
            fail();
        } catch (InterruptedException ex) {
            assertTrue(true);
        }
        verifyMocks();
    }

    //================================================

    public void testTakeBack() {
        Integer ref = 20;
        targetMock.takeback(ref);

        replayMocks();
        lendeableRef.takeback(ref);
        verifyMocks();
    }

    public void testPut() throws InterruptedException {
        Integer newRef = 10;
        Integer oldRef = 20;

        expect(targetMock.put(newRef)).andReturn(oldRef);
        replayMocks();
        Integer foundOldRef = lendeableRef.put(newRef);
        verifyMocks();

        assertSame(oldRef, foundOldRef);
    }

    public void testPutWithTimeout() throws InterruptedException, TimeoutException {
        long timeout = 20;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        Integer newRef = 10;
        Integer oldRef = 20;

        expect(targetMock.tryPut(newRef, timeout, unit)).andReturn(oldRef);
        replayMocks();
        Integer foundOldRef = lendeableRef.tryPut(newRef, timeout, unit);
        verifyMocks();

        assertSame(oldRef, foundOldRef);
    }

    public void testPeek() {
        Integer ref = 10;
        expect(targetMock.peek()).andReturn(ref);

        replayMocks();
        Integer foundRef = lendeableRef.peek();
        verifyMocks();

        assertSame(ref, foundRef);
    }
}
