```java
package org.ta4j.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link Position#getPositionCost()}.
 *
 * <p>The method adds the transaction cost (provided by the {@link CostModel}
 * passed to the {@link Position} constructor) to the holding (borrowing)
 * cost (provided by the holding {@link CostModel}). By mocking both cost
 * models we can verify the exact value returned without depending on the
 * concrete implementations of the cost models.</p>
 */
@ExtendWith(MockitoExtension.class)
class Position_getPositionCost_2_Test {

    /** Mocked transaction‑cost model – used for the {@code calculate(Position)} call. */
    @Mock
    CostModel transactionCostModel;

    /** Mocked holding‑cost model – used for the {@code calculate(Position)} call inside {@code getHoldingCost()}. */
    @Mock
    CostModel holdingCostModel;

    /** A position that is already closed (has entry and exit trades). */
    Position closedPosition;

    /** A position that is still open (only entry trade). */
    Position openPosition;

    /** Fixed values returned by the mocks – easier to read in the assertions. */
    private static final Num TRANSACTION_COST = DoubleNum.valueOf(5.0);
    private static final Num HOLDING_COST = DoubleNum.valueOf(2.0);

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Stub the mocks to return deterministic numbers.
        Mockito.when(transactionCostModel.calculate(any(Position.class))).thenReturn(TRANSACTION_COST);
        Mockito.when(holdingCostModel.calculate(any(Position.class))).thenReturn(HOLDING_COST);

        /* -----------------------------------------------------------------
         * Closed position – entry + exit trades.
         * ----------------------------------------------------------------- */
        Trade entry = Trade.buyAt(
                0,                     // index
                DoubleNum.valueOf(10), // price per asset
                DoubleNum.ONE,        // amount
                transactionCostModel  // the same transaction‑cost model used by the Position
        );

        Trade exit = Trade.sellAt(
                5,
                DoubleNum.valueOf(12),
                DoubleNum.ONE,
                transactionCostModel
        );

        closedPosition = new Position(entry, exit, transactionCostModel, holdingCostModel);

        /* -----------------------------------------------------------------
         * Open position – only entry trade (no exit yet).
         * ----------------------------------------------------------------- */
        openPosition = new Position(TradeType.BUY, transactionCostModel, holdingCostModel);
        // Operate once to create the entry trade.
        openPosition.operate(0, DoubleNum.valueOf(10), DoubleNum.ONE);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mocks between tests to avoid interaction leakage.
        Mockito.reset(transactionCostModel, holdingCostModel);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    @Test
    void getPositionCost_closedPosition_returnsTransactionPlusHoldingCost() {
        Num cost = closedPosition.getPositionCost();

        // Expected = transaction cost (5) + holding cost (2) = 7
        Num expected = DoubleNum.valueOf(7.0);
        assertEquals(expected, cost, "Closed position cost should be transaction + holding cost");
    }

    @Test
    void getPositionCost_openPosition_returnsTransactionPlusHoldingCost() {
        Num cost = openPosition.getPositionCost();

        // Expected = transaction cost (5) + holding cost (2) = 7
        Num expected = DoubleNum.valueOf(7.0);
        assertEquals(expected, cost, "Open position cost should be transaction + holding cost");
    }

    @Test
    void getPositionCost_usesProvidedCostModelsExactlyOnceEach() {
        // Call the method – the mocks have been stubbed already.
        closedPosition.getPositionCost();

        // Verify that each model's calculate method was invoked exactly once for the position.
        Mockito.verify(transactionCostModel, Mockito.times(1)).calculate(closedPosition);
        Mockito.verify(holdingCostModel, Mockito.times(1)).calculate(closedPosition);
    }
}
```