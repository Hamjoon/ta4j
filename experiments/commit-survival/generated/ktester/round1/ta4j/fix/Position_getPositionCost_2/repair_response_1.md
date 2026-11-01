```java
package org.ta4j.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.*;

import java.util.stream.Stream;

/**
 * Tests for {@link Position#getPositionCost()}.
 */
@ExtendWith(MockitoExtension.class)
class Position_getPositionCost_2_Test {

    /**
     * Mocked transaction‑cost model – used for the {@code calculate(Position)} call.
     */
    @Mock
    CostModel transactionCostModel;

    /**
     * Mocked holding‑cost model – used for the {@code calculate(Position)} call inside {@code getHoldingCost()}.
     */
    @Mock
    CostModel holdingCostModel;

    /**
     * A position that is already closed (has entry and exit trades).
     */
    Position closedPosition;

    /**
     * A position that is still open (only entry trade).
     */
    Position openPosition;

    /**
     * Fixed values returned by the mocks – easier to read in the assertions.
     */
    private static final Num TRANSACTION_COST = DoubleNum.valueOf(5.0);
    private static final Num HOLDING_COST = DoubleNum.valueOf(2.0);

    /**
     * Default values – overridden in parameterised tests.
     */
    private static final Num DEFAULT_TRANSACTION_COST = DoubleNum.valueOf(5.0);
    private static final Num DEFAULT_HOLDING_COST = DoubleNum.valueOf(2.0);

    @BeforeEach
    void setupBeforeEach() {
        // Stub the mocks to return deterministic numbers.
        when(transactionCostModel.calculate(any(Position.class))).thenReturn(TRANSACTION_COST);
        when(holdingCostModel.calculate(any(Position.class))).thenReturn(HOLDING_COST);

        /* -----------------------------------------------------------------
         * Closed position – entry + exit trades.
         * ----------------------------------------------------------------- */
        Trade entry = Trade.buyAt(
                0,                                 // index
                DoubleNum.valueOf(10),             // amount
                DoubleNum.ONE,                     // price per asset (using ONE as a placeholder)
                transactionCostModel);
        Trade exit = Trade.sellAt(
                5,
                DoubleNum.valueOf(12),
                DoubleNum.ONE,
                transactionCostModel);
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

    private static Stream<Arguments> standardCalculationProvider() {
        return Stream.of(
                // Both positive
                Arguments.of(DoubleNum.valueOf(100.0), DoubleNum.valueOf(50.0), DoubleNum.valueOf(150.0)),
                // Both zero
                Arguments.of(DoubleNum.valueOf(0.0), DoubleNum.valueOf(0.0), DoubleNum.valueOf(0.0)),
                // Both negative
                Arguments.of(DoubleNum.valueOf(-10.0), DoubleNum.valueOf(-20.0), DoubleNum.valueOf(-30.0)),
                // Transaction NaN
                Arguments.of(DoubleNum.NaN, DoubleNum.valueOf(25.0), DoubleNum.NaN),
                // Holding NaN
                Arguments.of(DoubleNum.valueOf(40.0), DoubleNum.NaN, DoubleNum.NaN),
                // Both NaN
                Arguments.of(DoubleNum.NaN, DoubleNum.NaN, DoubleNum.NaN),
                // Mixed sign & zero combinations
                Arguments.of(DoubleNum.valueOf(100.0), DoubleNum.valueOf(0.0), DoubleNum.valueOf(100.0)),
                Arguments.of(DoubleNum.valueOf(0.0), DoubleNum.valueOf(75.0), DoubleNum.valueOf(75.0)),
                Arguments.of(DoubleNum.valueOf(-40.0), DoubleNum.valueOf(0.0), DoubleNum.valueOf(-40.0)),
                Arguments.of(DoubleNum.valueOf(0.0), DoubleNum.valueOf(-25.0), DoubleNum.valueOf(-25.0)),
                Arguments.of(DoubleNum.valueOf(80.0), DoubleNum.valueOf(-30.0), DoubleNum.valueOf(50.0)),
                Arguments.of(DoubleNum.valueOf(-55.0), DoubleNum.valueOf(20.0), DoubleNum.valueOf(-35.0)),
                // Large values
                Arguments.of(DoubleNum.valueOf(1.0e20), DoubleNum.valueOf(2.0e20), DoubleNum.valueOf(3.0e20)),
                Arguments.of(DoubleNum.valueOf(-1.0e30), DoubleNum.valueOf(-1.0e30), DoubleNum.valueOf(-2.0e30))
        );
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
        verify(transactionCostModel, times(1)).calculate(closedPosition);
        verify(holdingCostModel, times(1)).calculate(closedPosition);
    }

    @ParameterizedTest(name = "{index}: transaction={0}, holding={1} => expected={2}")
    @MethodSource("standardCalculationProvider")
    void getPositionCost_standardCalculations(Num transaction, Num holding, Num expected) {
        when(transactionCostModel.calculate(any(Position.class))).thenReturn(transaction);
        when(holdingCostModel.calculate(any(Position.class))).thenReturn(holding);
        Num result = closedPosition.getPositionCost();
        assertEquals(expected, result,
                () -> "Expected transaction (" + transaction + ") + holding (" + holding + ") = " + expected);
    }

    @Test
    void getPositionCost_transactionCostModelIsNull_throwsNullPointerException() {
        // Create a Position with a null transaction‑cost model.
        Position pos = new Position(
                Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.ONE, null),
                Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.ONE, null),
                null,
                holdingCostModel);
        // holdingCostModel is still mocked to return a valid value.
        when(holdingCostModel.calculate(any(Position.class))).thenReturn(DoubleNum.valueOf(2.0));
        assertThrows(NullPointerException.class, pos::getPositionCost,
                "When the transactionCostModel field is null a NPE should be thrown");
    }

    @Test
    void getPositionCost_holdingCostReturnsNull_throwsNullPointerException() {
        // Stub holding cost model to return null.
        when(holdingCostModel.calculate(any(Position.class))).thenReturn(null);
        when(transactionCostModel.calculate(any(Position.class))).thenReturn(DoubleNum.valueOf(5.0));
        assertThrows(NullPointerException.class, closedPosition::getPositionCost,
                "A null holding cost should cause a NPE when added to a valid transaction cost");
    }

    @Test
    void getPositionCost_transactionCostModelReturnsNull_throwsNullPointerException() {
        when(transactionCostModel.calculate(any(Position.class))).thenReturn(null);
        when(holdingCostModel.calculate(any(Position.class))).thenReturn(DoubleNum.valueOf(2.0));
        assertThrows(NullPointerException.class, closedPosition::getPositionCost,
                "A null transaction cost should cause a NPE when added to a valid holding cost");
    }

    @Test
    void getPositionCost_transactionCostModelThrowsIllegalStateException_propagates() {
        when(transactionCostModel.calculate(any(Position.class))).thenThrow(new IllegalStateException("boom"));
        assertThrows(IllegalStateException.class, closedPosition::getPositionCost,
                "Exception from transactionCostModel must propagate");
    }

    @Test
    void getPositionCost_holdingCostModelThrowsIllegalStateException_propagates() {
        when(holdingCostModel.calculate(any(Position.class))).thenThrow(new IllegalStateException("boom"));
        assertThrows(IllegalStateException.class, closedPosition::getPositionCost,
                "Exception from holdingCostModel must propagate");
    }

    @Test
    void getPositionCost_transactionCostModelThrowsNullPointerException_propagates() {
        when(transactionCostModel.calculate(any(Position.class))).thenThrow(new NullPointerException("boom"));
        assertThrows(NullPointerException.class, closedPosition::getPositionCost,
                "NullPointerException from transactionCostModel must propagate");
    }

    @Test
    void getPositionCost_holdingCostModelThrowsRuntimeException_propagates() {
        when(holdingCostModel.calculate(any(Position.class))).thenThrow(new IllegalArgumentException("boom"));
        assertThrows(IllegalArgumentException.class, closedPosition::getPositionCost,
                "RuntimeException from holdingCostModel must propagate");
    }

    @Test
    void getPositionCost_getHoldingCostThrowsIllegalStateException_propagates() {
        // Subclass Position to override getHoldingCost().
        Position buggy = new Position(
                Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.ONE, transactionCostModel),
                Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.ONE, transactionCostModel),
                transactionCostModel,
                holdingCostModel) {

            @Override
            public Num getHoldingCost() {
                throw new IllegalStateException("holding broken");
            }
        };
        when(transactionCostModel.calculate(any(Position.class))).thenReturn(DoubleNum.valueOf(5.0));
        assertThrows(IllegalStateException.class, buggy::getPositionCost,
                "Exception from overridden getHoldingCost should propagate");
    }

    @Test
    void getPositionCost_multipleCalls_returnSameResult() {
        when(transactionCostModel.calculate(any(Position.class))).thenReturn(DoubleNum.valueOf(70.0));
        when(holdingCostModel.calculate(any(Position.class))).thenReturn(DoubleNum.valueOf(50.0));
        Num first = closedPosition.getPositionCost();
        Num second = closedPosition.getPositionCost();
        assertEquals(first, second, "Repeated calls must return the same Num instance/value");
        assertEquals(DoubleNum.valueOf(120.0), first, "120 = 70 + 50");
    }

    @Test
    void getPositionCost_incompatibleNumImplementations_throwIllegalArgumentException() {
        // transaction cost = DoubleNum, holding cost = DecimalNum (different implementation)
        when(transactionCostModel.calculate(any(Position.class))).thenReturn(DoubleNum.valueOf(10.0));
        when(holdingCostModel.calculate(any(Position.class))).thenReturn(DecimalNum.valueOf(5.0));
        assertThrows(IllegalArgumentException.class, closedPosition::getPositionCost,
                "Mixing different Num implementations should raise IllegalArgumentException");
    }

    @Test
    void getPositionCost_customNumPlusThrowsArithmeticException_propagates() {
        // Mock a Num that throws when plus() is invoked.
        Num throwingNum = mock(Num.class);
        when(throwingNum.plus(any())).thenThrow(new ArithmeticException("overflow"));
        when(transactionCostModel.calculate(any(Position.class))).thenReturn(throwingNum);
        when(holdingCostModel.calculate(any(Position.class))).thenReturn(DoubleNum.valueOf(5.0));
        assertThrows(ArithmeticException.class, closedPosition::getPositionCost,
                "ArithmeticException from Num.plus must propagate");
    }

    @Test
    void getPositionCost_largePositiveAndNegativeValues_sumCorrectly() {
        when(transactionCostModel.calculate(any(Position.class))).thenReturn(DoubleNum.valueOf(1.0e25));
        when(holdingCostModel.calculate(any(Position.class))).thenReturn(DoubleNum.valueOf(-9.999999999999999e24));
        Num result = closedPosition.getPositionCost();
        // ≈ 1.0e22
        Num expected = DoubleNum.valueOf(1.0e25 - 9.999999999999999e24);
        assertEquals(expected, result, "Large mixed‑sign values must be summed accurately");
    }
}
```