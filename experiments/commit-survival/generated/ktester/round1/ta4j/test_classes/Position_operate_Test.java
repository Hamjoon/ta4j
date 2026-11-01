package org.ta4j.core;

import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.*;
import org.ta4j.core.num.NaN;
import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.*;

@ExtendWith(MockitoExtension.class)
class Position_operate_Test {

    /* --------------------------------------------------------------------- */
    private CostModel transactionModel;

    private CostModel holdingModel;

    @BeforeEach
    void setupBeforeEach() {
        // Simple linear cost models – they are deterministic and cheap.
        transactionModel = new LinearTransactionCostModel(0.01);
        holdingModel = new LinearBorrowingCostModel(0.001);
    }

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
            String message = "Unexpected exception type thrown; expected " + expectedType.getName() + " but got " + actualThrown.getClass().getName();
            fail(message);
        }
        fail("Expected " + expectedType.getName() + " to be thrown, but nothing was thrown.");
        // unreachable
        return null;
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
    }

    @Test
    void whenNewPositionOperate_thenCreatesBuyEntryWithNaNValues() {
        // given – default starting type = BUY
        Position position = new Position();
        // when
        position.operate(0);
        // then
        Trade expectedEntry = Trade.buyAt(0, NaN.NaN, NaN.NaN);
        assertEquals("A newly created Position should generate a BUY entry trade with NaN price/amount", expectedEntry, position.getEntry());
    }

    @Test
    void whenPositionAlreadyOpenedOperate_thenCreatesComplementaryExit() {
        // given
        Position position = new Position();
        // creates entry
        position.operate(0);
        // when – should create SELL exit
        position.operate(1);
        // then
        Trade expectedExit = Trade.sellAt(1, NaN.NaN, NaN.NaN);
        assertEquals("After an entry trade, a second operate() must create the complementary exit trade", expectedExit, position.getExit());
    }

    @Test
    void whenOperateCalledOnClosedPosition_itDoesNotAlterExistingTrades() {
        // given
        Position position = new Position();
        // entry
        position.operate(0);
        // exit – position now closed
        position.operate(1);
        Trade entryBefore = position.getEntry();
        Trade exitBefore = position.getExit();
        // when – calling operate again should be a no‑op
        position.operate(2);
        // then
        assertEquals("Entry trade must stay unchanged after the position is closed", entryBefore, position.getEntry());
        assertEquals("Exit trade must stay unchanged after the position is closed", exitBefore, position.getExit());
    }

    @Test
    void whenSellPositionOperateWithPriceAndAmount_thenCreatesSellEntry() {
        // given
        Position position = new Position(TradeType.SELL, transactionModel, holdingModel);
        Num price = DoubleNum.valueOf(2.5);
        Num amount = DoubleNum.valueOf(10);
        // when
        position.operate(3, price, amount);
        // then
        Trade expectedEntry = Trade.sellAt(3, price, amount, transactionModel);
        assertEquals("A SELL‑starting Position must create a SELL entry with the supplied price/amount", expectedEntry, position.getEntry());
    }

    @Test
    void whenBuyPositionOperateWithPriceAndAmount_thenCreatesBuyEntry() {
        // given
        Position position = new Position(TradeType.BUY, transactionModel, holdingModel);
        Num price = DoubleNum.valueOf(1.2);
        Num amount = DoubleNum.valueOf(5);
        // when
        position.operate(7, price, amount);
        // then
        Trade expectedEntry = Trade.buyAt(7, price, amount, transactionModel);
        assertEquals("A BUY‑starting Position must create a BUY entry with the supplied price/amount", expectedEntry, position.getEntry());
    }

    @Test
    void whenOpenedSellPositionOperateAgainWithPriceAndAmount_thenCreatesBuyExit() {
        // given
        Position position = new Position(TradeType.SELL, transactionModel, holdingModel);
        // entry (SELL)
        position.operate(0, DoubleNum.valueOf(3), DoubleNum.valueOf(2));
        // when – second call should create the complementary BUY exit
        position.operate(4, DoubleNum.valueOf(3.5), DoubleNum.valueOf(2));
        // then
        Trade expectedExit = Trade.buyAt(4, DoubleNum.valueOf(3.5), DoubleNum.valueOf(2), transactionModel);
        assertEquals("After a SELL entry, the next operate() must create a BUY exit with the given price/amount", expectedExit, position.getExit());
    }

    @Test
    void whenOpenedBuyPositionOperateAgainWithPriceAndAmount_thenCreatesSellExit() {
        // given
        Position position = new Position(TradeType.BUY, transactionModel, holdingModel);
        // entry (BUY)
        position.operate(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        // when – second call should create the complementary SELL exit
        position.operate(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1));
        // then
        Trade expectedExit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        assertEquals("After a BUY entry, the next operate() must create a SELL exit with the given price/amount", expectedExit, position.getExit());
    }

    @Test
    void whenOperateCalledWithNaNValues_itFallsBackToDefaultBehaviour() {
        // given
        // BUY start
        Position position = new Position();
        // when
        position.operate(0, NaN.NaN, NaN.NaN);
        // then
        Trade expectedEntry = Trade.buyAt(0, NaN.NaN, NaN.NaN);
        assertEquals("Passing NaN for price/amount must behave like the simple operate(int) overload", expectedEntry, position.getEntry());
    }

    /**
     * Parameterised test for the three “valid scenarios” described in the JSON.
     *
     * <ul>
     *   <li>index 5 – first call creates the entry trade.</li>
     *   <li>index 0 – entry trade at index 0.</li>
     *   <li>index 10 – after a previous entry, creates the exit trade.</li>
     * </ul>
     */
    @ParameterizedTest(name = "operate({0}) on a Position should return expected Trade")
    @CsvSource({ // index,   preSetup (none=0, entryFirst=1), expectedType (BUY/SELL)
    // fresh position, entry at 5
    // fresh position, entry at 0
    // entry already created, exit at 10
    "5, 0, BUY", "0, 0, BUY", "10, 1, SELL" })
    void whenOperateValidScenarios_returnExpectedTrade(int index, int preSetup, String expectedType) {
        Object[][] scenarios = { { 5, 0, "BUY" }, { 0, 0, "BUY" }, { 10, 1, "SELL" } };
        for (Object[] s : scenarios) {
            int idx = (int) s[0];
            int preSetupVal = (int) s[1];
            String expectedTypeVal = (String) s[2];
            // default BUY start
            Position position = new Position();
            if (preSetupVal == 1) {
                // creates entry (BUY) at index 5
                position.operate(5);
            }
            Trade result = position.operate(idx);
            Trade expected;
            if ("BUY".equals(expectedTypeVal)) {
                expected = Trade.buyAt(idx, NaN.NaN, NaN.NaN);
            } else {
                expected = Trade.sellAt(idx, NaN.NaN, NaN.NaN);
            }
            assertEquals("operate(" + idx + ") should return a " + expectedTypeVal + " trade", expected, result);
        }
    }

    @Test
    void whenOperateWithNegativeIndex_throwsIndexOutOfBoundsException() {
        Position position = new Position();
        assertThrows(IndexOutOfBoundsException.class, () -> position.operate(-1));
    }

    @Test
    void whenOperateWithHugeIndex_throwsIndexOutOfBoundsException() {
        Position position = new Position();
        // Assuming a realistic series size is far smaller than 1000;
        // the implementation should guard against out‑of‑range indexes.
        assertThrows(IndexOutOfBoundsException.class, () -> position.operate(1000));
    }

    @Test
    void whenOperateWithNullInteger_throwsIllegalArgumentException() throws NoSuchMethodException {
        // The method signature uses primitive int, so we have to invoke it via reflection
        // to simulate a “null” argument (which results in an IllegalArgumentException).
        Method method = Position.class.getMethod("operate", int.class);
        Position position = new Position();
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> method.invoke(position, new Object[] { null }));
        assertTrue(thrown.getCause() instanceof IllegalArgumentException, "Root cause must be IllegalArgumentException");
    }

    @Test
    void whenPositionIsNew_operateReturnsNull() {
        // given – a brand‑new Position (no entry yet)
        Position position = new Position();
        // when
        Trade result = position.operate(2);
        // then
        assertNull("operate on a NEW position should return null (no trade created yet)", result);
    }

    @Test
    void whenPositionAlreadyClosed_operateReturnsNull() {
        // given – create and close a position
        Position position = new Position();
        // entry
        position.operate(0);
        // exit – now closed
        position.operate(1);
        // when
        Trade result = position.operate(12);
        // then
        assertNull("operate on a CLOSED position must return null", result);
    }

    @Test
    void whenIndexInsideOpenedPositionButNotEntryOrExit_operateReturnsNull() {
        // given – only entry trade exists
        Position position = new Position();
        // entry at index 3
        position.operate(3);
        // when – call operate with an intermediate index (neither entry nor exit)
        Trade result = position.operate(7);
        // then
        assertNull("operate on an intermediate index of an opened position must return null", result);
    }

    @Test
    void whenOperateDelegatesToOverloadedMethod_resultMatchesOverload() {
        // given
        Position position = new Position();
        int testIndex = 4;
        // when – call the simple overload
        Trade simpleResult = position.operate(testIndex);
        // then – call the overloaded version explicitly and compare
        Trade overloadedResult = position.operate(testIndex, NaN.NaN, NaN.NaN);
        assertEquals("operate(int) must delegate to operate(int, NaN, NaN) and return the same Trade", overloadedResult, simpleResult);
    }
}
