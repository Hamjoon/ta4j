```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import static org.ta4j.core.TestUtils.assertNumEquals;

/**
 * Tests for {@link Position#getProfit(int, Num)}.
 *
 * The tests cover both closed and open positions (long and short) and verify that
 * the overload with {@code finalIndex} and {@code finalPrice} returns the same
 * result as the parameter‑less {@code getProfit()} when the position is already
 * closed, and that it correctly accounts for holding costs when the position is
 * still open.
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
    /*  Tests                                                               */
    /* --------------------------------------------------------------------- */

    @Test
    void whenClosedLongPosition_getProfitWithParamsEqualsParameterless() {
        // Closed long position (entry + exit)
        Position closedLong = new Position(longEntry, longExit, transactionModel, holdingModel);

        // Parameter‑less profit (used as reference)
        Num expected = closedLong.getProfit();

        // Profit using the overload with final index / price
        Num actual = closedLong.getProfit(10, DoubleNum.valueOf(2));

        assertNumEquals(expected, actual);
    }

    @Test
    void whenOpenLongPosition_getProfitWithParamsReflectsHoldingCost() {
        // Open long position – only entry is performed
        Position openLong = new Position(Trade.TradeType.BUY, transactionModel, holdingModel);
        openLong.operate(5, DoubleNum.valueOf(2), DoubleNum.valueOf(1)); // entry at price 2

        // Final price is higher (3) and we are at bar index 10
        Num finalPrice = DoubleNum.valueOf(3);
        int finalIndex = 10;

        // Expected gross profit = (finalPrice - entryPrice) * amount
        Num grossProfit = finalPrice.minus(DoubleNum.valueOf(2)).multipliedBy(DoubleNum.valueOf(1));

        // Transaction cost for the entry trade (1 % of price * amount)
        Num transactionCost = DoubleNum.valueOf(2).multipliedBy(DoubleNum.valueOf(0.01));

        // Holding cost = holdingModel * price * (finalIndex - entryIndex)
        // LinearBorrowingCostModel uses: cost = price * amount * rate * barsHeld
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
        // Closed short position (SELL → BUY)
        Position closedShort = new Position(shortEntry, shortExit, transactionModel, holdingModel);

        Num expected = closedShort.getProfit();
        Num actual = closedShort.getProfit(10, DoubleNum.valueOf(2));

        assertNumEquals(expected, actual);
    }

    @Test
    void whenOpenShortPosition_getProfitWithParamsReflectsHoldingCost() {
        // Open short position – only entry (SELL) is performed
        Position openShort = new Position(Trade.TradeType.SELL, transactionModel, holdingModel);
        openShort.operate(5, DoubleNum.valueOf(2), DoubleNum.valueOf(1)); // entry at price 2

        // Final price is lower (1.5) – profit for short = entryPrice - finalPrice
        Num finalPrice = DoubleNum.valueOf(1.5);
        int finalIndex = 12;

        // Expected gross profit = (entryPrice - finalPrice) * amount
        Num grossProfit = DoubleNum.valueOf(2).minus(finalPrice).multipliedBy(DoubleNum.valueOf(1));

        // Transaction cost for the entry trade (1 % of price * amount)
        Num transactionCost = DoubleNum.valueOf(2).multipliedBy(DoubleNum.valueOf(0.01));

        // Holding cost = price * amount * rate * barsHeld
        Num holdingCost = DoubleNum.valueOf(2)                     // price (entry price)
                .multipliedBy(DoubleNum.valueOf(1))                // amount
                .multipliedBy(DoubleNum.valueOf(0.001))            // rate
                .multipliedBy(DoubleNum.valueOf(finalIndex - 5)); // bars held

        Num expected = grossProfit.minus(transactionCost).minus(holdingCost);
        Num actual = openShort.getProfit(finalIndex, finalPrice);

        assertNumEquals(expected, actual);
    }
}
```