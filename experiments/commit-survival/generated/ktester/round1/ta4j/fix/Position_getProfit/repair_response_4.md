```java
package org.ta4j.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

/**
 * Tests for {@link Position#getProfit()}.
 */
@ExtendWith(MockitoExtension.class)
class Position_getProfit_Test {

    /**
     * Cost model applied to every trade (1 % of the trade value).
     */
    private CostModel transactionModel;

    /**
     * Holding‑cost model (0.1 % per bar).
     */
    private CostModel holdingModel;

    /**
     * A BUY trade used as entry in long‑position tests.
     */
    private Trade longEntry;

    /**
     * A SELL trade used as exit in long‑position tests.
     */
    private Trade longExit;

    /**
     * A SELL trade used as entry in short‑position tests.
     */
    private Trade shortEntry;

    /**
     * A BUY trade used as exit in short‑position tests.
     */
    private Trade shortExit;

    @BeforeEach
    void setupBeforeEach() {
        // Initialise the cost models used by all tests.
        // 1 %
        transactionModel = new LinearTransactionCostModel(0.01);
        // 0.1 %
        holdingModel = new LinearBorrowingCostModel(0.001);
        // Long (BUY → SELL) trades – same price so gross profit = 0.
        longEntry = Trade.buyAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        longExit = Trade.sellAt(2, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        // Short (SELL → BUY) trades – same price so gross profit = 0.
        shortEntry = Trade.sellAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        shortExit = Trade.buyAt(2, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
    }

    /* -------------------------------------------------------------
     *  Parameterised tests for closed long positions (ZeroCostModel)
     * ------------------------------------------------------------- */
    private static Stream<Arguments> closedLongZeroCostProvider() {
        return Stream.of(
                // profit case
                Arguments.of(DoubleNum.valueOf(20), DoubleNum.valueOf(25), DoubleNum.valueOf(50), DoubleNum.valueOf(250)),
                // breakeven case
                Arguments.of(DoubleNum.valueOf(15), DoubleNum.valueOf(15), DoubleNum.valueOf(100), DoubleNum.valueOf(0)),
                // loss case
                Arguments.of(DoubleNum.valueOf(40), DoubleNum.valueOf(35), DoubleNum.valueOf(30), DoubleNum.valueOf(-150))
        );
    }

    /* -------------------------------------------------------------
     *  Parameterised tests for closed short positions (ZeroCostModel)
     * ------------------------------------------------------------- */
    private static Stream<Arguments> closedShortZeroCostProvider() {
        return Stream.of(
                // profit case (price falls)
                Arguments.of(DoubleNum.valueOf(60), DoubleNum.valueOf(52), DoubleNum.valueOf(80), DoubleNum.valueOf(640)),
                // loss case (price rises)
                Arguments.of(DoubleNum.valueOf(45), DoubleNum.valueOf(50), DoubleNum.valueOf(70), DoubleNum.valueOf(-350))
        );
    }

    @Test
    void getProfit_whenPositionOpened_returnsZero() {
        // A brand‑new position (default BUY) that has only been entered.
        Position opened = new Position();
        opened.operate(0);
        Num profit = opened.getProfit();
        assertEquals(DoubleNum.valueOf(0), profit);
    }

    @Test
    void getProfit_whenClosedLongPosition_returnsGrossProfitMinusTransactionCosts() {
        // Long position: BUY then SELL at the same price.
        Position closedLong = new Position(longEntry, longExit, transactionModel, holdingModel);
        Num profit = closedLong.getProfit();
        // Expected profit = 0 – (2 * 0.01 * 1) * 2 = -0.04
        assertEquals(DoubleNum.valueOf(-0.04), profit);
    }

    @Test
    void getProfit_whenClosedShortPosition_returnsGrossProfitMinusCostsAndHoldingCosts() {
        // Short position: SELL then BUY at the same price, held for 9 bars (indices 1 → 10).
        Position closedShort = new Position(shortEntry, shortExit, transactionModel, holdingModel);
        Num profit = closedShort.getProfit();
        // holding cost = 2 * 9 * 0.001 = 0.018
        Num expectedHoldingCost = DoubleNum.valueOf(2.0 * 9.0 * 0.001);
        // expected profit = -0.04 - holdingCost
        Num expectedProfit = DoubleNum.valueOf(-0.04).minus(expectedHoldingCost);
        assertEquals(expectedProfit, profit);
    }

    @Test
    void getProfit_whenOpenShortPosition_returnsZero() {
        // Open a short position (starting type SELL) without an exit.
        Position openedShort = new Position(TradeType.SELL, transactionModel, holdingModel);
        openedShort.operate(0);
        Num profit = openedShort.getProfit();
        assertEquals(DoubleNum.valueOf(0), profit);
    }

    @ParameterizedTest
    @MethodSource("closedLongZeroCostProvider")
    void getProfit_closedLongZeroCostModel_returnsExpected(Num entryPrice, Num exitPrice, Num amount, Num expected) {
        CostModel zeroCost = new ZeroCostModel();
        Trade entry = Trade.buyAt(0, entryPrice, amount, zeroCost);
        Trade exit = Trade.sellAt(1, exitPrice, amount, zeroCost);
        Position position = new Position(entry, exit, zeroCost, zeroCost);
        Num profit = position.getProfit();
        assertEquals(expected, profit);
    }

    @ParameterizedTest
    @MethodSource("closedShortZeroCostProvider")
    void getProfit_closedShortZeroCostModel_returnsExpected(Num entryPrice, Num exitPrice, Num amount, Num expected) {
        CostModel zeroCost = new ZeroCostModel();
        Trade entry = Trade.sellAt(0, entryPrice, amount, zeroCost);
        Trade exit = Trade.buyAt(1, exitPrice, amount, zeroCost);
        Position position = new Position(entry, exit, zeroCost, zeroCost);
        Num profit = position.getProfit();
        assertEquals(expected, profit);
    }

    @Test
    void getProfit_closedLongWithProportionalCost_returnsProfitMinusCost() {
        // 2 % transaction cost on both entry and exit
        CostModel percentCost = new LinearTransactionCostModel(0.02);
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(30), DoubleNum.valueOf(200), percentCost);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(33), DoubleNum.valueOf(200), percentCost);
        // PositionCost = (30*200*0.02) + (33*200*0.02) = 252
        // GrossProfit = (33‑30)*200 = 600
        // Expected profit = 600 – 252 = 348
        Position position = new Position(entry, exit, percentCost, new ZeroCostModel());
        Num profit = position.getProfit();
        assertEquals(DoubleNum.valueOf(348), profit);
    }

    @Test
    void getProfit_closedPositionWithNaNExitPrice_returnsNaN() {
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), new ZeroCostModel());
        Trade exit = Trade.sellAt(1, DoubleNum.NaN, DoubleNum.valueOf(10), new ZeroCostModel());
        Position position = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
        Num profit = position.getProfit();
        assertTrue(profit.isNaN(), "Profit should be NaN when exit price is NaN");
    }

    @Test
    void getProfit_closedPositionWithZeroAmount_returnsMinusPositionCost() {
        // amount = 0, but a transaction cost of 7 % is forced via a custom model
        CostModel customCost = new LinearTransactionCostModel(0.07);
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(0), customCost);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(120), DoubleNum.valueOf(0), customCost);
        Position position = new Position(entry, exit, customCost, new ZeroCostModel());
        // Expected profit = 0 – 7 = -7 (the custom model returns a non‑zero cost for zero amount)
        Num profit = position.getProfit();
        assertEquals(DoubleNum.valueOf(-7), profit);
    }

    @Test
    void getProfit_exitNullAndIsOpenedFalse_throwsNullPointerException() throws Exception {
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        Position real = new Position(entry, exit, transactionModel, holdingModel);
        Position spy = spy(real);
        doReturn(false).when(spy).isOpened();
        Field exitField = Position.class.getDeclaredField("exit");
        exitField.setAccessible(true);
        exitField.set(spy, null);
        assertThrows(NullPointerException.class, spy::getProfit,
                "Calling getProfit() on a position whose exit is null should throw NPE");
    }

    @Test
    void getProfit_exitPricePerAssetNull_throwsNullPointerException() {
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        Trade exitMock = mock(Trade.class);
        when(exitMock.getPricePerAsset()).thenReturn(null);
        when(exitMock.getAmount()).thenReturn(DoubleNum.valueOf(1));
        when(exitMock.getType()).thenReturn(Trade.TradeType.SELL);
        Position position = new Position(entry, exitMock, transactionModel, holdingModel);
        assertThrows(NullPointerException.class, position::getProfit,
                "Profit calculation should NPE when exit price per asset is null");
    }

    @Test
    void getProfit_negativeExitPrice_throwsIllegalArgumentException() {
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(-5), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);
        assertThrows(IllegalArgumentException.class, position::getProfit,
                "Negative exit price should cause IllegalArgumentException");
    }

    @Test
    void getProfit_nullCostModel_throwsNullPointerException() {
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);
        Position spy = spy(position);
        doThrow(new NullPointerException("cost model is null")).when(spy).getPositionCost();
        assertThrows(NullPointerException.class, spy::getProfit,
                "If the internal cost model is null, getProfit() should propagate NPE");
    }
}
```