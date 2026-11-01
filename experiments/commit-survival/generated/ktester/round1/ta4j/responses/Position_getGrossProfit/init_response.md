```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Position#getGrossProfit()}.
 *
 * <p>The method has two distinct behaviours:
 * <ul>
 *   <li>If the position is still opened (no exit trade), it must return zero.</li>
 *   <li>If the position is closed, it must delegate to {@link Position#getGrossProfit(Num)}
 *       using the exit trade price.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class Position_getGrossProfit_Test {

    private Position openedPosition;
    private Position closedPosition;
    private Trade entryBuy;
    private Trade exitSell;
    private Trade entrySell;
    private Trade exitBuy;
    private CostModel transactionModel;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests
    }

    @BeforeEach
    void setupBeforeEach() {
        // Simple transaction cost model – costs are irrelevant for gross profit
        transactionModel = new ZeroCostModel();

        // ----- Opened (still active) position -----
        // Default constructor creates a BUY‑starting position.
        openedPosition = new Position();               // BUY start
        // Simulate entry trade (price is not required for gross profit when open)
        openedPosition.operate(0);                     // creates entry trade

        // ----- Closed position (BUY → SELL) -----
        entryBuy = Trade.buyAt(0,
                DoubleNum.valueOf(10),                 // entry price
                DoubleNum.valueOf(1),                  // amount
                transactionModel);
        exitSell = Trade.sellAt(5,
                DoubleNum.valueOf(12),                 // exit price
                DoubleNum.valueOf(1),
                transactionModel);
        closedPosition = new Position(entryBuy, exitSell,
                transactionModel, new ZeroCostModel());

        // ----- Closed short position (SELL → BUY) -----
        entrySell = Trade.sellAt(0,
                DoubleNum.valueOf(15),
                DoubleNum.valueOf(1),
                transactionModel);
        exitBuy = Trade.buyAt(4,
                DoubleNum.valueOf(13),
                DoubleNum.valueOf(1),
                transactionModel);
        // We reuse the same variable name for clarity in the short‑position test
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up per test
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to release
    }

    @Test
    void whenPositionIsOpened_getGrossProfitReturnsZero() {
        // The position created in setUp is still opened (no exit trade)
        assertTrue(openedPosition.isOpened(), "Position should be opened");

        Num grossProfit = openedPosition.getGrossProfit();

        // Expected zero – the library uses a Num representing 0
        Num expectedZero = DoubleNum.valueOf(0);
        assertEquals(expectedZero, grossProfit,
                "Gross profit of an opened position must be zero");
    }

    @Test
    void whenPositionIsClosed_getGrossProfitDelegatesToOverload() {
        // Verify that the closed BUY → SELL position returns the correct gross profit
        assertTrue(closedPosition.isClosed(), "Position should be closed");

        Num grossProfitViaMethod = closedPosition.getGrossProfit();
        Num grossProfitViaOverload = closedPosition.getGrossProfit(exitSell.getPricePerAsset());

        assertEquals(grossProfitViaOverload, grossProfitViaMethod,
                "getGrossProfit() must delegate to the overload using exit price");

        // Manual expected value: exit price – entry price = 12 – 10 = 2
        Num expected = DoubleNum.valueOf(2);
        assertEquals(expected, grossProfitViaMethod,
                "Gross profit for BUY→SELL should equal exit‑price minus entry‑price");
    }

    @Test
    void whenShortPositionIsClosed_getGrossProfitIsEntryMinusExit() {
        // Build a short (SELL → BUY) closed position
        Position shortPosition = new Position(entrySell, exitBuy,
                transactionModel, new ZeroCostModel());

        assertTrue(shortPosition.isClosed(), "Short position should be closed");

        Num grossProfit = shortPosition.getGrossProfit();

        // Expected: entry price – exit price = 15 – 13 = 2
        Num expected = DoubleNum.valueOf(2);
        assertEquals(expected, grossProfit,
                "Gross profit for SELL→BUY should equal entry‑price minus exit‑price");
    }
}
```