```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Position#getPositionCost(int)}.
 *
 * The method adds the transaction cost (provided by the {@link CostModel}
 * supplied to the {@link Position} constructor) and the holding cost
 * (provided by the second {@link CostModel} argument of the constructor).
 *
 * By mocking both cost models we can control the values returned by the
 * {@code calculate(...)} method and therefore assert that the sum returned
 * by {@code getPositionCost} is correct.
 */
@ExtendWith(MockitoExtension.class)
class Position_getPositionCost_Test {

    /** Mock that will act as the transaction‑cost model. */
    @Mock
    private CostModel transactionCostModel;

    /** Mock that will act as the holding‑cost model. */
    @Mock
    private CostModel holdingCostModel;

    /** Position instance under test. */
    private Position position;

    /** Index that will be passed to {@code getPositionCost}. */
    private static final int FINAL_INDEX = 10;

    /** Values returned by the mocked models. */
    private static final Num TRANSACTION_COST = DoubleNum.valueOf(5.25);
    private static final Num HOLDING_COST = DoubleNum.valueOf(2.75);

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for this test class.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Stub the mocked CostModel instances.
        when(transactionCostModel.calculate(any(Position.class), eq(FINAL_INDEX)))
                .thenReturn(TRANSACTION_COST);
        when(holdingCostModel.calculate(any(Position.class), eq(FINAL_INDEX)))
                .thenReturn(HOLDING_COST);

        // Create a Position that uses the mocked cost models.
        // Starting type can be either BUY or SELL – it does not matter for the
        // cost calculation itself.
        position = new Position(Trade.TradeType.BUY, transactionCostModel, holdingCostModel);
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
    void getPositionCost_returnsSumOfTransactionAndHoldingCosts() {
        // When
        Num result = position.getPositionCost(FINAL_INDEX);

        // Then
        Num expected = TRANSACTION_COST.plus(HOLDING_COST);
        assertEquals(expected, result,
                "Position.getPositionCost should return transactionCost + holdingCost");
    }

    @Test
    void getPositionCost_callsBothCostModelsExactlyOnce() {
        // When
        position.getPositionCost(FINAL_INDEX);

        // Then – verify interaction with the mocks
        verify(transactionCostModel, times(1))
                .calculate(eq(position), eq(FINAL_INDEX));
        verify(holdingCostModel, times(1))
                .calculate(eq(position), eq(FINAL_INDEX));
        verifyNoMoreInteractions(transactionCostModel, holdingCostModel);
    }
}
```