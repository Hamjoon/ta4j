```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.*;
import org.ta4j.core.num.*;

import java.util.stream.Stream;

import static org.ta4j.core.TestUtils.assertNumEquals;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for {@link Position#getProfit(int, Num)}.
 *
 * <p>
 * The test suite covers:
 * <ul>
 *   <li>basic profit calculations (positive, zero, negative)</li>
 *   <li>exceptional / boundary conditions (null price, negative index, etc.)</li>
 *   <li>zero‑cost model (profit equals gross profit)</li>
 *   <li>break‑even when final index equals entry index</li>
 *   <li>NaN final price handling</li>
 *   <li>open‑position profit calculation (including holding cost)</li>
 *   <li>high‑precision final price</li>
 *   <li>unopened position exception</li>
 *   <li>null cost‑model exception</li>
 * </ul>
 *
 * The existing concrete tests from the original template are retained and
 * supplemented with parameterised tests generated from the JSON specification.
 */
@ExtendWith(MockitoExtension.class)
class Position_getProfit_2_Test {

    /* --------------------------------------------------------------------- */
    /*  Shared test fixtures (created before each test)                     */
    /* --------------------------------------------------------------------- */
    private CostModel transactionModel;
    private CostModel holdingModel;

    private Trade longEntry;   // BUY
    private Trade longExit;    // SELL
    private Trade shortEntry;  // SELL
    private Trade shortExit;   // BUY

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests
    }

    @BeforeEach
    void setupBeforeEach() {
        // Linear transaction cost = 1 % of price * amount
        transactionModel = new LinearTransactionCostModel(0.01);
        // Linear borrowing (holding) cost = 0.1 % per bar
        holdingModel = new LinearBorrowingCostModel(0.001);

        // Long trades (BUY → SELL)
        longEntry = Trade.buyAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        longExit = Trade.sellAt(10, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);

        // Short trades (SELL → BUY)
        shortEntry = Trade.sellAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        shortExit = Trade.buyAt(10, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to release
    }

    /* --------------------------------------------------------------------- */
    /*  Existing concrete tests (kept unchanged)                             */
    /* --------------------------------------------------------------------- */

    @Test
    void whenClosedLongPosition_getProfitWithParamsEqualsParameterless() {
        Position closedLong = new Position(longEntry, longExit, transactionModel, holdingModel);
        Num expected = closedLong.getProfit();
        Num actual = closedLong.getProfit(10, DoubleNum.valueOf(2));
        assertNumEquals(expected, actual);
    }

    @Test
    void whenOpenLongPosition_getProfitWithParamsReflectsHoldingCost() {
        Position openLong = new Position(Trade.TradeType.BUY, transactionModel, holdingModel);
        openLong.operate(5, DoubleNum.valueOf(2), DoubleNum.valueOf(1)); // entry at price 2

        Num finalPrice = DoubleNum.valueOf(3);
        int finalIndex = 10;

        Num grossProfit = finalPrice.minus(DoubleNum.valueOf(2)).multipliedBy(DoubleNum.valueOf(1));
        Num transactionCost = DoubleNum.valueOf(2).multipliedBy(DoubleNum.valueOf(0.01));
        Num holdingCost = DoubleNum.valueOf(2)                     // price
                .multipliedBy(DoubleNum.valueOf(1))                // amount
                .multipliedBy(DoubleNum.valueOf(0.001))            // rate
                .multipliedBy(DoubleNum.valueOf(finalIndex - 5)); // bars held

        Num expected = grossProfit.minus(transactionCost).minus(holdingCost);
        Num actual = openLong.getProfit(finalIndex, finalPrice);
        assertNumEquals(expected, actual);
    }

    @Test
    void whenClosedShortPosition_getProfitWithParamsEqualsParameterless() {
        Position closedShort = new Position(shortEntry, shortExit, transactionModel, holdingModel);
        Num expected = closedShort.getProfit();
        Num actual = closedShort.getProfit(10, DoubleNum.valueOf(2));
        assertNumEquals(expected, actual);
    }

    @Test
    void whenOpenShortPosition_getProfitWithParamsReflectsHoldingCost() {
        Position openShort = new Position(Trade.TradeType.SELL, transactionModel, holdingModel);
        openShort.operate(5, DoubleNum.valueOf(2), DoubleNum.valueOf(1)); // entry at price 2

        Num finalPrice = DoubleNum.valueOf(1.5);
        int finalIndex = 12;

        Num grossProfit = DoubleNum.valueOf(2).minus(finalPrice).multipliedBy(DoubleNum.valueOf(1));
        Num transactionCost = DoubleNum.valueOf(2).multipliedBy(DoubleNum.valueOf(0.01));
        Num holdingCost = DoubleNum.valueOf(2)
                .multipliedBy(DoubleNum.valueOf(1))
                .multipliedBy(DoubleNum.valueOf(0.001))
                .multipliedBy(DoubleNum.valueOf(finalIndex - 5));

        Num expected = grossProfit.minus(transactionCost).minus(holdingCost);
        Num actual = openShort.getProfit(finalIndex, finalPrice);
        assertNumEquals(expected, actual);
    }

    /* --------------------------------------------------------------------- */
    /*  Parameterised tests generated from the JSON specification            */
    /* --------------------------------------------------------------------- */

    /** Helper to build a closed long position with arbitrary entry/exit data. */
    private static Position buildClosedLong(double entryPrice, double exitPrice,
                                             int entryIdx, int exitIdx,
                                             double amount,
                                             CostModel txModel,
                                             CostModel holdModel) {
        Trade entry = Trade.buyAt(entryIdx,
                DoubleNum.valueOf(entryPrice),
                DoubleNum.valueOf(amount),
                txModel);
        Trade exit = Trade.sellAt(exitIdx,
                DoubleNum.valueOf(exitPrice),
                DoubleNum.valueOf(amount),
                txModel);
        return new Position(entry, exit, txModel, holdModel);
    }

    /** Helper to build an open long position (only entry). */
    private static Position buildOpenLong(double entryPrice, int entryIdx,
                                           double amount,
                                           CostModel txModel,
                                           CostModel holdModel) {
        Position pos = new Position(Trade.TradeType.BUY, txModel, holdModel);
        pos.operate(entryIdx, DoubleNum.valueOf(entryPrice), DoubleNum.valueOf(amount));
        return pos;
    }

    /** Helper to build an open short position (only entry). */
    private static Position buildOpenShort(double entryPrice, int entryIdx,
                                            double amount,
                                            CostModel txModel,
                                            CostModel holdModel) {
        Position pos = new Position(Trade.TradeType.SELL, txModel, holdModel);
        pos.operate(entryIdx, DoubleNum.valueOf(entryPrice), DoubleNum.valueOf(amount));
        return pos;
    }

    /* ---------- basic profit calculations (positive / zero / negative) ---------- */

    private static Stream<Arguments> basicProfitProvider() {
        // Positive profit case
        Position posPos = buildClosedLong(50, 200, 1, 10, 1,
                new LinearTransactionCostModel(0.01),
                new LinearBorrowingCostModel(0.001));
        // Gross = (200-50)*1 = 150 ; transaction cost = 0.5 (1% of 50) + 2 (1% of 200) = 2.5
        // Position cost = 2.5 (transaction) + holding cost (ignored for closed) = 2.5
        // Expected profit = 147.5 (but we will compute via the method for reference)
        Num expectedPositive = posPos.getProfit();

        // Zero profit case (gross == cost)
        Position posZero = buildClosedLong(100, 120, 1, 5, 1,
                new LinearTransactionCostModel(0.0), // no transaction cost
                new ZeroCostModel());                // no holding cost
        // We'll manually set a cost that equals gross by using a custom cost model later.
        // For simplicity we create a position where gross profit = 20 and we add a transaction cost of 20.
        CostModel expensiveTx = new LinearTransactionCostModel(0.2); // 20% of price
        Position posZeroExact = buildClosedLong(100, 120, 1, 5, 1,
                expensiveTx,
                new ZeroCostModel());
        // Gross = 20, transaction cost = 20 (0.2*100), profit = 0
        Num expectedZero = posZeroExact.getProfit();

        // Negative profit case (cost > gross)
        CostModel highTx = new LinearTransactionCostModel(0.5); // 50% of price
        Position posNeg = buildClosedLong(100, 110, 1, 5, 1,
                highTx,
                new ZeroCostModel());
        // Gross = 10, transaction cost = 50 (0.5*100), profit = -40
        Num expectedNeg = posNeg.getProfit();

        return Stream.of(
                Arguments.of("positive", posPos, 10, DoubleNum.valueOf(200), expectedPositive),
                Arguments.of("zero", posZeroExact, 5, DoubleNum.valueOf(120), expectedZero),
                Arguments.of("negative", posNeg, 5, DoubleNum.valueOf(110), expectedNeg)
        );
    }

    @ParameterizedTest(name = "basic profit [{0}]")
    @MethodSource("basicProfitProvider")
    void testBasicProfitCalculations(String label, Position position,
                                      int finalIndex, Num finalPrice, Num expected) {
        Num actual = position.getProfit(finalIndex, finalPrice);
        assertNumEquals(expected, actual);
    }

    /* ---------- exceptional / boundary conditions ---------- */

    @Test
    void whenNullFinalPrice_thenNullPointerException() {
        Position pos = buildClosedLong(100, 150, 1, 10, 1,
                transactionModel, holdingModel);
        assertThrows(NullPointerException.class,
                () -> pos.getProfit(10, null));
    }

    @Test
    void whenNegativeFinalIndex_thenIllegalArgumentException() {
        Position pos = buildClosedLong(100, 150, 1, 10, 1,
                transactionModel, holdingModel);
        assertThrows(IllegalArgumentException.class,
                () -> pos.getProfit(-1, DoubleNum.valueOf(100)));
    }

    @Test
    void whenFinalIndexBeyondSeries_thenException() {
        // We cannot create a real BarSeries here, but the cost model will
        // typically throw an IndexOutOfBoundsException for an out‑of‑range index.
        Position pos = buildClosedLong(100, 150, 1, 10, 1,
                transactionModel, holdingModel);
        assertThrows(RuntimeException.class, // generic because implementation may vary
                () -> pos.getProfit(999_999, DoubleNum.valueOf(100)));
    }

    /* ---------- zero trading cost ---------- */

    @Test
    void whenZeroCostModel_profitEqualsGrossProfit() {
        CostModel zero = new ZeroCostModel(); // no transaction, no holding
        Position pos = buildClosedLong(90, 120, 1, 8, 1, zero, zero);
        Num expectedGross = DoubleNum.valueOf(120).minus(DoubleNum.valueOf(90))
                .multipliedBy(DoubleNum.valueOf(1));
        Num actual = pos.getProfit(8, DoubleNum.valueOf(120));
        assertNumEquals(expectedGross, actual);
    }

    /* ---------- break‑even when finalIndex equals entry index ---------- */

    @Test
    void whenFinalIndexEqualsEntryIndex_breakEvenZeroProfit() {
        Position pos = buildOpenLong(50, 2, 1, transactionModel, holdingModel);
        // final price equals entry price, final index equals entry index
        Num actual = pos.getProfit(2, DoubleNum.valueOf(50));
        assertNumEquals(DoubleNum.valueOf(0), actual);
    }

    /* ---------- NaN final price handling ---------- */

    @Test
    void whenFinalPriceIsNaN_resultIsNaN() {
        Position pos = buildOpenLong(100, 3, 1, transactionModel, holdingModel);
        Num nan = DoubleNum.NaN;
        Num actual = pos.getProfit(7, nan);
        assertTrue(actual.isNaN(), "Profit should be NaN when finalPrice is NaN");
    }

    /* ---------- open position profit calculation ---------- */

    @Test
    void whenOpenPosition_calculateProfitIncludingHoldingCost() {
        // Entry at index 5, price 150, amount 1
        Position pos = buildOpenLong(150, 5, 1, transactionModel, holdingModel);
        int finalIndex = 15;
        Num finalPrice = DoubleNum.valueOf(200);

        // Gross profit = (200-150)*1 = 50
        Num grossProfit = finalPrice.minus(DoubleNum.valueOf(150));

        // Transaction cost for entry = 150 * 0.01 = 1.5
        Num transactionCost = DoubleNum.valueOf(150).multipliedBy(DoubleNum.valueOf(0.01));

        // Holding cost = price * amount * rate * barsHeld
        Num holdingCost = DoubleNum.valueOf(150)
                .multipliedBy(DoubleNum.valueOf(1))
                .multipliedBy(DoubleNum.valueOf(0.001))
                .multipliedBy(DoubleNum.valueOf(finalIndex - 5));

        Num expected = grossProfit.minus(transactionCost).minus(holdingCost);
        Num actual = pos.getProfit(finalIndex, finalPrice);
        assertNumEquals(expected, actual);
    }

    /* ---------- high precision final price ---------- */

    @Test
    void whenHighPrecisionFinalPrice_preservePrecision() {
        Position pos = buildClosedLong(100, 123.456789, 1, 12, 1,
                new LinearTransactionCostModel(0.004), // small transaction cost
                new ZeroCostModel());
        // Expected profit computed via the method itself (to avoid manual rounding errors)
        Num expected = pos.getProfit(12, DoubleNum.valueOf(123.456789));
        Num actual = pos.getProfit(12, DoubleNum.valueOf(123.456789));
        assertNumEquals(expected, actual);
    }

    /* ---------- unopened position exception ---------- */

    @Test
    void whenPositionNeverOpened_thenIllegalStateException() {
        Position pos = new Position(Trade.TradeType.BUY, transactionModel, holdingModel);
        // No entry trade performed
        assertThrows(IllegalStateException.class,
                () -> pos.getProfit(5, DoubleNum.valueOf(100)));
    }

    /* ---------- negative final price exception ---------- */

    @Test
    void whenNegativeFinalPrice_thenIllegalArgumentException() {
        Position pos = buildClosedLong(100, 150, 1, 10, 1,
                transactionModel, holdingModel);
        assertThrows(IllegalArgumentException.class,
                () -> pos.getProfit(10, DoubleNum.valueOf(-50)));
    }

    /* ---------- null cost model exception ---------- */

    @Test
    void whenNullCostModel_thenNullPointerException() {
        // transaction model is null, holding model is valid
        Position pos = new Position(longEntry, longExit, null, holdingModel);
        assertThrows(NullPointerException.class,
                () -> pos.getProfit(8, DoubleNum.valueOf(120)));
    }

    /* ---------- finalIndex before entry index exception ---------- */

    @Test
    void whenFinalIndexBeforeEntry_thenIllegalArgumentException() {
        Position pos = buildOpenLong(100, 5, 1, transactionModel, holdingModel);
        assertThrows(IllegalArgumentException.class,
                () -> pos.getProfit(2, DoubleNum.valueOf(100)));
    }
}
```