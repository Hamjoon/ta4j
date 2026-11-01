```java
package org.ta4j.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;

import org.junit.jupiter.api.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link Position#getProfit()} (the no‑argument version).
 *
 * The tests cover three main scenarios:
 *
 * <ul>
 *   <li>an opened (still active) position – profit must be zero</li>
 *   <li>a closed long position – profit equals gross profit minus transaction costs</li>
 *   <li>a closed short position – profit equals gross profit minus transaction costs
 *       and holding costs</li>
 *   <li>an opened short position – profit must be zero</li>
 * </ul>
 *
 * The test class uses JUnit 5 and only public APIs of {@link Position},
 * {@link Trade} and the cost models. No private members are accessed.
 */
@ExtendWith(MockitoExtension.class)
class Position_getProfit_Test {

    /** Cost model applied to every trade (1 % of the trade value). */
    private CostModel transactionModel;
    /** Holding‑cost model (0.1 % per bar). */
    private CostModel holdingModel;

    /** A BUY trade used as entry in long‑position tests. */
    private Trade longEntry;
    /** A SELL trade used as exit in long‑position tests. */
    private Trade longExit;

    /** A SELL trade used as entry in short‑position tests. */
    private Trade shortEntry;
    /** A BUY trade used as exit in short‑position tests. */
    private Trade shortExit;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Initialise the cost models used by all tests.
        transactionModel = new LinearTransactionCostModel(0.01); // 1 %
        holdingModel = new LinearBorrowingCostModel(0.001);      // 0.1 %

        // Long (BUY → SELL) trades – same price so gross profit = 0.
        longEntry = Trade.buyAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        longExit  = Trade.sellAt(2, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);

        // Short (SELL → BUY) trades – same price so gross profit = 0.
        shortEntry = Trade.sellAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        shortExit  = Trade.buyAt(2, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    @Test
    void getProfit_whenPositionOpened_returnsZero() {
        // A brand‑new position (default BUY) that has only been entered.
        Position opened = new Position();          // starts with BUY
        opened.operate(0);                          // entry only, no exit yet

        Num profit = opened.getProfit();            // should be zero for an opened position
        assertNumEquals(DoubleNum.valueOf(0), profit);
    }

    @Test
    void getProfit_whenClosedLongPosition_returnsGrossProfitMinusTransactionCosts() {
        // Long position: BUY then SELL at the same price.
        Position closedLong = new Position(longEntry, longExit, transactionModel, holdingModel);

        Num profit = closedLong.getProfit();        // gross profit = 0, transaction cost = 0.04
        // Expected profit = 0 – (2 * 0.01 * 1) * 2 = -0.04
        assertNumEquals(DoubleNum.valueOf(-0.04), profit);
    }

    @Test
    void getProfit_whenClosedShortPosition_returnsGrossProfitMinusCostsAndHoldingCosts() {
        // Short position: SELL then BUY at the same price, held for 9 bars (indices 1 → 10).
        Position closedShort = new Position(shortEntry, shortExit, transactionModel, holdingModel);

        Num profit = closedShort.getProfit();      // gross profit = 0, transaction cost = 0.04,
                                                   // holding cost = 2 * 9 * 0.001 = 0.018
        Num expectedHoldingCost = DoubleNum.valueOf(2.0 * 9.0 * 0.001); // 0.018
        Num expectedProfit = DoubleNum.valueOf(-0.04).minus(expectedHoldingCost); // -0.058

        assertNumEquals(expectedProfit, profit);
    }

    @Test
    void getProfit_whenOpenShortPosition_returnsZero() {
        // Open a short position (starting type SELL) without an exit.
        Position openedShort = new Position(TradeType.SELL, transactionModel, holdingModel);
        openedShort.operate(0);                     // entry only (SELL)

        Num profit = openedShort.getProfit();       // should be zero because the position is still open
        assertNumEquals(DoubleNum.valueOf(0), profit);
    }
}
```