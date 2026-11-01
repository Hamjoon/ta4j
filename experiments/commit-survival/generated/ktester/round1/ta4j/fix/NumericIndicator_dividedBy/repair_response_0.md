```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.util.function.Function;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator#dividedBy(Indicator)}.
 *
 * <p>The original test suite depended on JUnit 5, Mockito and a {@code MockBarSeries}
 * implementation that are not available in the execution environment.  The test
 * code has been rewritten to use JUnit 4 only and to avoid any external test‑only
 * classes.  A dummy {@code BarSeries} reference (null) is sufficient because the
 * indicators used in the tests either ignore the series or only store it for
 * {@code getBarSeries()} calls.</p>
 */
public class NumericIndicator_dividedBy_Test {

    /** dummy series reference – not used by the indicators under test */
    private static final BarSeries DUMMY_SERIES = null;

    /** a simple number‑to‑Num mapper used by {@link #constantNumeric(double)} */
    private static final Function<Number, Num> NUM_FUNCTION = n -> DecimalNum.valueOf(n.doubleValue());

    /**
     * Helper to build a constant {@link NumericIndicator} (numerator).
     */
    private static NumericIndicator constantNumeric(double value) {
        Indicator<Num> constInd = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(value));
        return NumericIndicator.of(constInd);
    }

    /**
     * Indicator that returns (index + 1) as {@link Num}.
     */
    private static class IndexIndicator implements Indicator<Num> {
        @Override public Num getValue(int index) { return DecimalNum.valueOf(index + 1); }
        @Override public BarSeries getBarSeries() { return DUMMY_SERIES; }
        @Override public int getUnstableBars() { return 0; }
        @Override public String toString() { return "IndexIndicator"; }
    }

    /**
     * Indicator that alternates 2 (even) and 0 (odd).
     */
    private static class AlternatingZeroIndicator implements Indicator<Num> {
        @Override public Num getValue(int index) {
            return (index % 2 == 0) ? DecimalNum.valueOf(2) : DecimalNum.valueOf(0);
        }
        @Override public BarSeries getBarSeries() { return DUMMY_SERIES; }
        @Override public int getUnstableBars() { return 0; }
        @Override public String toString() { return "AlternatingZeroIndicator"; }
    }

    /**
     * Indicator that always returns {@code null}.
     */
    private static class NullReturningIndicator implements Indicator<Num> {
        @Override public Num getValue(int index) { return null; }
        @Override public BarSeries getBarSeries() { return DUMMY_SERIES; }
        @Override public int getUnstableBars() { return 0; }
    }

    /**
     * Indicator that throws {@link IllegalStateException} on any {@code getValue} call.
     */
    private static class ExceptionThrowingIndicator implements Indicator<Num> {
        @Override public Num getValue(int index) {
            throw new IllegalStateException("Deliberate exception from divisor");
        }
        @Override public BarSeries getBarSeries() { return DUMMY_SERIES; }
        @Override public int getUnstableBars() { return 0; }
    }

    /** a tiny series length used for loops that need a size */
    private static final int SERIES_SIZE = 5;

    /* --------------------------------------------------------------------- */
    /* Helper assertion utilities                                            */
    /* --------------------------------------------------------------------- */

    private static void assertNumEquals(Num expected, Num actual) {
        assertTrue("Expected " + expected + " but got " + actual,
                expected.isEqual(actual));
    }

    private static void assertNumEquals(Num expected, Num actual, String message) {
        assertTrue(message, expected.isEqual(actual));
    }

    private static <T extends Throwable> void assertThrows(Class<T> expected, Runnable runnable) {
        try {
            runnable.run();
            fail("Expected exception of type " + expected.getName());
        } catch (Throwable actual) {
            if (!expected.isInstance(actual)) {
                fail("Expected exception of type " + expected.getName()
                        + " but got " + actual.getClass().getName());
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /* Tests                                                                */
    /* --------------------------------------------------------------------- */

    @Test
    public void dividedByConstantIndicator() {
        // given
        Indicator<Num> closePrice = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(10));
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        Indicator<Num> constantFive = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(5));
        // when
        NumericIndicator result = numeric.dividedBy(constantFive);
        // then
        assertNumEquals(closePrice.getValue(0).dividedBy(DecimalNum.valueOf(5)),
                result.getValue(0));
        assertNumEquals(closePrice.getValue(3).dividedBy(DecimalNum.valueOf(5)),
                result.getValue(3));
    }

    @Test
    public void dividedByDynamicIndicator() {
        // given
        Indicator<Num> closePrice = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(12));
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        // EMAIndicator implements Indicator<Num> – we can use a simple constant as a stand‑in
        Indicator<Num> divisor = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(3));
        // when
        NumericIndicator result = numeric.dividedBy(divisor);
        // then
        assertNumEquals(closePrice.getValue(0).dividedBy(divisor.getValue(0)),
                result.getValue(0));
        assertNumEquals(closePrice.getValue(2).dividedBy(divisor.getValue(2)),
                result.getValue(2));
    }

    @Test
    public void delegateIsCorrectAfterDivision() {
        // given
        Indicator<Num> closePrice = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(8));
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        Indicator<Num> divisor = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(2));
        // when
        NumericIndicator result = numeric.dividedBy(divisor);
        // then
        assertNotSame(numeric.delegate(), result.delegate(),
                "The delegate of the divided indicator should be a new instance");
        assertTrue(result.delegate() instanceof Indicator,
                "The delegate must still be an Indicator");
    }

    @Test
    public void normalDivision_WithConstantDivisor() {
        // numerator constant 10, divisor constant 2 → expected 5
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> divisor = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(2));
        NumericIndicator result = numerator.dividedBy(divisor);
        for (int i = 0; i < SERIES_SIZE; i++) {
            assertNumEquals(DecimalNum.valueOf(5), result.getValue(i),
                    "Incorrect division result at index " + i);
        }

        // numerator constant 10, divisor constant 0.5 → expected 20
        divisor = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(0.5));
        result = numerator.dividedBy(divisor);
        for (int i = 0; i < SERIES_SIZE; i++) {
            assertNumEquals(DecimalNum.valueOf(20), result.getValue(i),
                    "Incorrect division result at index " + i);
        }
    }

    @Test
    public void divisionByZeroConstant_ShouldThrowArithmeticException() {
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> zeroDivisor = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(0));
        NumericIndicator result = numerator.dividedBy(zeroDivisor);
        assertThrows(ArithmeticException.class, () -> result.getValue(0));
    }

    @Test
    public void numeratorZero_DividedByNonZero_ShouldYieldZero() {
        NumericIndicator zeroNumerator = constantNumeric(0);
        Indicator<Num> divisor = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(5));
        NumericIndicator result = zeroNumerator.dividedBy(divisor);
        for (int i = 0; i < SERIES_SIZE; i++) {
            assertNumEquals(DecimalNum.ZERO, result.getValue(i));
        }
    }

    @Test
    public void negativeDivisor_ShouldPropagateSign() {
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> negativeDivisor = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(-2));
        NumericIndicator result = numerator.dividedBy(negativeDivisor);
        for (int i = 0; i < SERIES_SIZE; i++) {
            assertNumEquals(DecimalNum.valueOf(-5), result.getValue(i));
        }
    }

    @Test
    public void negativeNumerator_DividedByPositive_ShouldBeNegative() {
        NumericIndicator negativeNumerator = constantNumeric(-10);
        Indicator<Num> divisor = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(2));
        NumericIndicator result = negativeNumerator.dividedBy(divisor);
        for (int i = 0; i < SERIES_SIZE; i++) {
            assertNumEquals(DecimalNum.valueOf(-5), result.getValue(i));
        }
    }

    @Test
    public void varyingDivisor_IndexIndicator() {
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> divisor = new IndexIndicator();
        NumericIndicator result = numerator.dividedBy(divisor);
        for (int i = 0; i < 5; i++) {
            Num expected = DecimalNum.valueOf(10).dividedBy(DecimalNum.valueOf(i + 1));
            assertNumEquals(expected, result.getValue(i));
        }
    }

    @Test
    public void alternatingZeroDivisor_EvenIndicesOk_OddIndicesThrow() {
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> divisor = new AlternatingZeroIndicator();
        NumericIndicator result = numerator.dividedBy(divisor);
        // even index (0) → divisor 2 → 5
        assertNumEquals(DecimalNum.valueOf(5), result.getValue(0));
        // odd index (1) → divisor 0 → exception
        assertThrows(ArithmeticException.class, () -> result.getValue(1));
    }

    @Test
    public void nullDivisorIndicator_ShouldThrowNullPointerExceptionAtEntry() {
        NumericIndicator numerator = constantNumeric(10);
        // cast null to the expected type to avoid the overload ambiguity
        assertThrows(NullPointerException.class,
                () -> numerator.dividedBy((Indicator<Num>) null));
    }

    @Test
    public void divisorReturnsNullNum_ShouldThrowNullPointerExceptionWhenEvaluated() {
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> nullDivisor = new NullReturningIndicator();
        NumericIndicator result = numerator.dividedBy(nullDivisor);
        assertThrows(NullPointerException.class, () -> result.getValue(0));
    }

    @Test
    public void divisorThrowsCustomException_ShouldPropagate() {
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> badDivisor = new ExceptionThrowingIndicator();
        NumericIndicator result = numerator.dividedBy(badDivisor);
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> result.getValue(0));
        assertEquals("Deliberate exception from divisor", ex.getMessage());
    }

    @Test
    public void numeratorWithNullDelegate_ShouldThrowWhenEvaluated() {
        // Create a NumericIndicator whose internal delegate is null
        NumericIndicator nullNumerator = NumericIndicator.of(null);
        Indicator<Num> divisor = new ConstantIndicator<>(DUMMY_SERIES, DecimalNum.valueOf(2));
        NumericIndicator result = nullNumerator.dividedBy(divisor);
        assertThrows(NullPointerException.class, () -> result.getValue(0));
    }

    /* --------------------------------------------------------------------- */
    /* Helper overload for assertThrows returning the caught exception        */
    /* --------------------------------------------------------------------- */
    private static <T extends Throwable> T assertThrows(Class<T> expected, Runnable runnable) {
        try {
            runnable.run();
            fail("Expected exception of type " + expected.getName());
        } catch (Throwable actual) {
            if (!expected.isInstance(actual)) {
                fail("Expected exception of type " + expected.getName()
                        + " but got " + actual.getClass().getName());
            }
            @SuppressWarnings("unchecked")
            T casted = (T) actual;
            return casted;
        }
        // unreachable
        return null;
    }
}
```