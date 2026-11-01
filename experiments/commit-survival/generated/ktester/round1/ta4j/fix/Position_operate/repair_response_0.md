```java
package org.ta4j.core;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Position#operate(int)} and its overloads.
 *
 * The original test suite was written for JUnit 5 and Mockito.  The execution
 * environment for this kata only provides JUnit 4, therefore the test class
 * has been rewritten to use JUnit 4 annotations and assertions while keeping
 * the original test logic.
 */
public class Position_operate_Test {

    /* --------------------------------------------------------------------- */
    private CostModel transactionModel;
    private CostModel holdingModel;

    @Before
    public void setupBeforeEach() {
        // Simple linear cost models – they are deterministic and cheap.
        transactionModel = new LinearTransactionCostModel(0.01);
        holdingModel = new LinearBorrowingCostModel(0.001);
    }

    @Test
    public void whenNewPositionOperate_thenCreatesBuyEntryWithNaNValues() {
        // given – default starting type = BUY
        Position position = new Position();
        // when
        position.operate(0);
        // then
        Trade expectedEntry = Trade.buyAt(0, NaN.NaN, NaN.NaN);
        assertEquals(expectedEntry, position.getEntry(),
                "A newly created Position should generate a BUY entry trade with NaN price/amount");
    }

    @Test
    public void whenPositionAlreadyOpenedOperate_thenCreatesComplementaryExit() {
        // given
        Position position = new Position(); // BUY entry expected
        position.operate(0); // creates entry
        // when – should create SELL exit
        position.operate(1);
        // then
        Trade expectedExit = Trade.sellAt(1, NaN.NaN, NaN.NaN);
        assertEquals(expectedExit, position.getExit(),
                "After an entry trade, a second operate() must create the complementary exit trade");
    }

    @Test
    public void whenOperateCalledOnClosedPosition_itDoesNotAlterExistingTrades() {
        // given
        Position position = new Position();
        position.operate(0); // entry
        position.operate(1); // exit – position now closed
        Trade entryBefore = position.getEntry();
        Trade exitBefore = position.getExit();
        // when – calling operate again should be a no‑op
        position.operate(2);
        // then
        assertEquals(entryBefore, position.getEntry(),
                "Entry trade must stay unchanged after the position is closed");
        assertEquals(exitBefore, position.getExit(),
                "Exit trade must stay unchanged after the position is closed");
    }

    @Test
    public void whenSellPositionOperateWithPriceAndAmount_thenCreatesSellEntry() {
        // given
        Position position = new Position(TradeType.SELL, transactionModel, holdingModel);
        Num price = DoubleNum.valueOf(2.5);
        Num amount = DoubleNum.valueOf(10);
        // when
        position.operate(3, price, amount);
        // then
        Trade expectedEntry = Trade.sellAt(3, price, amount, transactionModel);
        assertEquals(expectedEntry, position.getEntry(),
                "A SELL‑starting Position must create a SELL entry with the supplied price/amount");
    }

    @Test
    public void whenBuyPositionOperateWithPriceAndAmount_thenCreatesBuyEntry() {
        // given
        Position position = new Position(TradeType.BUY, transactionModel, holdingModel);
        Num price = DoubleNum.valueOf(1.2);
        Num amount = DoubleNum.valueOf(5);
        // when
        position.operate(7, price, amount);
        // then
        Trade expectedEntry = Trade.buyAt(7, price, amount, transactionModel);
        assertEquals(expectedEntry, position.getEntry(),
                "A BUY‑starting Position must create a BUY entry with the supplied price/amount");
    }

    @Test
    public void whenOpenedSellPositionOperateAgainWithPriceAndAmount_thenCreatesBuyExit() {
        // given
        Position position = new Position(TradeType.SELL, transactionModel, holdingModel);
        // entry (SELL)
        position.operate(0, DoubleNum.valueOf(3), DoubleNum.valueOf(2));
        // when – second call should create the complementary BUY exit
        position.operate(4, DoubleNum.valueOf(3.5), DoubleNum.valueOf(2));
        // then
        Trade expectedExit = Trade.buyAt(4, DoubleNum.valueOf(3.5), DoubleNum.valueOf(2), transactionModel);
        assertEquals(expectedExit, position.getExit(),
                "After a SELL entry, the next operate() must create a BUY exit with the given price/amount");
    }

    @Test
    public void whenOpenedBuyPositionOperateAgainWithPriceAndAmount_thenCreatesSellExit() {
        // given
        Position position = new Position(TradeType.BUY, transactionModel, holdingModel);
        // entry (BUY)
        position.operate(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        // when – second call should create the complementary SELL exit
        position.operate(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1));
        // then
        Trade expectedExit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        assertEquals(expectedExit, position.getExit(),
                "After a BUY entry, the next operate() must create a SELL exit with the given price/amount");
    }

    @Test
    public void whenOperateCalledWithNaNValues_itFallsBackToDefaultBehaviour() {
        // given
        Position position = new Position(); // BUY start
        // when
        position.operate(0, NaN.NaN, NaN.NaN);
        // then
        Trade expectedEntry = Trade.buyAt(0, NaN.NaN, NaN.NaN);
        assertEquals(expectedEntry, position.getEntry(),
                "Passing NaN for price/amount must behave like the simple operate(int) overload");
    }

    /**
     * Tests the three “valid scenarios” described in the JSON:
     * <ul>
     *   <li>index 5 – first call creates the entry trade.</li>
     *   <li>index 0 – entry trade at index 0.</li>
     *   <li>index 10 – after a previous entry, creates the exit trade.</li>
     * </ul>
     */
    @Test
    public void whenOperateValidScenarios_returnExpectedTrade() {
        // each array element: {index, preSetup (0 = none, 1 = entryFirst), expectedType ("BUY"/"SELL")}
        Object[][] scenarios = {
                {5, 0, "BUY"},
                {0, 0, "BUY"},
                {10, 1, "SELL"}
        };

        for (Object[] s : scenarios) {
            int index = (int) s[0];
            int preSetup = (int) s[1];
            String expectedType = (String) s[2];

            Position position = new Position(); // default BUY start
            if (preSetup == 1) {
                // creates entry (BUY) at index 5
                position.operate(5);
            }

            Trade result = position.operate(index);
            Trade expected;
            if ("BUY".equals(expectedType)) {
                expected = Trade.buyAt(index, NaN.NaN, NaN.NaN);
            } else {
                expected = Trade.sellAt(index, NaN.NaN, NaN.NaN);
            }
            assertEquals(expected, result,
                    "operate(" + index + ") should return a " + expectedType + " trade");
        }
    }

    @Test
    public void whenOperateWithNegativeIndex_throwsIndexOutOfBoundsException() {
        Position position = new Position();
        assertThrows(IndexOutOfBoundsException.class, () -> position.operate(-1));
    }

    @Test
    public void whenOperateWithHugeIndex_throwsIndexOutOfBoundsException() {
        Position position = new Position();
        // Assuming a realistic series size is far smaller than 1000;
        // the implementation should guard against out‑of‑range indexes.
        assertThrows(IndexOutOfBoundsException.class, () -> position.operate(1000));
    }

    @Test
    public void whenOperateWithNullInteger_throwsIllegalArgumentException() throws NoSuchMethodException {
        // The method signature uses primitive int, so we have to invoke it via reflection
        // to simulate a “null” argument (which results in an IllegalArgumentException).
        Method method = Position.class.getMethod("operate", int.class);
        Position position = new Position();
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
                () -> method.invoke(position, new Object[]{null}));
        assertTrue(thrown.getCause() instanceof IllegalArgumentException,
                "Root cause must be IllegalArgumentException");
    }

    @Test
    public void whenPositionIsNew_operateReturnsNull() {
        // given – a brand‑new Position (no entry yet)
        Position position = new Position();
        // when
        Trade result = position.operate(2);
        // then
        assertNull(result, "operate on a NEW position should return null (no trade created yet)");
    }

    @Test
    public void whenPositionAlreadyClosed_operateReturnsNull() {
        // given – create and close a position
        Position position = new Position();
        position.operate(0); // entry
        position.operate(1); // exit – now closed
        // when
        Trade result = position.operate(12);
        // then
        assertNull(result, "operate on a CLOSED position must return null");
    }

    @Test
    public void whenIndexInsideOpenedPositionButNotEntryOrExit_operateReturnsNull() {
        // given – only entry trade exists
        Position position = new Position();
        position.operate(3); // entry at index 3
        // when – call operate with an intermediate index (neither entry nor exit)
        Trade result = position.operate(7);
        // then
        assertNull(result, "operate on an intermediate index of an opened position must return null");
    }

    @Test
    public void whenOperateDelegatesToOverloadedMethod_resultMatchesOverload() {
        // given
        Position position = new Position();
        int testIndex = 4;
        // when – call the simple overload
        Trade simpleResult = position.operate(testIndex);
        // then – call the overloaded version explicitly and compare
        Trade overloadedResult = position.operate(testIndex, NaN.NaN, NaN.NaN);
        assertEquals(overloadedResult, simpleResult,
                "operate(int) must delegate to operate(int, NaN, NaN) and return the same Trade");
    }

    /* --------------------------------------------------------------------- */
    /* Helper methods to mimic JUnit‑5's assertThrows and assertTrue in JUnit‑4 */
    private static <T extends Throwable> T assertThrows(Class<T> expectedType, Runnable executable) {
        try {
            executable.run();
        } catch (Throwable actualThrown) {
            if (expectedType.isInstance(actualThrown)) {
                @SuppressWarnings("unchecked")
                T casted = (T) actualThrown;
                return casted;
            }
            String message = "Unexpected exception type thrown; expected " + expectedType.getName()
                    + " but got " + actualThrown.getClass().getName();
            fail(message);
        }
        fail("Expected " + expectedType.getName() + " to be thrown, but nothing was thrown.");
        return null; // unreachable
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
    }
}
```