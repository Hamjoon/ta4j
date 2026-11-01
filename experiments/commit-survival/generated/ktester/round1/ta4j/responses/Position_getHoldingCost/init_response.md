```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Position_getHoldingCost_Test {

    // Mocks
    @Mock
    private CostModel mockHoldingCostModel;

    // System under test
    private Position position;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests
    }

    @BeforeEach
    void setupBeforeEach() {
        // Use a ZeroCostModel for transaction costs (not relevant for holding cost)
        CostModel transactionCostModel = new ZeroCostModel();

        // Build a Position that uses the mocked holding cost model
        position = new Position(Trade.TradeType.BUY, transactionCostModel, mockHoldingCostModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mock interactions to avoid cross‑test interference
        reset(mockHoldingCostModel);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required
    }

    @Test
    void getHoldingCost_delegatesToHoldingCostModel_andReturnsCalculatedValue() {
        // GIVEN – the holding cost model returns a deterministic value
        Num expectedHoldingCost = DoubleNum.valueOf(12.34);
        when(mockHoldingCostModel.calculate(position)).thenReturn(expectedHoldingCost);

        // WHEN – the method under test is invoked
        Num actualHoldingCost = position.getHoldingCost();

        // THEN – the result matches the mocked value and the model was called exactly once
        assertEquals(expectedHoldingCost, actualHoldingCost,
                "Position#getHoldingCost should return the value provided by the holding cost model");
        verify(mockHoldingCostModel, times(1)).calculate(position);
    }

    @Test
    void getHoldingCost_returnsNaN_whenHoldingCostModelReturnsNaN() {
        // GIVEN – the holding cost model returns NaN
        Num nan = DoubleNum.NaN;
        when(mockHoldingCostModel.calculate(position)).thenReturn(nan);

        // WHEN
        Num result = position.getHoldingCost();

        // THEN
        assertTrue(result.isNaN(), "When the holding cost model returns NaN, Position#getHoldingCost should also return NaN");
        verify(mockHoldingCostModel, times(1)).calculate(position);
    }

    @Test
    void getHoldingCost_throwsWhenHoldingCostModelIsNull() {
        // GIVEN – a Position constructed with a null holding cost model
        CostModel transactionCostModel = new ZeroCostModel();
        Position posWithNullHolding = assertThrows(IllegalArgumentException.class,
                () -> new Position(Trade.TradeType.BUY, transactionCostModel, null));

        // THEN – the constructor should reject a null holding cost model
        assertEquals("Starting type must not be null", posWithNullHolding.getMessage(),
                "Constructor should validate non‑null holding cost model (via starting type check)");
    }
}
```