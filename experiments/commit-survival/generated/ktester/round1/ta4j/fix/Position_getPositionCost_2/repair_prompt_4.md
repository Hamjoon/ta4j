
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
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

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.*;

import java.util.stream.Stream;
import org.ta4j.core.Trade.TradeType;
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.Test;
import org.ta4j.core.num.NaN;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.Arguments;
import static org.ta4j.core.num.NaN.NaN;
import org.junit.jupiter.api.*;

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

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/Position_getPositionCost_2_Test.java:21: error: ';' expected
import org.junit.jupiter.params.provider.MethodSource
                                                     ^
1 error

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.Position': '\n * A {@code Position} is a pair of two {@link Trade trades}.\n *\n * <p>\n * The exit trade has the complement type of the entry trade, i.e.:\n * <ul>\n * <li>entry == BUY --> exit == SELL\n * <li>entry == SELL --> exit == BUY\n * </ul>\n ', 'api document of method getPositionCost()': '\n     * Calculates the total cost of the closed position.\n     *\n     * @return the cost of the position\n     ', 'return type': 'org.ta4j.core.num.Num', 'dependent classes': 'class `org.ta4j.core.Position`:\n\tdependent fields:\n\t\torg.ta4j.core.analysis.cost.CostModel org.ta4j.core.Position.transactionCostModel;\n\trelated functions:\n\t\tmethod `public Num getPositionCost(int)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getPositionCost()`, api document: \n     * Calculates the total cost of the position.\n     *\n     * @param finalIndex the index of the final bar to be considered (if position is\n     *                   open)\n     * @return the cost of the position\n     ;\n\t\tmethod `public Num getHoldingCost()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getPositionCost()`, api document: \n     * Calculates the holding cost of the closed position.\n     *\n     * @return the cost of the position\n     ;\nclass `org.ta4j.core.analysis.CashFlow`:\n\trelated functions:\n\t\tmethod ` static Num addCost(Num, Num, boolean)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getPositionCost()`, api document: \n     * Adjusts (intermediate) price to incorporate trading costs.\n     *\n     * @param rawPrice    the gross asset price\n     * @param holdingCost share of the holding cost per period\n     * @param isLongTrade true, if the entry trade type is BUY\n     ;\nclass `org.ta4j.core.indicators.KSTIndicator`:\n\trelated functions:\n\t\tmethod `protected Num calculate(int)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getPositionCost()`;\nclass `org.ta4j.core.analysis.cost.LinearTransactionCostModel`:\n\trelated functions:\n\t\tmethod `public Num calculate(Position)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getPositionCost()`;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


