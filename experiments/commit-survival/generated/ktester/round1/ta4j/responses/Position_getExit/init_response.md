```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Position#getExit()}.
 *
 * <p>These tests cover the following scenarios:
 *
 * <ul>
 *   <li>Newly created {@code Position} has no exit trade.</li>
 *   <li>After the entry trade is performed the exit is still {@code null}.</li>
 *   <li>After the exit trade is performed {@code getExit()} returns the correct {@link Trade}.</li>
 *   <li>When a {@code Position} is created with explicit entry/exit trades, {@code getExit()} returns the supplied exit trade.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class Position_getExit_Test {

    /* --------------------------------------------------------------------- */
    /*  Shared fixtures (created once for the whole test class)            */
    /* --------------------------------------------------------------------- */
    private static CostModel transactionModel;
    private static CostModel holdingModel;

    @BeforeAll
    static void setupBeforeAll() {
        // Use a non‑zero transaction model to make the created trades realistic.
        transactionModel = new LinearTransactionCostModel(0.01);
        // Holding costs are not relevant for getExit() but are required by some constructors.
        holdingModel = new ZeroCostModel();
    }

    /* --------------------------------------------------------------------- */
    /*  Per‑test fixtures                                                    */
    /* --------------------------------------------------------------------- */
    private Position emptyPosition;          // created with default constructor
    private Position entryOnlyPosition;      // entry performed, exit not yet
    private Position fullPosition;           // entry + exit performed
    private Position customPosition;         // constructed with explicit trades

    private Trade entryTrade;                 // BUY trade
    private Trade exitTrade;                  // SELL trade (complementary to entry)

    @BeforeEach
    void setupBeforeEach() {
        // 1. fresh Position objects
        emptyPosition = new Position();                     // default BUY start
        entryOnlyPosition = new Position();                // will receive entry only
        fullPosition = new Position();                     // will receive entry + exit
        // 2. create deterministic trades
        entryTrade = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        exitTrade = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        // 3. Position built from explicit trades (constructor validates cost model equality)
        customPosition = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // No external resources to release; method kept for symmetry.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to clean.
    }

    /* --------------------------------------------------------------------- */
    /*  Tests                                                                */
    /* --------------------------------------------------------------------- */

    @Test
    void getExit_ShouldReturnNull_ForNewPosition() {
        assertNull(emptyPosition.getExit(),
                "A freshly created Position must not have an exit trade.");
    }

    @Test
    void getExit_ShouldReturnNull_AfterOnlyEntry() {
        // Simulate entry only (the default starting type is BUY)
        entryOnlyPosition.operate(0); // creates a BUY entry at index 0
        assertNull(entryOnlyPosition.getExit(),
                "Position should still have no exit after only the entry trade.");
    }

    @Test
    void getExit_ShouldReturnExitTrade_AfterEntryAndExit() {
        // Perform entry
        fullPosition.operate(0); // entry BUY at index 0 (price NaN)
        // Perform exit
        fullPosition.operate(1); // exit SELL at index 1 (price NaN)

        Trade expectedExit = Trade.sellAt(1, Num.NaN, Num.NaN);
        assertEquals(expectedExit, fullPosition.getExit(),
                "After a second operate call the Position must contain the correct exit trade.");
    }

    @Test
    void getExit_ShouldReturnProvidedExit_WhenPositionCreatedWithExplicitTrades() {
        // The customPosition was constructed with entryTrade and exitTrade.
        assertEquals(exitTrade, customPosition.getExit(),
                "When a Position is built with explicit entry/exit trades, getExit() must return the supplied exit trade.");
    }

    @Test
    void getExit_ShouldBeConsistent_WithOperateUsingCustomPriceAndAmount() {
        // Create a Position that will be operated with explicit price/amount.
        Position p = new Position(TradeType.BUY, transactionModel, holdingModel);
        // Entry at index 2, price 20, amount 2
        p.operate(2, DoubleNum.valueOf(20), DoubleNum.valueOf(2));
        // Exit at index 3, price 22, amount 2
        p.operate(3, DoubleNum.valueOf(22), DoubleNum.valueOf(2));

        Trade expectedExit = Trade.sellAt(3, DoubleNum.valueOf(22), DoubleNum.valueOf(2));
        assertEquals(expectedExit, p.getExit(),
                "Exit trade created via operate(index, price, amount) must be returned by getExit().");
    }
}
```