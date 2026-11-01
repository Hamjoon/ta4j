```java
package org.ta4j.core;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.junit.Test;
import org.junit.Before;

/**
 * Unit tests for {@link Position#hasLoss()}.
 *
 * <p>This test class is written using JUnit 4 only, so it does not rely on
 * JUnit 5/Jupiter or Mockito extensions.</p>
 */
public class Position_hasLoss_Test {

    private Position emptyPosition;
    private Position profitPositivePosition;
    private Position profitNegativePosition;
    private CostModel transactionModel;
    private CostModel holdingModel;

    /**
     * Helper subclass that allows us to force a profit value or to throw
     * exceptions / errors from {@link #getProfit()}.
     */
    private static class TestPosition extends Position {

        private Num forcedProfit = null;
        private RuntimeException exceptionToThrow = null;
        private Error errorToThrow = null;

        void setForcedProfit(Num profit) {
            this.forcedProfit = profit;
        }

        void setExceptionToThrow(RuntimeException ex) {
            this.exceptionToThrow = ex;
        }

        void setErrorToThrow(Error err) {
            this.errorToThrow = err;
        }

        @Override
        public Num getProfit() {
            if (errorToThrow != null) {
                throw errorToThrow;
            }
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            if (forcedProfit != null) {
                return forcedProfit;
            }
            return super.getProfit();
        }
    }

    @Before
    public void setUp() {
        // Cost models used by the trades/positions
        transactionModel = new LinearTransactionCostModel(0.01);
        holdingModel = new LinearBorrowingCostModel(0.001);

        // 1. Empty position – default constructor (BUY start)
        emptyPosition = new Position();

        // 2. Positive‑profit closed position (BUY @2, SELL @5)
        Trade entryBuy = Trade.buyAt(0, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        Trade exitSell = Trade.sellAt(1, DoubleNum.valueOf(5), DoubleNum.valueOf(1), transactionModel);
        profitPositivePosition = new Position(entryBuy, exitSell, transactionModel, holdingModel);

        // 3. Negative‑profit closed position (BUY @5, SELL @2)
        Trade entryBuyLoss = Trade.buyAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(1), transactionModel);
        Trade exitSellLoss = Trade.sellAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        profitNegativePosition = new Position(entryBuyLoss, exitSellLoss, transactionModel, holdingModel);
    }

    /* --------------------------------------------------------------------- */
    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError("assertTrue failed: " + msg);
        }
    }

    private static void assertFalse(boolean condition, String msg) {
        if (condition) {
            throw new AssertionError("assertFalse failed: " + msg);
        }
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("assertEquals failed: " + msg + " – expected: " + expected + ", actual: " + actual);
        }
    }

    /**
     * Executes {@code executable} and returns the thrown exception of type {@code T}.
     */
    private static <T extends Throwable> T assertThrows(Class<T> expectedType, Runnable executable, String msg) {
        try {
            executable.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                @SuppressWarnings("unchecked")
                T casted = (T) t;
                return casted;
            }
            throw new AssertionError("assertThrows failed: " + msg + " – unexpected exception type: " + t);
        }
        throw new AssertionError("assertThrows failed: " + msg + " – no exception thrown");
    }

    /* --------------------------------------------------------------------- */
    public static void main(String[] args) {
        Position_hasLoss_Test test = new Position_hasLoss_Test();
        test.setUp();
        test.whenPositionIsEmpty_thenHasLossIsFalse();
        test.whenPositionHasPositiveProfit_thenHasLossIsFalse();
        test.whenPositionHasNegativeProfit_thenHasLossIsTrue();
        test.whenShortPositionEndsWithLoss_thenHasLossIsTrue();
        test.whenShortPositionEndsWithProfit_thenHasLossIsFalse();
        test.whenProfitIsZero_thenHasLossIsFalse();
        test.whenProfitIsSpecialNumericValue_thenHasLossBehavesAccordingly();
        test.whenGetProfitReturnsNull_thenHasLossThrowsNullPointerException();
        test.whenNumIsNegativeThrowsRuntimeException_thenHasLossPropagates();
        test.whenNumRepresentsZeroButIsNegativeReturnsTrue_thenHasLossIsTrue();
        test.whenGetProfitThrowsIllegalStateException_thenHasLossPropagates();
        test.whenGetProfitThrowsError_thenHasLossPropagatesError();
        System.out.println("All Position.hasLoss() tests passed.");
    }

    @Test
    public void whenPositionIsEmpty_thenHasLossIsFalse() {
        assertFalse(emptyPosition.hasLoss(),
                "A newly created position should not be considered as having a loss");
    }

    @Test
    public void whenPositionHasPositiveProfit_thenHasLossIsFalse() {
        assertTrue(profitPositivePosition.getProfit().isPositive(),
                "Setup error: expected a positive profit for the position");
        assertFalse(profitPositivePosition.hasLoss(),
                "A position with positive profit must report hasLoss() == false");
    }

    @Test
    public void whenPositionHasNegativeProfit_thenHasLossIsTrue() {
        assertTrue(profitNegativePosition.getProfit().isNegative(),
                "Setup error: expected a negative profit for the position");
        assertTrue(profitNegativePosition.hasLoss(),
                "A position with negative profit must report hasLoss() == true");
    }

    @Test
    public void whenShortPositionEndsWithLoss_thenHasLossIsTrue() {
        Trade entrySell = Trade.sellAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(1), transactionModel);
        Trade exitBuy = Trade.buyAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        Position shortLoss = new Position(entrySell, exitBuy, transactionModel, holdingModel);
        assertTrue(shortLoss.getProfit().isNegative(),
                "Setup error: expected a negative profit for the short position");
        assertTrue(shortLoss.hasLoss(),
                "Short position with loss must report hasLoss() == true");
    }

    @Test
    public void whenShortPositionEndsWithProfit_thenHasLossIsFalse() {
        Trade entrySell = Trade.sellAt(0, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        Trade exitBuy = Trade.buyAt(1, DoubleNum.valueOf(5), DoubleNum.valueOf(1), transactionModel);
        Position shortProfit = new Position(entrySell, exitBuy, transactionModel, holdingModel);
        assertTrue(shortProfit.getProfit().isPositive(),
                "Setup error: expected a positive profit for the short position");
        assertFalse(shortProfit.hasLoss(),
                "Short position with profit must report hasLoss() == false");
    }

    @Test
    public void whenProfitIsZero_thenHasLossIsFalse() {
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(3), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(3), DoubleNum.valueOf(1), transactionModel);
        Position zeroProfitPos = new Position(entry, exit, transactionModel, holdingModel);
        assertTrue(zeroProfitPos.getProfit().isZero(),
                "Setup error: profit should be zero");
        assertFalse(zeroProfitPos.hasLoss(),
                "Zero profit must result in hasLoss() == false");
    }

    @Test
    public void whenProfitIsSpecialNumericValue_thenHasLossBehavesAccordingly() {
        Object[][] data = {
                { "NaN", false },
                { "Infinity", false },
                { "-Infinity", true }
        };
        for (Object[] row : data) {
            String literal = (String) row[0];
            boolean expectedValue = (boolean) row[1];
            double value;
            switch (literal) {
                case "NaN":
                    value = Double.NaN;
                    break;
                case "Infinity":
                    value = Double.POSITIVE_INFINITY;
                    break;
                case "-Infinity":
                    value = Double.NEGATIVE_INFINITY;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported literal");
            }
            Num specialNum = DoubleNum.valueOf(value);
            TestPosition pos = new TestPosition();
            pos.setForcedProfit(specialNum);
            assertEquals(expectedValue, pos.hasLoss(),
                    "hasLoss() should return " + expectedValue + " for profit = " + literal);
        }
    }

    @Test
    public void whenGetProfitReturnsNull_thenHasLossThrowsNullPointerException() {
        TestPosition pos = new TestPosition();
        pos.setForcedProfit(null);
        assertThrows(NullPointerException.class, pos::hasLoss,
                "If getProfit() returns null, hasLoss() must propagate NullPointerException");
    }

    @Test
    public void whenNumIsNegativeThrowsRuntimeException_thenHasLossPropagates() {
        // Mock a Num whose isNegative() throws RuntimeException
        Num badNum = new Num() {

            @Override
            public Num plus(Num other) { return null; }

            @Override
            public Num minus(Num other) { return null; }

            @Override
            public Num multipliedBy(Num other) { return null; }

            @Override
            public Num dividedBy(Num other) { return null; }

            @Override
            public Num remainder(Num other) { return null; }

            @Override
            public Num abs() { return null; }

            @Override
            public Num negated() { return null; }

            @Override
            public Num pow(int n) { return null; }

            @Override
            public Num sqrt() { return null; }

            @Override
            public Num log() { return null; }

            @Override
            public Num log10() { return null; }

            @Override
            public Num sin() { return null; }

            @Override
            public Num cos() { return null; }

            @Override
            public Num tan() { return null; }

            @Override
            public Num asin() { return null; }

            @Override
            public Num acos() { return null; }

            @Override
            public Num atan() { return null; }

            @Override
            public Num atan2(Num other) { return null; }

            @Override
            public Num exp() { return null; }

            @Override
            public Num floor() { return null; }

            @Override
            public Num ceil() { return null; }

            @Override
            public Num round() { return null; }

            @Override
            public Num stripTrailingZeros() { return null; }

            @Override
            public Num min(Num other) { return null; }

            @Override
            public Num max(Num other) { return null; }

            @Override
            public int compareTo(Num o) { return 0; }

            @Override
            public boolean isZero() { return false; }

            @Override
            public boolean isPositive() { return false; }

            @Override
            public boolean isNegative() { throw new RuntimeException("boom"); }

            @Override
            public double doubleValue() { return 0; }

            @Override
            public String toString() { return "badNum"; }

            // Num extends Function<Number,Num>
            @Override
            public Num apply(Number number) { return this; }
        };
        TestPosition pos = new TestPosition();
        pos.setForcedProfit(badNum);
        RuntimeException ex = assertThrows(RuntimeException.class, pos::hasLoss,
                "RuntimeException from Num.isNegative() must propagate");
        assertEquals("boom", ex.getMessage(),
                "Exception message should be 'boom'");
    }

    @Test
    public void whenNumRepresentsZeroButIsNegativeReturnsTrue_thenHasLossIsTrue() {
        Num deceptiveNum = new Num() {

            @Override
            public Num plus(Num other) { return null; }

            @Override
            public Num minus(Num other) { return null; }

            @Override
            public Num multipliedBy(Num other) { return null; }

            @Override
            public Num dividedBy(Num other) { return null; }

            @Override
            public Num remainder(Num other) { return null; }

            @Override
            public Num abs() { return null; }

            @Override
            public Num negated() { return null; }

            @Override
            public Num pow(int n) { return null; }

            @Override
            public Num sqrt() { return null; }

            @Override
            public Num log() { return null; }

            @Override
            public Num log10() { return null; }

            @Override
            public Num sin() { return null; }

            @Override
            public Num cos() { return null; }

            @Override
            public Num tan() { return null; }

            @Override
            public Num asin() { return null; }

            @Override
            public Num acos() { return null; }

            @Override
            public Num atan() { return null; }

            @Override
            public Num atan2(Num other) { return null; }

            @Override
            public Num exp() { return null; }

            @Override
            public Num floor() { return null; }

            @Override
            public Num ceil() { return null; }

            @Override
            public Num round() { return null; }

            @Override
            public Num stripTrailingZeros() { return null; }

            @Override
            public Num min(Num other) { return null; }

            @Override
            public Num max(Num other) { return null; }

            @Override
            public int compareTo(Num o) { return 0; }

            @Override
            public boolean isZero() { return true; }

            @Override
            public boolean isPositive() { return false; }

            // Lies! Returns true even though the value is zero
            @Override
            public boolean isNegative() { return true; }

            @Override
            public double doubleValue() { return 0; }

            @Override
            public String toString() { return "deceptiveNum"; }

            @Override
            public Num apply(Number number) { return this; }
        };
        TestPosition pos = new TestPosition();
        pos.setForcedProfit(deceptiveNum);
        assertTrue(pos.hasLoss(),
                "If Num.isNegative() returns true (even for zero), hasLoss() must return true");
    }

    @Test
    public void whenGetProfitThrowsIllegalStateException_thenHasLossPropagates() {
        TestPosition pos = new TestPosition();
        pos.setExceptionToThrow(new IllegalStateException("not initialized"));
        IllegalStateException ex = assertThrows(IllegalStateException.class, pos::hasLoss,
                "IllegalStateException from getProfit() must propagate");
        assertEquals("not initialized", ex.getMessage(),
                "Exception message should be 'not initialized'");
    }

    @Test
    public void whenGetProfitThrowsError_thenHasLossPropagatesError() {
        TestPosition pos = new TestPosition();
        pos.setErrorToThrow(new AssertionError("assertion failed"));
        AssertionError err = assertThrows(AssertionError.class, pos::hasLoss,
                "Error from getProfit() must propagate unchanged");
        assertEquals("assertion failed", err.getMessage(),
                "Error message should be 'assertion failed'");
    }
}
```