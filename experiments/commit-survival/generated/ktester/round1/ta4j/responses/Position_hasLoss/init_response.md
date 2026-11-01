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

/**
 * Unit tests for {@link Position#hasLoss()}.
 *
 * <p>The tests cover three scenarios:
 *
 * <ul>
 *   <li>a newly created (empty) position – profit is zero, therefore {@code hasLoss()}
 *       must return {@code false};
 *   <li>a closed position with a positive profit – {@code hasLoss()} must return {@code false};
 *   <li>a closed position with a negative profit – {@code hasLoss()} must return {@code true}.
 * </ul>
 *
 * The tests use real {@link Trade} objects (via the static factory methods
 * {@code Trade.buyAt(...)} and {@code Trade.sellAt(...)}). No private members are
 * accessed directly.
 */
@ExtendWith(MockitoExtension.class)
class Position_hasLoss_Test {

    private Position emptyPosition;
    private Position profitPositivePosition;
    private Position profitNegativePosition;

    private CostModel transactionModel;
    private CostModel holdingModel;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Cost models used by the trades/positions
        transactionModel = new LinearTransactionCostModel(0.01);
        holdingModel = new LinearBorrowingCostModel(0.001);

        // -----------------------------------------------------------------
        // 1. Empty position – created with the default constructor (BUY start)
        // -----------------------------------------------------------------
        emptyPosition = new Position(); // no operate() called, so no entry/exit

        // -----------------------------------------------------------------
        // 2. Positive‑profit closed position (BUY @2, SELL @5)
        // -----------------------------------------------------------------
        Trade entryBuy = Trade.buyAt(0,
                DoubleNum.valueOf(2),   // price
                DoubleNum.valueOf(1),   // amount
                transactionModel);
        Trade exitSell = Trade.sellAt(1,
                DoubleNum.valueOf(5),
                DoubleNum.valueOf(1),
                transactionModel);
        profitPositivePosition = new Position(entryBuy, exitSell,
                transactionModel, holdingModel);

        // -----------------------------------------------------------------
        // 3. Negative‑profit closed position (BUY @5, SELL @2)
        // -----------------------------------------------------------------
        Trade entryBuyLoss = Trade.buyAt(0,
                DoubleNum.valueOf(5),
                DoubleNum.valueOf(1),
                transactionModel);
        Trade exitSellLoss = Trade.sellAt(1,
                DoubleNum.valueOf(2),
                DoubleNum.valueOf(1),
                transactionModel);
        profitNegativePosition = new Position(entryBuyLoss, exitSellLoss,
                transactionModel, holdingModel);
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
    void whenPositionIsEmpty_thenHasLossIsFalse() {
        // An empty position has a profit of zero → not negative.
        assertFalse(emptyPosition.hasLoss(),
                "A newly created position should not be considered as having a loss");
    }

    @Test
    void whenPositionHasPositiveProfit_thenHasLossIsFalse() {
        // The profit is (5‑2) minus transaction costs → positive.
        assertTrue(profitPositivePosition.getProfit().isPositive(),
                "Setup error: expected a positive profit for the position");
        assertFalse(profitPositivePosition.hasLoss(),
                "A position with positive profit must report hasLoss() == false");
    }

    @Test
    void whenPositionHasNegativeProfit_thenHasLossIsTrue() {
        // The profit is (2‑5) minus transaction costs → negative.
        assertTrue(profitNegativePosition.getProfit().isNegative(),
                "Setup error: expected a negative profit for the position");
        assertTrue(profitNegativePosition.hasLoss(),
                "A position with negative profit must report hasLoss() == true");
    }

    @Test
    void whenShortPositionEndsWithLoss_thenHasLossIsTrue() {
        // Short (SELL @5, BUY @2) → loss.
        Trade entrySell = Trade.sellAt(0,
                DoubleNum.valueOf(5),
                DoubleNum.valueOf(1),
                transactionModel);
        Trade exitBuy = Trade.buyAt(1,
                DoubleNum.valueOf(2),
                DoubleNum.valueOf(1),
                transactionModel);
        Position shortLoss = new Position(entrySell, exitBuy,
                transactionModel, holdingModel);

        assertTrue(shortLoss.getProfit().isNegative(),
                "Setup error: expected a negative profit for the short position");
        assertTrue(shortLoss.hasLoss(),
                "Short position with loss must report hasLoss() == true");
    }

    @Test
    void whenShortPositionEndsWithProfit_thenHasLossIsFalse() {
        // Short (SELL @2, BUY @5) → profit.
        Trade entrySell = Trade.sellAt(0,
                DoubleNum.valueOf(2),
                DoubleNum.valueOf(1),
                transactionModel);
        Trade exitBuy = Trade.buyAt(1,
                DoubleNum.valueOf(5),
                DoubleNum.valueOf(1),
                transactionModel);
        Position shortProfit = new Position(entrySell, exitBuy,
                transactionModel, holdingModel);

        assertTrue(shortProfit.getProfit().isPositive(),
                "Setup error: expected a positive profit for the short position");
        assertFalse(shortProfit.hasLoss(),
                "Short position with profit must report hasLoss() == false");
    }
}
```