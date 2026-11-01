```java
package org.ta4j.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Comprehensive unit tests for {@link Position#hasLoss()}.
 *
 * <p>The test suite covers:
 *
 * <ul>
 *   <li>standard profit scenarios (negative, zero, positive);
 *   <li>special numeric values (NaN, ±Infinity);
 *   <li>behaviour when {@code getProfit()} returns {@code null};
 *   <li>exception propagation from {@code Num.isNegative()} and {@code getProfit()};
 *   <li>short‑position cases (already present in the original template);
 *   <li>edge case where a {@code Num} representing zero reports {@code isNegative()}
 *       as {@code true}.
 * </ul>
 *
 * <p>All tests use real {@link Trade} objects except where a {@link Num} mock is
 * required to simulate exceptional behaviour. No private members are accessed
 * directly; reflection is only used when the focal method itself were private,
 * which is not the case here.
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
        // No global resources needed.
    }

    @BeforeEach
    void setupBeforeEach() {
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
                DoubleNum.valueOf(2),
                DoubleNum.valueOf(1),
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

    // -----------------------------------------------------------------
    // Existing basic scenarios (kept unchanged)
    // -----------------------------------------------------------------
    @Test
    void whenPositionIsEmpty_thenHasLossIsFalse() {
        assertFalse(emptyPosition.hasLoss(),
                "A newly created position should not be considered as having a loss");
    }

    @Test
    void whenPositionHasPositiveProfit_thenHasLossIsFalse() {
        assertTrue(profitPositivePosition.getProfit().isPositive(),
                "Setup error: expected a positive profit for the position");
        assertFalse(profitPositivePosition.hasLoss(),
                "A position with positive profit must report hasLoss() == false");
    }

    @Test
    void whenPositionHasNegativeProfit_thenHasLossIsTrue() {
        assertTrue(profitNegativePosition.getProfit().isNegative(),
                "Setup error: expected a negative profit for the position");
        assertTrue(profitNegativePosition.hasLoss(),
                "A position with negative profit must report hasLoss() == true");
    }

    @Test
    void whenShortPositionEndsWithLoss_thenHasLossIsTrue() {
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

    // -----------------------------------------------------------------
    // New scenarios derived from the JSON specification
    // -----------------------------------------------------------------

    @Test
    void whenProfitIsZero_thenHasLossIsFalse() {
        // BUY @3, SELL @3 → zero profit (ignoring transaction costs)
        Trade entry = Trade.buyAt(0,
                DoubleNum.valueOf(3),
                DoubleNum.valueOf(1),
                transactionModel);
        Trade exit = Trade.sellAt(1,
                DoubleNum.valueOf(3),
                DoubleNum.valueOf(1),
                transactionModel);
        Position zeroProfitPos = new Position(entry, exit,
                transactionModel, holdingModel);

        assertTrue(zeroProfitPos.getProfit().isZero(),
                "Setup error: profit should be zero");
        assertFalse(zeroProfitPos.hasLoss(),
                "Zero profit must result in hasLoss() == false");
    }

    @ParameterizedTest(name = "{index} => profit={0}, expected={1}")
    @CsvSource({
            // profit value, expected hasLoss()
            "NaN,false",
            "Infinity,false",
            "-Infinity,true"
    })
    void whenProfitIsSpecialNumericValue_thenHasLossBehavesAccordingly(String profitLiteral, boolean expected) {
        // Build a Num from the literal string
        double value;
        switch (profitLiteral) {
            case "NaN": value = Double.NaN; break;
            case "Infinity": value = Double.POSITIVE_INFINITY; break;
            case "-Infinity": value = Double.NEGATIVE_INFINITY; break;
            default: throw new IllegalArgumentException("Unsupported literal");
        }
        Num specialNum = DoubleNum.valueOf(value);

        // Spy a Position and force getProfit() to return the special Num
        Position spyPos = spy(new Position());
        doReturn(specialNum).when(spyPos).getProfit();

        assertEquals(expected, spyPos.hasLoss(),
                "hasLoss() should return " + expected + " for profit = " + profitLiteral);
    }

    @Test
    void whenGetProfitReturnsNull_thenHasLossThrowsNullPointerException() {
        Position spyPos = spy(new Position());
        doReturn(null).when(spyPos).getProfit();

        assertThrows(NullPointerException.class, spyPos::hasLoss,
                "If getProfit() returns null, hasLoss() must propagate NullPointerException");
    }

    @Test
    void whenNumIsNegativeThrowsRuntimeException_thenHasLossPropagates() {
        // Mock a Num whose isNegative() throws RuntimeException
        Num badNum = mock(Num.class);
        when(badNum.isNegative()).thenThrow(new RuntimeException("boom"));

        Position spyPos = spy(new Position());
        doReturn(badNum).when(spyPos).getProfit();

        RuntimeException ex = assertThrows(RuntimeException.class, spyPos::hasLoss,
                "RuntimeException from Num.isNegative() must propagate");
        assertEquals("boom", ex.getMessage());
    }

    @Test
    void whenNumRepresentsZeroButIsNegativeReturnsTrue_thenHasLossIsTrue() {
        // Mock a Num that represents zero but lies about negativity
        Num deceptiveNum = mock(Num.class);
        when(deceptiveNum.isNegative()).thenReturn(true);
        // other Num methods are irrelevant for hasLoss()
        Position spyPos = spy(new Position());
        doReturn(deceptiveNum).when(spyPos).getProfit();

        assertTrue(spyPos.hasLoss(),
                "If Num.isNegative() returns true (even for zero), hasLoss() must return true");
    }

    @Test
    void whenGetProfitThrowsIllegalStateException_thenHasLossPropagates() {
        Position spyPos = spy(new Position());
        doThrow(new IllegalStateException("not initialized")).when(spyPos).getProfit();

        IllegalStateException ex = assertThrows(IllegalStateException.class, spyPos::hasLoss,
                "IllegalStateException from getProfit() must propagate");
        assertEquals("not initialized", ex.getMessage());
    }

    @Test
    void whenGetProfitThrowsError_thenHasLossPropagatesError() {
        Position spyPos = spy(new Position());
        doThrow(new AssertionError("assertion failed")).when(spyPos).getProfit();

        AssertionError err = assertThrows(AssertionError.class, spyPos::hasLoss,
                "Error from getProfit() must propagate unchanged");
        assertEquals("assertion failed", err.getMessage());
    }
}
```