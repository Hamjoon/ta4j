```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import static org.mockito.Mockito.*;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link Position#getHoldingCost(int)}.
 *
 * <p>The method simply delegates to the {@link CostModel} supplied at construction
 * time. These tests verify that delegation works correctly and that the default
 * {@link ZeroCostModel} returns a zero holding cost.</p>
 */
@ExtendWith(MockitoExtension.class)
class Position_getHoldingCost_2_Test {

    /** Position that uses a mocked {@link CostModel} for holding costs. */
    private Position positionWithMockModel;

    /** Mocked holding‑cost model. */
    @Mock
    private CostModel holdingCostModelMock;

    /** Position that uses the default {@link ZeroCostModel}. */
    private Position positionWithZeroModel;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // --------------------------------------------------------------------
        // Position that delegates to a mocked CostModel.
        // --------------------------------------------------------------------
        // The transaction‑cost model is irrelevant for the holding‑cost test,
        // therefore we use the simple ZeroCostModel.
        CostModel transactionModel = new ZeroCostModel();

        // Build a Position that uses the mocked holding‑cost model.
        positionWithMockModel = new Position(Trade.TradeType.BUY,
                                            transactionModel,
                                            holdingCostModelMock);

        // --------------------------------------------------------------------
        // Position that relies on the library’s default ZeroCostModel.
        // --------------------------------------------------------------------
        positionWithZeroModel = new Position(); // uses ZeroCostModel internally
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
    void whenHoldingCostModelIsMocked_thenResultIsReturnedFromModel() {
        // GIVEN
        int finalIndex = 7;
        Num expected = DoubleNum.valueOf(3.1415);
        when(holdingCostModelMock.calculate(positionWithMockModel, finalIndex))
                .thenReturn(expected);

        // WHEN
        Num actual = positionWithMockModel.getHoldingCost(finalIndex);

        // THEN
        assertEquals(expected, actual,
                "Position#getHoldingCost should return the value supplied by the holding‑cost model");
        verify(holdingCostModelMock, times(1))
                .calculate(positionWithMockModel, finalIndex);
    }

    @Test
    void whenUsingDefaultZeroCostModel_thenHoldingCostIsZero() {
        // GIVEN
        int finalIndex = 12;
        Num expectedZero = DoubleNum.valueOf(0);

        // WHEN
        Num actual = positionWithZeroModel.getHoldingCost(finalIndex);

        // THEN
        assertEquals(expectedZero, actual,
                "Default ZeroCostModel must produce a holding cost of zero");
    }
}
```