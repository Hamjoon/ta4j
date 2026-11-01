```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.*;
import org.ta4j.core.num.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;

/* --------------------------------------------------------------------- */
@ExtendWith(MockitoExtension.class)
class Position_getProfit_2_Test {

    private CostModel transactionModel;
    private CostModel holdingModel;

    // BUY
    private Trade longEntry;
    // SELL
    private Trade longExit;
    // SELL
    private Trade shortEntry;
    // BUY
    private Trade shortExit;

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

    /** Helper to build a closed long position with arbitrary entry/exit data. */
    private static Position buildClosedLong(double entryPrice, double exitPrice, int entryIdx, int exitIdx,
                                            double amount, CostModel txModel, CostModel holdModel) {
        Trade entry = Trade.buyAt(entryIdx, DoubleNum.valueOf(entryPrice), DoubleNum.valueOf(amount), txModel);
        Trade exit = Trade.sellAt(exitIdx, DoubleNum.valueOf(exitPrice), DoubleNum.valueOf(amount), txModel);
        return new Position(entry, exit, txModel, holdModel);
    }

    /** Helper to build an open long position (only entry). */
    private static Position buildOpenLong(double entryPrice, int entryIdx, double amount,
                                          CostModel txModel, CostModel holdModel) {
        Position pos = new Position(Trade.TradeType.BUY, txModel, holdModel);
        pos.operate(entryIdx, DoubleNum.valueOf(entryPrice), DoubleNum.valueOf(amount));
        return pos;
    }

    /** Helper to build an open short position (only entry). */
    private static Position buildOpenShort(double entryPrice, int entryIdx, double amount,
                                           CostModel txModel, CostModel holdModel) {
        Position pos = new Position(Trade.TradeType.SELL, txModel, holdModel);
        pos.operate(entryIdx, DoubleNum.valueOf(entryPrice), DoubleNum.valueOf(amount));
        return pos;
    }

    private static Stream<Arguments> basicProfitProvider() {
        // Positive profit case
        Position posPos = buildClosedLong(50, 200, 1, 10, 1,
                new LinearTransactionCostModel(0.01), new LinearBorrowingCostModel(0.001));
        Num expectedPositive = posPos.getProfit();

        // Zero profit case (gross == cost)
        CostModel expensiveTx = new LinearTransactionCostModel(0.2);
        Position posZeroExact = buildClosedLong(100, 120, 1, 5, 1,
                expensiveTx, new ZeroCostModel());
        Num expectedZero = posZeroExact.getProfit();

        // Negative profit case (cost > gross)
        CostModel highTx = new LinearTransactionCostModel(0.5);
        Position posNeg = buildClosedLong(100, 110, 1, 5, 1,
                highTx, new ZeroCostModel());
        Num expectedNeg = posNeg.getProfit();

        return Stream.of(
                Arguments.of("positive", posPos, 10, DoubleNum.valueOf(200), expectedPositive),
                Arguments.of("zero", posZeroExact, 5, DoubleNum.valueOf(120), expectedZero),
                Arguments.of("negative", posNeg, 5, DoubleNum.valueOf(110), expectedNeg)
        );
    }

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
        openLong.operate(5, DoubleNum.valueOf(2), DoubleNum.valueOf(1));

        Num finalPrice = DoubleNum.valueOf(3);
        int finalIndex = 10;

        Num grossProfit = finalPrice.minus(DoubleNum.valueOf(2)).multipliedBy(DoubleNum.valueOf(1));
        Num transactionCost = DoubleNum.valueOf(2).multipliedBy(DoubleNum.valueOf(0.01));
        Num holdingCost = DoubleNum.valueOf(2)
                .multipliedBy(DoubleNum.valueOf(1))
                .multipliedBy(DoubleNum.valueOf(0.001))
                .multipliedBy(DoubleNum.valueOf(finalIndex - 5));

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
        openShort.operate(5, DoubleNum.valueOf(2), DoubleNum.valueOf(1));

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

    @ParameterizedTest(name = "basic profit [{0}]")
    @MethodSource("basicProfitProvider")
    void testBasicProfitCalculations(String label, Position position, int finalIndex,
                                     Num finalPrice, Num expected) {
        Num actual = position.getProfit(finalIndex, finalPrice);
        assertNumEquals(expected, actual);
    }

    @Test
    void whenNullFinalPrice_thenNullPointerException() {
        Position pos = buildClosedLong(100, 150, 1, 10, 1, transactionModel, holdingModel);
        assertThrows(NullPointerException.class, () -> pos.getProfit(10, null));
    }

    @Test
    void whenNegativeFinalIndex_thenIllegalArgumentException() {
        Position pos = buildClosedLong(100, 150, 1, 10, 1, transactionModel, holdingModel);
        assertThrows(IllegalArgumentException.class, () -> pos.getProfit(-1, DoubleNum.valueOf(100)));
    }

    @Test
    void whenFinalIndexBeyondSeries_thenException() {
        Position pos = buildClosedLong(100, 150, 1, 10, 1, transactionModel, holdingModel);
        assertThrows(RuntimeException.class, () -> pos.getProfit(999_999, DoubleNum.valueOf(100)));
    }

    @Test
    void whenZeroCostModel_profitEqualsGrossProfit() {
        CostModel zero = new ZeroCostModel();
        Position pos = buildClosedLong(90, 120, 1, 8, 1, zero, zero);
        Num expectedGross = DoubleNum.valueOf(120).minus(DoubleNum.valueOf(90)).multipliedBy(DoubleNum.valueOf(1));
        Num actual = pos.getProfit(8, DoubleNum.valueOf(120));
        assertNumEquals(expectedGross, actual);
    }

    @Test
    void whenFinalIndexEqualsEntryIndex_breakEvenZeroProfit() {
        Position pos = buildOpenLong(50, 2, 1, transactionModel, holdingModel);
        Num actual = pos.getProfit(2, DoubleNum.valueOf(50));
        assertNumEquals(DoubleNum.valueOf(0), actual);
    }

    @Test
    void whenFinalPriceIsNaN_resultIsNaN() {
        Position pos = buildOpenLong(100, 3, 1, transactionModel, holdingModel);
        Num nan = DoubleNum.NaN;
        Num actual = pos.getProfit(7, nan);
        assertTrue(actual.isNaN(), "Profit should be NaN when finalPrice is NaN");
    }

    @Test
    void whenOpenPosition_calculateProfitIncludingHoldingCost() {
        Position pos = buildOpenLong(150, 5, 1, transactionModel, holdingModel);
        int finalIndex = 15;
        Num finalPrice = DoubleNum.valueOf(200);

        Num grossProfit = finalPrice.minus(DoubleNum.valueOf(150));
        Num transactionCost = DoubleNum.valueOf(150).multipliedBy(DoubleNum.valueOf(0.01));
        Num holdingCost = DoubleNum.valueOf(150)
                .multipliedBy(DoubleNum.valueOf(1))
                .multipliedBy(DoubleNum.valueOf(0.001))
                .multipliedBy(DoubleNum.valueOf(finalIndex - 5));

        Num expected = grossProfit.minus(transactionCost).minus(holdingCost);
        Num actual = pos.getProfit(finalIndex, finalPrice);
        assertNumEquals(expected, actual);
    }

    @Test
    void whenHighPrecisionFinalPrice_preservePrecision() {
        Position pos = buildClosedLong(100, 123.456789, 1, 12, 1,
                new LinearTransactionCostModel(0.004), new ZeroCostModel());
        Num expected = pos.getProfit(12, DoubleNum.valueOf(123.456789));
        Num actual = pos.getProfit(12, DoubleNum.valueOf(123.456789));
        assertNumEquals(expected, actual);
    }

    @Test
    void whenPositionNeverOpened_thenIllegalStateException() {
        Position pos = new Position(Trade.TradeType.BUY, transactionModel, holdingModel);
        assertThrows(IllegalStateException.class, () -> pos.getProfit(5, DoubleNum.valueOf(100)));
    }

    @Test
    void whenNegativeFinalPrice_thenIllegalArgumentException() {
        Position pos = buildClosedLong(100, 150, 1, 10, 1, transactionModel, holdingModel);
        assertThrows(IllegalArgumentException.class, () -> pos.getProfit(10, DoubleNum.valueOf(-50)));
    }

    @Test
    void whenNullCostModel_thenNullPointerException() {
        Position pos = new Position(longEntry, longExit, null, holdingModel);
        assertThrows(NullPointerException.class, () -> pos.getProfit(8, DoubleNum.valueOf(120)));
    }

    @Test
    void whenFinalIndexBeforeEntry_thenIllegalArgumentException() {
        Position pos = buildOpenLong(100, 5, 1, transactionModel, holdingModel);
        assertThrows(IllegalArgumentException.class, () -> pos.getProfit(2, DoubleNum.valueOf(100)));
    }
}
```