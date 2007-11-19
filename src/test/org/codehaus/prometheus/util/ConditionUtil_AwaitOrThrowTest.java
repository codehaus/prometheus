package org.codehaus.prometheus.util;

import static org.easymock.EasyMock.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;

public class ConditionUtil_AwaitOrThrowTest extends ConditionUtil_AbstractTest {
    private Condition condition;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        condition = createMock(Condition.class);
    }

    public void replayMocks() {
        replay(condition);
    }

    public void verifyMocks() {
        verify(condition);
    }

    public void testArguments() throws TimeoutException, InterruptedException {
        replayMocks();

        try {
            ConditionUtil.awaitOrThrow(null, 1, TimeUnit.MICROSECONDS);
            fail();
        } catch (NullPointerException ex) {
        }

        try {
            ConditionUtil.awaitOrThrow(condition, 1, null);
            fail();
        } catch (NullPointerException ex) {
        }

        try {
            ConditionUtil.awaitOrThrow(condition, -1, TimeUnit.MINUTES);
            fail();
        } catch (TimeoutException ex) {
        }

        verifyMocks();
    }

    public void testCheckTranslationToNanos() throws TimeoutException, InterruptedException {
        long timeout = 10;
        TimeUnit unit = TimeUnit.MICROSECONDS;
        long timeoutNs = unit.toNanos(timeout);

        expect(condition.awaitNanos(timeoutNs)).andReturn(1L);

        replayMocks();
        long remainingNs = ConditionUtil.awaitOrThrow(condition,timeout,unit);
        verifyMocks();

        assertEquals(1L,remainingNs);
    }
}
