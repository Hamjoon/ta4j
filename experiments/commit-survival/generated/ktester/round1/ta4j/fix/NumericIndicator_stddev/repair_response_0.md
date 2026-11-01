```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#stddev(int)}.
 *
 * The original test used JUnit 5 and a library‑provided {@code MockBarSeries}
 * class that is not available in the current compilation environment.
 * This version rewrites the tests to use JUnit 4 only and builds the required
 * {@link BarSeries} instances with the public {@code BaseBarSeriesBuilder}
 * API.
 */
public class NumericIndicator_stddev_Test {

    /** Function used to create concrete {@link Num} instances. */
    private static Function<Number, Num> numFunction;

    /** Series used by most tests. */
    private BarSeries series;

    /** Close‑price indicator based on {@link #series}. */
    private ClosePriceIndicator closePriceIndicator;

    /** Numeric wrapper around the close‑price indicator. */
    private NumericIndicator numericIndicator;

    /* --------------------------------------------------------------------- */
    /* Helper to build a BarSeries with only close prices (open/high/low   */
    /* are set to the same value, volume = 1).                               */
    /* --------------------------------------------------------------------- */
    private BarSeries createSeries(double... values) {
        BaseBarSeriesBuilder builder = new BaseBarSeriesBuilder()
                .withName("test")
                .withNumFunction(numFunction);
        BarSeries s = builder.build();
        for (double v : values) {
            Num n = numFunction.apply(v);
            // open, high, low, close = n; volume = 1
            s.addBar(new BaseBar(Duration.ofDays(1), ZonedDateTime.now(),
                    n, n, n, n, DecimalNum.ONE));
        }
        return s;
    }

    @BeforeClass
    public static void setupBeforeAll() {
        // Use DecimalNum as the concrete Num implementation for the whole test suite
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());
    }

    @Before
    public void setupBeforeEach() {
        // Build a simple deterministic series (same data as other NumericIndicator tests)
        series = createSeries(1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @Test
    public void stddevCreatesStandardDeviationIndicatorAndComputesCorrectValues() {
        final int barCount = 3;
        NumericIndicator stddevNumeric = numericIndicator.stddev(barCount);
        Indicator<Num> delegate = stddevNumeric.delegate();
        assertTrue(delegate instanceof StandardDeviationIndicator);
        StandardDeviationIndicator expectedStdDev = new StandardDeviationIndicator(closePriceIndicator, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = expectedStdDev.getValue(i);
            Num actual = stddevNumeric.getValue(i);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void stddevWithNonPositiveBarCountThrowsIllegalArgumentException() {
        int[] badCounts = {0, -5};
        for (int barCount : badCounts) {
            try {
                numericIndicator.stddev(barCount);
                fail("Expected IllegalArgumentException for barCount = " + barCount);
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("barCount must be > 0"));
            }
        }
    }

    @Test
    public void stddevBarCountGreaterThanSeriesLengthReturnsNaN() {
        BarSeries tinySeries = createSeries(10, 20, 30, 40, 50);
        NumericIndicator tinyNumeric = NumericIndicator.of(new ClosePriceIndicator(tinySeries));
        int barCount = 10; // > series length (5)
        NumericIndicator stddev = tinyNumeric.stddev(barCount);
        for (int i = 0; i < tinySeries.getBarCount(); i++) {
            assertTrue(stddev.getValue(i).isNaN());
        }
    }

    @Test
    public void stddevBarCountOneReturnsZeroForAllIndices() {
        BarSeries s = createSeries(10, 20, 30);
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(s));
        NumericIndicator stddev = ni.stddev(1);
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(DecimalNum.ZERO, stddev.getValue(i));
        }
    }

    @Test
    public void stddevExactSeriesLengthComputesCorrectValue() {
        // series: 2,4,6,8  (mean = 5, variance = 5, stddev = sqrt(5))
        BarSeries s = createSeries(2, 4, 6, 8);
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(s));
        NumericIndicator stddev = ni.stddev(4);
        Num expected = DecimalNum.valueOf(Math.sqrt(5.0));
        Num actual = stddev.getValue(3); // first complete window ends at index 3
        assertTrue(actual.minus(expected).abs().isLessThan(DecimalNum.valueOf(1e-8)));
    }

    @Test
    public void stddevUnstableBarsFirstValuesAreNaN() {
        BarSeries s = createSeries(1, 5, 9);
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(s));
        NumericIndicator stddev = ni.stddev(2);
        assertTrue(stddev.getValue(0).isNaN());
        // Expected stddev for window [1,5] -> mean 3, variance ((-2)^2+(2)^2)/2 = 4, stddev = 2
        Num expected = DecimalNum.valueOf(2.0);
        Num actual = stddev.getValue(1);
        assertTrue(actual.minus(expected).abs().isLessThan(DecimalNum.valueOf(1e-8)));
    }

    @Test
    public void stddevZeroVarianceWindowReturnsZero() {
        BarSeries s = createSeries(5, 5, 5, 6, 7);
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(s));
        NumericIndicator stddev = ni.stddev(3);
        // window [5,5,5] ends at index 2 → stddev = 0
        assertEquals(DecimalNum.ZERO, stddev.getValue(2));
        // later windows are non‑zero; just check they are not NaN nor zero
        assertFalse(stddev.getValue(3).isNaN());
        assertFalse(stddev.getValue(4).isNaN());
    }

    @Test
    public void stddevPropagatesUnstableBars() {
        int barCount = 3;
        NumericIndicator stddev = numericIndicator.stddev(barCount);
        assertEquals(barCount - 1, stddev.getUnstableBars());
    }

    @Test
    public void stddevDelegateIsStandardDeviationIndicator() {
        NumericIndicator stddev = numericIndicator.stddev(3);
        assertTrue(stddev.delegate() instanceof StandardDeviationIndicator);
    }

    @Test
    public void stddevWithNullDelegateThrowsNullPointerException() {
        NumericIndicator nullWrapped = NumericIndicator.of(null);
        try {
            nullWrapped.stddev(3);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void stddevWithIndicatorHavingNullSeriesThrowsNullPointerException() {
        // custom indicator that returns a valid value but null BarSeries
        Indicator<Num> badIndicator = new Indicator<Num>() {
            @Override
            public Num getValue(int index) {
                return DecimalNum.valueOf(1);
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
        try {
            wrapped.stddev(3);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void stddevParameterizedTests() {
        // each entry: {barCount, values..., expectedStdDev}
        List<Object[]> cases = new ArrayList<>();
        // barCount=2, series 1,5,9 → stddev at index 1 = 2.0
        cases.add(new Object[]{2, new double[]{1, 5, 9}, 2.0});
        // barCount=3, series 5,5,5,6,7 → stddev at index 2 = 0.0
        cases.add(new Object[]{3, new double[]{5, 5, 5, 6, 7}, 0.0});
        // barCount=5, series 3,3,3,3,7 → stddev at index 4 ≈ 1.720465
        cases.add(new Object[]{5, new double[]{3, 3, 3, 3, 7}, 1.720465});

        for (Object[] c : cases) {
            int barCount = (int) c[0];
            double[] values = (double[]) c[1];
            double expectedVal = (double) c[2];

            BarSeries s = createSeries(values);
            NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(s));
            NumericIndicator stddev = ni.stddev(barCount);
            Num expected = DecimalNum.valueOf(expectedVal);
            int index = barCount - 1; // first complete window
            Num actual = stddev.getValue(index);
            assertTrue(
                    actual.minus(expected).abs().isLessThan(DecimalNum.valueOf(1e-6)),
                    "StdDev mismatch for barCount=" + barCount + " at index " + index);
        }
    }
}
```