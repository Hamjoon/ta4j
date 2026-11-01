```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Additional tests for {@link NumericIndicator#dividedBy(Indicator)} covering
 * the exhaustive JSON test matrix.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_dividedBy_Test {

    /** a simple number‑to‑Num mapper used by {@link MockBarSeries} */
    private static final Function<Number, Num> NUM_FUNCTION = n -> DecimalNum.valueOf(n.doubleValue());

    private static BarSeries series;
    private static ClosePriceIndicator closePrice;
    private static EMAIndicator ema;

    @BeforeAll
    static void setupBeforeAll() {
        // a short series – the values are not important, they just need to be reproducible
        series = new MockBarSeries(NUM_FUNCTION,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
    }

    @BeforeEach
    void setupBeforeEach() {
        // nothing to initialise per‑test at the moment
    }

    @AfterEach
    void teardownAfterEach() {
        // nothing to clean up per‑test at the moment
    }

    @AfterAll
    static void teardownAfterAll() {
        // no global resources to release
    }

    /* --------------------------------------------------------------------- */
    /* Existing tests (kept unchanged)                                    */
    /* --------------------------------------------------------------------- */

    @Test
    void dividedByConstantIndicator() {
        // given
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        Indicator<Num> constantFive = new ConstantIndicator<>(series, DecimalNum.valueOf(5));

        // when
        NumericIndicator result = numeric.dividedBy(constantFive);

        // then – verify a few sample points
        assertNumEquals(closePrice.getValue(0).dividedBy(DecimalNum.valueOf(5)),
                result.getValue(0));
        assertNumEquals(closePrice.getValue(8).dividedBy(DecimalNum.valueOf(5)),
                result.getValue(8));
    }

    @Test
    void dividedByDynamicIndicator() {
        // given
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        Indicator<Num> divisor = ema; // EMAIndicator implements Indicator<Num>

        // when
        NumericIndicator result = numeric.dividedBy(divisor);

        // then – verify a few sample points
        assertNumEquals(closePrice.getValue(0).dividedBy(ema.getValue(0)),
                result.getValue(0));
        assertNumEquals(closePrice.getValue(8).dividedBy(ema.getValue(8)),
                result.getValue(8));
    }

    @Test
    void delegateIsCorrectAfterDivision() {
        // given
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        Indicator<Num> divisor = new ConstantIndicator<>(series, DecimalNum.valueOf(2));

        // when
        NumericIndicator result = numeric.dividedBy(divisor);

        // then – the delegate of the resulting indicator must be a BinaryOperation (quotient)
        // we only assert that the delegate is not the original one and that it implements Indicator
        assertNotSame(numeric.delegate(), result.delegate(),
                "The delegate of the divided indicator should be a new instance");
        assertTrue(result.delegate() instanceof Indicator,
                "The delegate must still be an Indicator");
    }

    /* --------------------------------------------------------------------- */
    /* New tests derived from the JSON matrix                               */
    /* --------------------------------------------------------------------- */

    /** Helper to build a constant NumericIndicator (numerator) */
    private static NumericIndicator constantNumeric(double value) {
        Indicator<Num> constInd = new ConstantIndicator<>(series, DecimalNum.valueOf(value));
        return NumericIndicator.of(constInd);
    }

    /** Indicator that returns (index + 1) as Num */
    private static class IndexIndicator implements Indicator<Num> {
        private final BarSeries series;

        IndexIndicator(BarSeries series) {
            this.series = series;
        }

        @Override
        public Num getValue(int index) {
            return DecimalNum.valueOf(index + 1);
        }

        @Override
        public BarSeries getBarSeries() {
            return series;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }

        @Override
        public String toString() {
            return "IndexIndicator";
        }
    }

    /** Indicator that alternates 2 (even) and 0 (odd) */
    private static class AlternatingZeroIndicator implements Indicator<Num> {
        private final BarSeries series;

        AlternatingZeroIndicator(BarSeries series) {
            this.series = series;
        }

        @Override
        public Num getValue(int index) {
            return (index % 2 == 0) ? DecimalNum.valueOf(2) : DecimalNum.valueOf(0);
        }

        @Override
        public BarSeries getBarSeries() {
            return series;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }

        @Override
        public String toString() {
            return "AlternatingZeroIndicator";
        }
    }

    /** Indicator that always returns null */
    private static class NullReturningIndicator implements Indicator<Num> {
        private final BarSeries series;

        NullReturningIndicator(BarSeries series) {
            this.series = series;
        }

        @Override
        public Num getValue(int index) {
            return null;
        }

        @Override
        public BarSeries getBarSeries() {
            return series;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }
    }

    /** Indicator that throws IllegalStateException on any getValue call */
    private static class ExceptionThrowingIndicator implements Indicator<Num> {
        private final BarSeries series;

        ExceptionThrowingIndicator(BarSeries series) {
            this.series = series;
        }

        @Override
        public Num getValue(int index) {
            throw new IllegalStateException("Deliberate exception from divisor");
        }

        @Override
        public BarSeries getBarSeries() {
            return series;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }
    }

    /* -------------------- Normal division (parameterized) -------------------- */

    @ParameterizedTest(name = "{index} => divisor={0}, expected={1}")
    @CsvSource({
            // divisor value, expected result (10 / divisor)
            "2,5",
            "0.5,20"
    })
    void normalDivision_WithConstantDivisor(double divisorValue, double expected) {
        // given: numerator constant 10, divisor constant as per parameters
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> divisor = new ConstantIndicator<>(series, DecimalNum.valueOf(divisorValue));

        // when
        NumericIndicator result = numerator.dividedBy(divisor);

        // then – check a few random indices
        for (int i = 0; i < Math.min(5, series.getBarCount()); i++) {
            assertNumEquals(DecimalNum.valueOf(expected), result.getValue(i),
                    "Incorrect division result at index " + i);
        }
    }

    /* -------------------- Division by zero constant -------------------- */

    @Test
    void divisionByZeroConstant_ShouldThrowArithmeticException() {
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> zeroDivisor = new ConstantIndicator<>(series, DecimalNum.valueOf(0));

        NumericIndicator result = numerator.dividedBy(zeroDivisor);

        // The exception is thrown lazily when getValue is evaluated
        assertThrows(ArithmeticException.class, () -> result.getValue(0),
                "Division by zero should raise ArithmeticException");
    }

    /* -------------------- Numerator zero -------------------- */

    @Test
    void numeratorZero_DividedByNonZero_ShouldYieldZero() {
        NumericIndicator zeroNumerator = constantNumeric(0);
        Indicator<Num> divisor = new ConstantIndicator<>(series, DecimalNum.valueOf(5));

        NumericIndicator result = zeroNumerator.dividedBy(divisor);

        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(DecimalNum.ZERO, result.getValue(i),
                    "Zero numerator must produce zero at index " + i);
        }
    }

    /* -------------------- Negative divisor -------------------- */

    @Test
    void negativeDivisor_ShouldPropagateSign() {
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> negativeDivisor = new ConstantIndicator<>(series, DecimalNum.valueOf(-2));

        NumericIndicator result = numerator.dividedBy(negativeDivisor);

        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(DecimalNum.valueOf(-5), result.getValue(i),
                    "Sign should be negative at index " + i);
        }
    }

    /* -------------------- Negative numerator -------------------- */

    @Test
    void negativeNumerator_DividedByPositive_ShouldBeNegative() {
        NumericIndicator negativeNumerator = constantNumeric(-10);
        Indicator<Num> divisor = new ConstantIndicator<>(series, DecimalNum.valueOf(2));

        NumericIndicator result = negativeNumerator.dividedBy(divisor);

        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(DecimalNum.valueOf(-5), result.getValue(i),
                    "Result should be negative at index " + i);
        }
    }

    /* -------------------- Varying divisor (index based) -------------------- */

    @Test
    void varyingDivisor_IndexIndicator() {
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> divisor = new IndexIndicator(series);

        NumericIndicator result = numerator.dividedBy(divisor);

        // Verify first few indices manually
        for (int i = 0; i < 5; i++) {
            Num expected = DecimalNum.valueOf(10).dividedBy(DecimalNum.valueOf(i + 1));
            assertNumEquals(expected, result.getValue(i),
                    "Incorrect quotient at index " + i);
        }
    }

    /* -------------------- Mixed zero divisor (alternating) -------------------- */

    @Test
    void alternatingZeroDivisor_EvenIndicesOk_OddIndicesThrow() {
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> divisor = new AlternatingZeroIndicator(series);

        NumericIndicator result = numerator.dividedBy(divisor);

        // even index (0) -> divisor 2 => 5
        assertNumEquals(DecimalNum.valueOf(5), result.getValue(0));

        // odd index (1) -> divisor 0 => exception
        assertThrows(ArithmeticException.class, () -> result.getValue(1),
                "Division by zero on odd index should raise ArithmeticException");
    }

    /* -------------------- Null divisor indicator argument -------------------- */

    @Test
    void nullDivisorIndicator_ShouldThrowNullPointerExceptionAtEntry() {
        NumericIndicator numerator = constantNumeric(10);
        assertThrows(NullPointerException.class,
                () -> numerator.dividedBy(null),
                "Passing null as divisor must throw NullPointerException immediately");
    }

    /* -------------------- Divisor returns null Num -------------------- */

    @Test
    void divisorReturnsNullNum_ShouldThrowNullPointerExceptionWhenEvaluated() {
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> nullDivisor = new NullReturningIndicator(series);

        NumericIndicator result = numerator.dividedBy(nullDivisor);

        assertThrows(NullPointerException.class, () -> result.getValue(0),
                "Dividing by a null Num should raise NullPointerException");
    }

    /* -------------------- Divisor shorter series (IndexOutOfBounds) -------------------- */

    @Test
    void divisorShorterSeries_ShouldThrowIndexOutOfBoundsWhenBeyondItsLength() {
        // numerator built on the original 20‑bar series
        NumericIndicator numerator = constantNumeric(10);

        // divisor built on a separate series with only 5 bars
        BarSeries shortSeries = new MockBarSeries(NUM_FUNCTION, 1, 2, 3, 4, 5);
        Indicator<Num> shortDivisor = new ConstantIndicator<>(shortSeries, DecimalNum.valueOf(2));

        NumericIndicator result = numerator.dividedBy(shortDivisor);

        // Accessing index 0‑4 works fine (they exist in both series)
        for (int i = 0; i < 5; i++) {
            assertNumEquals(DecimalNum.valueOf(5), result.getValue(i));
        }

        // Index 5 is out of bounds for the divisor series
        assertThrows(IndexOutOfBoundsException.class, () -> result.getValue(5),
                "Access beyond divisor series length must raise IndexOutOfBoundsException");
    }

    /* -------------------- Divisor throws custom exception -------------------- */

    @Test
    void divisorThrowsCustomException_ShouldPropagate() {
        NumericIndicator numerator = constantNumeric(10);
        Indicator<Num> badDivisor = new ExceptionThrowingIndicator(series);

        NumericIndicator result = numerator.dividedBy(badDivisor);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> result.getValue(0));
        assertEquals("Deliberate exception from divisor", ex.getMessage(),
                "Exception from divisor must be propagated unchanged");
    }

    /* -------------------- Numerator with null delegate -------------------- */

    @Test
    void numeratorWithNullDelegate_ShouldThrowWhenEvaluated() {
        // Create a NumericIndicator whose internal delegate is null
        NumericIndicator nullNumerator = NumericIndicator.of(null);
        Indicator<Num> divisor = new ConstantIndicator<>(series, DecimalNum.valueOf(2));

        NumericIndicator result = nullNumerator.dividedBy(divisor);

        // The NPE occurs lazily when the numerator's value is accessed
        assertThrows(NullPointerException.class, () -> result.getValue(0),
                "Numerator with null delegate must cause NPE on evaluation");
    }
}
```