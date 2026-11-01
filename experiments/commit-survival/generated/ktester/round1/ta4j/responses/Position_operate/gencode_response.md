```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.num.NaN.NaN;

import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Additional unit tests for {@link Position#operate(int)} covering the
 * scenarios described in the JSON test‑case specification.
 *
 * <p>These tests complement the ones already present in the framework and
 * focus on return values, edge‑case handling and delegation behaviour.</p>
 */
@ExtendWith(MockitoExtension.class)
class Position_operate_Test {

    /* --------------------------------------------------------------------- */
    /*  Shared fixtures                                                      */
    /* --------------------------------------------------------------------- */
    private CostModel transactionModel;
    private CostModel holdingModel;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Simple linear cost models – they are deterministic and cheap.
        transactionModel = new LinearTransactionCostModel(0.01);
        holdingModel = new LinearBorrowingCostModel(0.001);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up after each test.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to release.
    }

    /* --------------------------------------------------------------------- */
    /*  Existing tests (kept unchanged)                                     */
    /* --------------------------------------------------------------------- */

    @Test
    void whenNewPositionOperate_thenCreatesBuyEntryWithNaNValues() {
        // given
        Position position = new Position(); // default starting type = BUY

        // when
        position.operate(0);

        // then
        Trade expectedEntry = Trade.buyAt(0, NaN, NaN);
        assertEquals(expectedEntry, position.getEntry(),
                "A newly created Position should generate a BUY entry trade with NaN price/amount");
    }

    @Test
    void whenPositionAlreadyOpenedOperate_thenCreatesComplementaryExit() {
        // given
        Position position = new Position(); // BUY entry expected
        position.operate(0); // creates entry

        // when
        position.operate(1); // should create SELL exit

        // then
        Trade expectedExit = Trade.sellAt(1, NaN, NaN);
        assertEquals(expectedExit, position.getExit(),
                "After an entry trade, a second operate() must create the complementary exit trade");
    }

    @Test
    void whenOperateCalledOnClosedPosition_itDoesNotAlterExistingTrades() {
        // given
        Position position = new Position(); // BUY entry
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
    void whenSellPositionOperateWithPriceAndAmount_thenCreatesSellEntry() {
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
    void whenBuyPositionOperateWithPriceAndAmount_thenCreatesBuyEntry() {
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
    void whenOpenedSellPositionOperateAgainWithPriceAndAmount_thenCreatesBuyExit() {
        // given
        Position position = new Position(TradeType.SELL, transactionModel, holdingModel);
        position.operate(0, DoubleNum.valueOf(3), DoubleNum.valueOf(2)); // entry (SELL)

        // when – second call should create the complementary BUY exit
        position.operate(4, DoubleNum.valueOf(3.5), DoubleNum.valueOf(2));

        // then
        Trade expectedExit = Trade.buyAt(4, DoubleNum.valueOf(3.5), DoubleNum.valueOf(2), transactionModel);
        assertEquals(expectedExit, position.getExit(),
                "After a SELL entry, the next operate() must create a BUY exit with the given price/amount");
    }

    @Test
    void whenOpenedBuyPositionOperateAgainWithPriceAndAmount_thenCreatesSellExit() {
        // given
        Position position = new Position(TradeType.BUY, transactionModel, holdingModel);
        position.operate(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1)); // entry (BUY)

        // when – second call should create the complementary SELL exit
        position.operate(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1));

        // then
        Trade expectedExit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        assertEquals(expectedExit, position.getExit(),
                "After a BUY entry, the next operate() must create a SELL exit with the given price/amount");
    }

    @Test
    void whenOperateCalledWithNaNValues_itFallsBackToDefaultBehaviour() {
        // given
        Position position = new Position(); // BUY start
        // when
        position.operate(0, NaN, NaN);
        // then
        Trade expectedEntry = Trade.buyAt(0, NaN, NaN);
        assertEquals(expectedEntry, position.getEntry(),
                "Passing NaN for price/amount must behave like the simple operate(int) overload");
    }

    /* --------------------------------------------------------------------- */
    /*  New tests derived from the JSON specification                        */
    /* --------------------------------------------------------------------- */

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
    @CsvSource({
            // index,   preSetup (none=0, entryFirst=1), expectedType (BUY/SELL)
            "5, 0, BUY",   // fresh position, entry at 5
            "0, 0, BUY",   // fresh position, entry at 0
            "10, 1, SELL"  // entry already created, exit at 10
    })
    void whenOperateValidScenarios_returnExpectedTrade(int index, int preSetup, String expectedType) {
        // given
        Position position = new Position(); // default BUY start

        // optional pre‑setup: create an entry at index 5 when required
        if (preSetup == 1) {
            position.operate(5); // creates entry (BUY) at 5
        }

        // when
        Trade result = position.operate(index);

        // then
        Trade expected;
        if ("BUY".equals(expectedType)) {
            expected = Trade.buyAt(index, NaN, NaN);
        } else {
            expected = Trade.sellAt(index, NaN, NaN);
        }
        assertEquals(expected, result,
                () -> "operate(" + index + ") should return a " + expectedType + " trade");
    }

    @Test
    void whenOperateWithNegativeIndex_throwsIndexOutOfBoundsException() {
        Position position = new Position();
        assertThrows(IndexOutOfBoundsException.class,
                () -> position.operate(-1),
                "Negative index must cause IndexOutOfBoundsException");
    }

    @Test
    void whenOperateWithHugeIndex_throwsIndexOutOfBoundsException() {
        Position position = new Position();
        // Assuming a realistic series size is far smaller than 1000;
        // the implementation should guard against out‑of‑range indexes.
        assertThrows(IndexOutOfBoundsException.class,
                () -> position.operate(1000),
                "Index far beyond series size must cause IndexOutOfBoundsException");
    }

    @Test
    void whenOperateWithNullInteger_throwsIllegalArgumentException() throws NoSuchMethodException {
        // The method signature uses primitive int, so we have to invoke it via reflection
        // to simulate a “null” argument (which results in an IllegalArgumentException).
        Method method = Position.class.getMethod("operate", int.class);
        Position position = new Position();

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
                () -> method.invoke(position, new Object[]{null}),
                "Invoking operate(int) with null via reflection should wrap an IllegalArgumentException");

        assertTrue(thrown.getCause() instanceof IllegalArgumentException,
                "Root cause must be IllegalArgumentException");
    }

    @Test
    void whenPositionIsNew_operateReturnsNull() {
        // given – a brand‑new Position (no entry yet)
        Position position = new Position();

        // when
        Trade result = position.operate(2);

        // then
        assertNull(result, "operate on a NEW position should return null (no trade created yet)");
    }

    @Test
    void whenPositionAlreadyClosed_operateReturnsNull() {
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
    void whenIndexInsideOpenedPositionButNotEntryOrExit_operateReturnsNull() {
        // given – only entry trade exists
        Position position = new Position();
        position.operate(3); // entry at index 3

        // when – call operate with an intermediate index (neither entry nor exit)
        Trade result = position.operate(7);

        // then
        assertNull(result, "operate on an intermediate index of an opened position must return null");
    }

    @Test
    void whenOperateDelegatesToOverloadedMethod_resultMatchesOverload() {
        // given
        Position position = new Position();
        int testIndex = 4;

        // when – call the simple overload
        Trade simpleResult = position.operate(testIndex);

        // then – call the overloaded version explicitly and compare
        Trade overloadedResult = position.operate(testIndex, NaN, NaN);
        assertEquals(overloadedResult, simpleResult,
                "operate(int) must delegate to operate(int, NaN, NaN) and return the same Trade");
    }
}
```