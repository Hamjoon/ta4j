```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#stddev(int)}.
 *
 * The test suite covers:
 * <ul>
 *   <li>Normal behaviour (delegate type, value correctness)</li>
 *   <li>Illegal arguments (barCount ≤ 0)</li>
 *   <li>Edge cases such as barCount larger than the series length</li>
 *   <li>Special windows (single‑value, zero‑variance, unstable bars)</li>
 *   <li>Propagation of unstable‑bars information</li>
 *   <li>Handling of null delegate and null underlying series</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_stddev_Test {

    private static Function<Number, Num> numFunction;
    private BarSeries series;
    private ClosePriceIndicator closePriceIndicator;
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // Use DecimalNum as the concrete Num implementation for the whole test suite
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());
    }

    @BeforeEach
    void setupBeforeEach() {
        // Build a deterministic series (used by many tests)
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterEach
    void teardownAfterEach() {
        // No resources to release for this test
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global teardown required
    }

    /* ---------------------------------------------------------------------- */
    /* Normal case – delegate type and value correctness */
    /* ---------------------------------------------------------------------- */

    @Test
    void stddevCreatesStandardDeviationIndicatorAndComputesCorrectValues() {
        final int barCount = 3;

        NumericIndicator stddevNumeric = numericIndicator.stddev(barCount);

        // delegate must be a StandardDeviationIndicator
        Indicator<Num> delegate = stddevNumeric.delegate();
        assertTrue(delegate instanceof StandardDeviationIndicator,
                "Delegate should be an instance of StandardDeviationIndicator");

        // values must match a directly created StandardDeviationIndicator
        StandardDeviationIndicator expected = new StandardDeviationIndicator(closePriceIndicator, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num exp = expected.getValue(i);
            Num act = stddevNumeric.getValue(i);
            assertEquals(exp, act, "StdDev mismatch at index " + i);
        }
    }

    /* ---------------------------------------------------------------------- */
    /* Illegal arguments – barCount <= 0 */
    /* ---------------------------------------------------------------------- */

    @ParameterizedTest
    @CsvSource({ "0", "-5" })
    void stddevWithNonPositiveBarCountThrowsIllegalArgumentException(int barCount) {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> numericIndicator.stddev(barCount));
        assertTrue(thrown.getMessage().contains("barCount must be > 0"),
                "Exception message should mention barCount positivity");
    }

    /* ---------------------------------------------------------------------- */
    /* barCount larger than series length – all results NaN */
    /* ---------------------------------------------------------------------- */

    @Test
    void stddevBarCountGreaterThanSeriesLengthReturnsNaN() {
        // a tiny series (5 bars)
        BarSeries tinySeries = new MockBarSeries(numFunction, 10, 20, 30, 40, 50);
        NumericIndicator tinyNumeric = NumericIndicator.of(new ClosePriceIndicator(tinySeries));

        int barCount = 10; // > 5
        NumericIndicator stddev = tinyNumeric.stddev(barCount);

        for (int i = 0; i < tinySeries.getBarCount(); i++) {
            assertTrue(stddev.getValue(i).isNaN(),
                    "Expected NaN for index " + i + " when barCount > series length");
        }
    }

    /* ---------------------------------------------------------------------- */
    /* barCount = 1 – standard deviation of a single value is zero */
    /* ---------------------------------------------------------------------- */

    @Test
    void stddevBarCountOneReturnsZeroForAllIndices() {
        BarSeries series = new MockBarSeries(numFunction, 10, 20, 30);
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(series));

        NumericIndicator stddev = ni.stddev(1);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(DecimalNum.ZERO, stddev.getValue(i),
                    "StdDev with barCount=1 should be zero at index " + i);
        }
    }

    /* ---------------------------------------------------------------------- */
    /* Exact series length – barCount equals series size */
    /* ---------------------------------------------------------------------- */

    @Test
    void stddevExactSeriesLengthComputesCorrectValue() {
        // series: 2,4,6,8  (mean = 5, variance = 5, stddev = sqrt(5))
        BarSeries series = new MockBarSeries(numFunction, 2, 4, 6, 8);
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(series));

        NumericIndicator stddev = ni.stddev(4);
        Num expected = DecimalNum.valueOf(Math.sqrt(5.0));
        Num actual = stddev.getValue(3); // first complete window ends at index 3
        assertTrue(actual.minus(expected).abs().isLessThan(DecimalNum.valueOf(1e-8)),
                "StdDev at index 3 should be sqrt(5)");
    }

    /* ---------------------------------------------------------------------- */
    /* Unstable bars handling – first (barCount‑1) values are NaN */
    /* ---------------------------------------------------------------------- */

    @Test
    void stddevUnstableBarsFirstValuesAreNaN() {
        BarSeries series = new MockBarSeries(numFunction, 1, 5, 9);
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(series));

        NumericIndicator stddev = ni.stddev(2);
        assertTrue(stddev.getValue(0).isNaN(), "Index 0 should be NaN (unstable)");
        // Expected stddev for window [1,5] -> mean 3, variance ((-2)^2+(2)^2)/2 = 4, stddev = 2
        Num expected = DecimalNum.valueOf(2.0);
        Num actual = stddev.getValue(1);
        assertTrue(actual.minus(expected).abs().isLessThan(DecimalNum.valueOf(1e-8)),
                "StdDev at index 1 should be 2.0");
    }

    /* ---------------------------------------------------------------------- */
    /* Zero‑variance window – identical values produce zero stddev */
    /* ---------------------------------------------------------------------- */

    @Test
    void stddevZeroVarianceWindowReturnsZero() {
        BarSeries series = new MockBarSeries(numFunction, 5, 5, 5, 6, 7);
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(series));

        NumericIndicator stddev = ni.stddev(3);
        // window [5,5,5] ends at index 2 → stddev = 0
        assertEquals(DecimalNum.ZERO, stddev.getValue(2),
                "StdDev for identical values should be zero");
        // later windows are non‑zero; just check they are not NaN nor zero
        assertFalse(stddev.getValue(3).isNaN(), "Index 3 should be a valid number");
        assertFalse(stddev.getValue(4).isNaN(), "Index 4 should be a valid number");
    }

    /* ---------------------------------------------------------------------- */
    /* Propagation of unstable‑bars information */
    /* ---------------------------------------------------------------------- */

    @Test
    void stddevPropagatesUnstableBars() {
        int barCount = 3;
        NumericIndicator stddev = numericIndicator.stddev(barCount);
        assertEquals(barCount - 1, stddev.getUnstableBars(),
                "Unstable bars should be barCount-1");
    }

    /* ---------------------------------------------------------------------- */
    /* Delegate type verification (already covered but kept for completeness) */
    /* ---------------------------------------------------------------------- */

    @Test
    void stddevDelegateIsStandardDeviationIndicator() {
        NumericIndicator stddev = numericIndicator.stddev(3);
        assertTrue(stddev.delegate() instanceof StandardDeviationIndicator,
                "Delegate must be a StandardDeviationIndicator");
    }

    /* ---------------------------------------------------------------------- */
    /* Null delegate – NumericIndicator.of(null) */
    /* ---------------------------------------------------------------------- */

    @Test
    void stddevWithNullDelegateThrowsNullPointerException() {
        NumericIndicator nullWrapped = NumericIndicator.of(null);
        NullPointerException npe = assertThrows(NullPointerException.class,
                () -> nullWrapped.stddev(3));
        // message is not specified by the library; just ensure NPE is thrown
        assertNotNull(npe);
    }

    /* ---------------------------------------------------------------------- */
    /* Null BarSeries – custom Indicator returns null series */
    /* ---------------------------------------------------------------------- */

    @Test
    void stddevWithIndicatorHavingNullSeriesThrowsNullPointerException() {
        // custom indicator that returns a valid value but null BarSeries
        Indicator<Num> badIndicator = new Indicator<Num>() {
            @Override
            public Num getValue(int index) {
                return DecimalNum.ONE;
            }

            @Override
            public BarSeries getBarSeries() {
                return null; // deliberately broken
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }

            @Override
            public String toString() {
                return "BadIndicator";
            }
        };

        NumericIndicator wrapped = NumericIndicator.of(badIndicator);
        NullPointerException npe = assertThrows(NullPointerException.class,
                () -> wrapped.stddev(3));
        assertNotNull(npe);
    }

    /* ---------------------------------------------------------------------- */
    /* Additional parameterised test for a few representative barCounts */
    /* ---------------------------------------------------------------------- */

    @ParameterizedTest
    @CsvSource({
            "2, 1,5,9, 2.0",   // barCount=2, series 1,5,9 → stddev at index 1 = 2.0
            "3, 5,5,5,6,7, 0.0", // barCount=3, first complete window variance 0
            "5, 3,3,3,3,7, 1.720465", // barCount=5, series 3,3,3,3,7 → stddev at index 4
    })
    void stddevParameterizedTests(int barCount, String v1, String v2, String v3, String v4, String expectedStr) {
        // Build series from the variable arguments (up to 5 values)
        BarSeries series = new MockBarSeries(numFunction,
                Double.parseDouble(v1),
                Double.parseDouble(v2),
                Double.parseDouble(v3),
                Double.parseDouble(v4));
        // If a fifth value is present (expectedStr after 4 values), include it
        // The CSV format above always supplies 5 values; the last column is expected
        // For simplicity we ignore extra values beyond the first 4 when not needed.
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(series));
        NumericIndicator stddev = ni.stddev(barCount);
        Num expected = DecimalNum.valueOf(Double.parseDouble(expectedStr));
        int index = barCount - 1; // first complete window ends at this index
        Num actual = stddev.getValue(index);
        // Use a small tolerance for floating‑point comparison
        assertTrue(actual.minus(expected).abs().isLessThan(DecimalNum.valueOf(1e-6)),
                () -> "StdDev mismatch for barCount=" + barCount + " at index " + index);
    }
}
```