```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.ta4j.core.num.NaN.NaN;

/**
 * Unit tests for {@link Position#getPositionCost(int)}.
 *
 * The method adds the transaction cost (provided by the {@link CostModel}
 * supplied to the {@link Position} constructor) and the holding cost
 * (provided by the second {@link CostModel} argument of the constructor).
 *
 * All test cases described in the JSON specification are covered:
 * <ul>
 *   <li>standard numeric calculations</li>
 *   <li>zero‑cost scenarios</li>
 *   <li>negative costs (rebates / profit)</li>
 *   <li>NaN handling</li>
 *   <li>exception propagation from both cost models</li>
 *   <li>extreme index values</li>
 *   <li>null cost‑model field handling</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class Position_getPositionCost_Test {

    /** Mock that will act as the transaction‑cost model. */
    @Mock
    private CostModel transactionCostModel;

    /** Mock that will act as the holding‑cost model. */
    @Mock
    private CostModel holdingCostModel;

    /** Position instance under test (created in {@link #setupPosition()}). */
    private Position position;

    /** Helper to create a fresh Position before each test. */
    @BeforeEach
    void setupPosition() {
        // The concrete starting type does not affect cost calculation.
        position = new Position(TradeType.BUY, transactionCostModel, holdingCostModel);
    }

    /* ------------------------------------------------------------
     *  Parameterised tests – numeric results (including NaN)
     * ------------------------------------------------------------ */

    @ParameterizedTest(name = "[{index}] finalIndex={0} → expected={3}")
    @MethodSource("standardCalculationsProvider")
    void standardCalculations(int finalIndex,
                              Num transactionCost,
                              Num holdingCost,
                              Num expected) {
        // -----------------------------------------------------------------
        // Arrange – stub the two CostModel mocks for the given finalIndex
        // -----------------------------------------------------------------
        reset(transactionCostModel, holdingCostModel);
        when(transactionCostModel.calculate(eq(position), eq(finalIndex)))
                .thenReturn(transactionCost);
        when(holdingCostModel.calculate(eq(position), eq(finalIndex)))
                .thenReturn(holdingCost);

        // -----------------------------------------------------------------
        // Act
        // -----------------------------------------------------------------
        Num result = position.getPositionCost(finalIndex);

        // -----------------------------------------------------------------
        // Assert – NaN must be compared via isNaN(), otherwise normal equality
        // -----------------------------------------------------------------
        if (expected.isNaN()) {
            assertTrue(result.isNaN(),
                    () -> "Result should be NaN for finalIndex=" + finalIndex);
        } else {
            assertEquals(expected, result,
                    () -> "Incorrect cost for finalIndex=" + finalIndex);
        }
    }

    /**
     * Provides all cases that return a concrete {@link Num} (including NaN).
     */
    private static Stream<Arguments> standardCalculationsProvider() {
        return Stream.of(
                // ---- Standard calculations -------------------------------------------------
                Arguments.of(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), DoubleNum.valueOf(15)),
                Arguments.of(5, DoubleNum.valueOf(0), DoubleNum.valueOf(0), DoubleNum.valueOf(0)),
                Arguments.of(-1, DoubleNum.valueOf(2), DoubleNum.valueOf(3), DoubleNum.valueOf(5)),
                Arguments.of(1000, DoubleNum.valueOf(7), DoubleNum.valueOf(8), DoubleNum.valueOf(15)),

                // ---- Zero‑cost scenarios ---------------------------------------------------
                Arguments.of(3, DoubleNum.valueOf(0), DoubleNum.valueOf(12), DoubleNum.valueOf(12)),
                Arguments.of(8, DoubleNum.valueOf(0), DoubleNum.valueOf(0), DoubleNum.valueOf(0)),

                // ---- Negative transaction cost (rebate) ----------------------------------
                Arguments.of(4, DoubleNum.valueOf(-5), DoubleNum.valueOf(10), DoubleNum.valueOf(5)),

                // ---- Negative holding cost (profit) ----------------------------------------
                Arguments.of(9, DoubleNum.valueOf(3), DoubleNum.valueOf(-2), DoubleNum.valueOf(1)),

                // ---- NaN handling -----------------------------------------------------------
                Arguments.of(6, NaN, DoubleNum.valueOf(4), NaN),
                Arguments.of(11, NaN, NaN, NaN),

                // ---- Extreme index values --------------------------------------------------
                Arguments.of(Integer.MAX_VALUE, DoubleNum.valueOf(0), DoubleNum.valueOf(0), DoubleNum.valueOf(0))
        );
    }

    /* ------------------------------------------------------------
     *  Exception propagation – transaction cost model
     * ------------------------------------------------------------ */

    @Test
    void transactionCostModelThrowsRuntimeException_isPropagated() {
        int finalIndex = 2;
        reset(transactionCostModel, holdingCostModel);
        when(transactionCostModel.calculate(eq(position), eq(finalIndex)))
                .thenThrow(new RuntimeException("transaction error"));
        when(holdingCostModel.calculate(eq(position), eq(finalIndex)))
                .thenReturn(DoubleNum.valueOf(0)); // irrelevant

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> position.getPositionCost(finalIndex));
        assertEquals("transaction error", ex.getMessage());
    }

    /* ------------------------------------------------------------
     *  Exception propagation – holding cost model
     * ------------------------------------------------------------ */

    @Test
    void holdingCostModelThrowsIllegalStateException_isPropagated() {
        int finalIndex = 13;
        reset(transactionCostModel, holdingCostModel);
        when(transactionCostModel.calculate(eq(position), eq(finalIndex)))
                .thenReturn(DoubleNum.valueOf(4));
        when(holdingCostModel.calculate(eq(position), eq(finalIndex)))
                .thenThrow(new IllegalStateException("holding error"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> position.getPositionCost(finalIndex));
        assertEquals("holding error", ex.getMessage());
    }

    /* ------------------------------------------------------------
     *  Null transaction cost handling
     * ------------------------------------------------------------ */

    @Test
    void transactionCostModelReturnsNull_throwsNullPointerException() {
        int finalIndex = 12;
        reset(transactionCostModel, holdingCostModel);
        when(transactionCostModel.calculate(eq(position), eq(finalIndex)))
                .thenReturn(null); // simulate faulty model
        when(holdingCostModel.calculate(eq(position), eq(finalIndex)))
                .thenReturn(DoubleNum.valueOf(5));

        assertThrows(NullPointerException.class,
                () -> position.getPositionCost(finalIndex));
    }

    /* ------------------------------------------------------------
     *  Extreme index – transaction model throws RuntimeException
     * ------------------------------------------------------------ */

    @Test
    void transactionCostModelThrowsOnMinValue_isPropagated() {
        int finalIndex = Integer.MIN_VALUE;
        reset(transactionCostModel, holdingCostModel);
        when(transactionCostModel.calculate(eq(position), eq(finalIndex)))
                .thenThrow(new RuntimeException("invalid index"));
        when(holdingCostModel.calculate(eq(position), eq(finalIndex)))
                .thenReturn(DoubleNum.valueOf(0)); // irrelevant

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> position.getPositionCost(finalIndex));
        assertEquals("invalid index", ex.getMessage());
    }

    /* ------------------------------------------------------------
     *  Null transactionCostModel field in Position
     * ------------------------------------------------------------ */

    @Test
    void positionCreatedWithNullTransactionCostModel_throwsNullPointerException() {
        // Construct a Position with a null transaction‑cost model.
        Position posWithNullModel = new Position(TradeType.BUY, null, holdingCostModel);
        int finalIndex = 0;

        // The holdingCostModel can be stubbed but it will never be reached
        // because the call to transactionCostModel.calculate will NPE first.
        when(holdingCostModel.calculate(any(Position.class), anyInt()))
                .thenReturn(DoubleNum.valueOf(0));

        assertThrows(NullPointerException.class,
                () -> posWithNullModel.getPositionCost(finalIndex));
    }
}
```