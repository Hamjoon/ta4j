```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator#minus(Indicator)}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>Subtraction with a {@link ConstantIndicator} (numeric overload).</li>
 *   <li>Behaviour when {@code null} is passed as the other indicator.</li>
 *   <li>Series‑mismatch handling.</li>
 *   <li>Handling of an indicator that returns {@code null} for a value.</li>
 * </ul>
 *
 * Existing tests for the {@code Number} overload and the normal dynamic
 * overload are kept unchanged.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_minus_Test {

    /** Series used by most tests – created once for the whole test class. */
    private static BarSeries series;

    /** Close‑price indicator that will be wrapped by {@link NumericIndicator}. */
    private static ClosePriceIndicator closePrice;

    /** EMA indicator used as a dynamic operand in the {@code minus} test. */
    private static EMAIndicator ema;

    /** Helper to convert primitive numbers to {@link Num}. */
    private static Function<Number, Num> numFunction;

    /** The {@link NumericIndicator} instance under test. */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // Convert a Number to the library's Num implementation (DecimalNum)
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());

        // Create a deterministic series: 1, 2, 3, …, 9, 8, 7, …, -2
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2);

        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
    }

    @BeforeEach
    void setupBeforeEach() {
        // Wrap the close‑price indicator with the fluent NumericIndicator
        numericIndicator = NumericIndicator.of(closePrice);
    }

    @AfterEach
    void teardownAfterEach() {
        // No mutable state to clean up between tests
    }

    @AfterAll
    static void teardownAfterAll() {
        // Release static references (helpful for large test suites)
        series = null;
        closePrice = null;
        ema = null;
        numFunction = null;
    }

    /* --------------------------------------------------------------------- */
    /* Existing tests (kept unchanged)                                       */
    /* --------------------------------------------------------------------- */

    @Test
    void minusWithNumber() {
        // static subtraction:  value - 5
        NumericIndicator result = numericIndicator.minus(5);
        assertNumEquals(1 - 5, result.getValue(0));
        assertNumEquals(9 - 5, result.getValue(8));
        assertNumEquals(-2 - 5, result.getValue(series.getBarCount() - 1));
    }

    @Test
    void minusWithIndicator() {
        // dynamic subtraction:  value - ema(value)
        NumericIndicator result = numericIndicator.minus(ema);
        // index 0
        Num expected0 = closePrice.getValue(0).minus(ema.getValue(0));
        assertNumEquals(expected0, result.getValue(0));

        // index 8 (the peak of the series)
        Num expected8 = closePrice.getValue(8).minus(ema.getValue(8));
        assertNumEquals(expected8, result.getValue(8));

        // a later index to ensure the operation works throughout the series
        int idx = 15;
        Num expectedIdx = closePrice.getValue(idx).minus(ema.getValue(idx));
        assertNumEquals(expectedIdx, result.getValue(idx));
    }

    @Test
    void minusUsesConstantIndicatorInternally() {
        // The overload that accepts a Number delegates to the Indicator overload
        // using a ConstantIndicator. We verify the behaviour is identical.
        NumericIndicator viaNumber = numericIndicator.minus(7);
        NumericIndicator viaIndicator = numericIndicator.minus(
                new ConstantIndicator<>(closePrice, numFunction.apply(7)));

        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(viaNumber.getValue(i), viaIndicator.getValue(i));
        }
    }

    /* --------------------------------------------------------------------- */
    /* New tests derived from the JSON specification                         */
    /* --------------------------------------------------------------------- */

    /**
     * Parameterized test that subtracts a {@link ConstantIndicator} from a
     * close‑price indicator built on a tiny series {@code [10,20,30]}.
     *
     * <p>Two cases are exercised:
     * <ul>
     *   <li>Subtracting a positive constant (5) → expected {@code [5,15,25]}</li>
     *   <li>Subtracting a negative constant (-3) → expected {@code [13,23,33]}</li>
     * </ul>
     */
    @ParameterizedTest(name = "{index} – subtract constant {0}")
    @MethodSource("constantIndicatorProvider")
    void minus_withConstantIndicator(Number constant, int[] expected) {
        // tiny series with known close prices
        BarSeries tinySeries = new MockBarSeries(numFunction, 10, 20, 30);
        ClosePriceIndicator tinyClose = new ClosePriceIndicator(tinySeries);
        NumericIndicator underTest = NumericIndicator.of(tinyClose);

        // Build the constant indicator that will be subtracted
        ConstantIndicator<Num> constInd = new ConstantIndicator<>(tinyClose,
                numFunction.apply(constant));

        NumericIndicator result = underTest.minus(constInd);

        for (int i = 0; i < expected.length; i++) {
            assertNumEquals(expected[i], result.getValue(i));
        }
    }

    /** Supplies the parameters for {@link #minus_withConstantIndicator(Number, int[])}. */
    private static Stream<Arguments> constantIndicatorProvider() {
        return Stream.of(
                Arguments.of(5, new int[]{5, 15, 25}),
                Arguments.of(-3, new int[]{13, 23, 33})
        );
    }

    /** Verifies that passing {@code null} as the other indicator results in NPE. */
    @Test
    void minus_nullOther() {
        assertThrows(NullPointerException.class,
                () -> numericIndicator.minus((Indicator<Num>) null));
    }

    /** Ensures that subtracting an indicator belonging to a different series throws. */
    @Test
    void minus_differentBarSeries() {
        // a second series with arbitrary values
        BarSeries seriesB = new MockBarSeries(numFunction, 1, 2, 3);
        ClosePriceIndicator closeB = new ClosePriceIndicator(seriesB);

        // The library is expected to validate series compatibility
        assertThrows(IllegalArgumentException.class,
                () -> numericIndicator.minus(closeB));
    }

    /**
     * Tests the behaviour when the supplied indicator returns {@code null}
     * for a value. The resulting {@link NumericIndicator} should propagate the
     * {@link NullPointerException} when its value is accessed.
     */
    @Test
    void minus_indicatorReturningNull() {
        // Custom indicator that returns null for index 0, otherwise delegates to closePrice
        Indicator<Num> nullReturningIndicator = new Indicator<Num>() {
            @Override
            public Num getValue(int index) {
                return index == 0 ? null : closePrice.getValue(index);
            }

            @Override
            public BarSeries getBarSeries() {
                return series;
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }
        };

        NumericIndicator result = numericIndicator.minus(nullReturningIndicator);

        // Accessing index 0 should trigger the NPE
        assertThrows(NullPointerException.class, () -> result.getValue(0));

        // A non‑null index should work fine (sanity check)
        assertDoesNotThrow(() -> result.getValue(1));
    }
}
```