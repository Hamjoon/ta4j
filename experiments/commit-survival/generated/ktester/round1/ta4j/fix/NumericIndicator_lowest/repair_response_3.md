```java
package org.ta4j.core.indicators.numeric;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Simple test harness for {@link NumericIndicator#lowest(int)}.
 *
 * <p>This class does not rely on JUnit or any external testing framework so that it
 * can be compiled in a plain Java environment. All test methods are invoked from
 * {@link #main(String[])} and use lightweight assertion helpers that throw an
 * {@link AssertionError} when a condition is not met.</p>
 */
public class NumericIndicator_lowest_Test {

    /** Function used by {@link MockBarSeries} to create {@link Num} instances. */
    private static Function<Number, Num> numFunction;

    /** Series used by the tests. */
    private static BarSeries series;

    /** Close‑price indicator based on {@link #series}. */
    private static ClosePriceIndicator closePrice;

    /* --------------------------------------------------------------------- */
    /*                     Simple assertion helpers                         */
    /* --------------------------------------------------------------------- */
    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("assertTrue failed");
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(
                    "assertEquals failed – expected: " + expected + ", actual: " + actual);
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return; // success
            }
            throw new AssertionError(
                    "assertThrows failed – expected exception of type " + expectedType.getName()
                            + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(
                "assertThrows failed – expected exception of type " + expectedType.getName()
                        + " but no exception was thrown");
    }

    /* --------------------------------------------------------------------- */
    /*                     Test lifecycle methods                           */
    /* --------------------------------------------------------------------- */
    private static void setupBeforeAll() {
        // DecimalNum is the default Num implementation in ta4j.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());
        // A simple series with positive, zero and negative values.
        series = buildSeries(1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
    }

    private static void teardownAfterAll() {
        series = null;
        closePrice = null;
        numFunction = null;
    }

    /* --------------------------------------------------------------------- */
    /*                     Helper to build a BarSeries                        */
    /* --------------------------------------------------------------------- */
    private static BarSeries buildSeries(double... values) {
        BarSeries s = new BaseBarSeriesBuilder().withName("test").build();
        ZonedDateTime time = ZonedDateTime.now();
        Duration barDuration = Duration.ofMinutes(1);
        for (double v : values) {
            Num n = numFunction.apply(v);
            // For simplicity all price fields are the same and volume is zero
            BaseBar bar = new BaseBar(barDuration, time,
                    n, // open
                    n, // high
                    n, // low
                    n, // close
                    n, // volume
                    DecimalNum.ZERO); // amount (price * volume, not used here)
            s.addBar(bar);
            time = time.plusMinutes(1);
        }
        return s;
    }

    /* --------------------------------------------------------------------- */
    /*                     Test cases                                        */
    /* --------------------------------------------------------------------- */
    private static void lowestCreatesCorrectDelegateAndValues() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        int barCount = 3;
        NumericIndicator lowest = numeric.lowest(barCount);
        Indicator<Num> delegate = lowest.delegate();
        assertTrue(delegate instanceof LowestValueIndicator);
        LowestValueIndicator expected = new LowestValueIndicator(closePrice, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(expected.getValue(i), lowest.getValue(i));
        }
    }

    private static void lowestWithBarCountGreaterThanSeriesLength() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        int barCount = series.getBarCount() + 5;
        NumericIndicator lowest = numeric.lowest(barCount);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        Num min = closePrice.getValue(0);
        for (int i = 1; i < series.getBarCount(); i++) {
            min = min.min(closePrice.getValue(i));
        }
        assertEquals(min, lowest.getValue(series.getBarCount() - 1));
    }

    private static void lowest_positive_barCount_calculatesCorrectValues() {
        BarSeries s = buildSeries(10, 8, 12, 5, 7);
        ClosePriceIndicator source = new ClosePriceIndicator(s);
        NumericIndicator numeric = NumericIndicator.of(source);
        NumericIndicator lowest = numeric.lowest(3);
        Indicator<Num> delegate = lowest.delegate();
        assertTrue(delegate instanceof LowestValueIndicator);
        Num[] expected = {
                DecimalNum.valueOf(10), DecimalNum.valueOf(8),
                DecimalNum.valueOf(8), DecimalNum.valueOf(5),
                DecimalNum.valueOf(5)
        };
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(expected[i], lowest.getValue(i));
        }
    }

    private static void lowest_invalid_barCount_throwsIllegalArgumentException() {
        BarSeries s = buildSeries(1, 2, 3);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
        int[] invalidBarCounts = {0, -5};
        for (int barCount : invalidBarCounts) {
            assertThrows(IllegalArgumentException.class, () -> numeric.lowest(barCount));
        }
    }

    private static void lowest_barCount_exceeds_series_usesAvailableValues() {
        BarSeries s = buildSeries(4, 9, 2, 7, 5);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
        NumericIndicator lowest = numeric.lowest(10);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        Num[] expected = {
                DecimalNum.valueOf(4), DecimalNum.valueOf(4),
                DecimalNum.valueOf(2), DecimalNum.valueOf(2),
                DecimalNum.valueOf(2)
        };
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(expected[i], lowest.getValue(i));
        }
    }

    private static void lowest_barCount_one_mirrorsSourceValues() {
        BarSeries s = buildSeries(3, 6, 1, 8);
        ClosePriceIndicator source = new ClosePriceIndicator(s);
        NumericIndicator numeric = NumericIndicator.of(source);
        NumericIndicator lowest = numeric.lowest(1);
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(source.getValue(i), lowest.getValue(i));
        }
    }

    private static void lowest_on_derived_indicator_calculatesCorrectWindow() {
        BarSeries s = buildSeries(5, 1, 4, 9, 2);
        ClosePriceIndicator source = new ClosePriceIndicator(s);
        NumericIndicator derived = NumericIndicator.of(source).plus(2);
        NumericIndicator lowest = derived.lowest(3);
        Num[] expected = {
                DecimalNum.valueOf(7), DecimalNum.valueOf(3),
                DecimalNum.valueOf(3), DecimalNum.valueOf(3),
                DecimalNum.valueOf(3)
        };
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(expected[i], lowest.getValue(i));
        }
    }

    /* --------------------------------------------------------------------- */
    /*                     Main entry point                                   */
    /* --------------------------------------------------------------------- */
    public static void main(String[] args) {
        setupBeforeAll();
        try {
            lowestCreatesCorrectDelegateAndValues();
            lowestWithBarCountGreaterThanSeriesLength();
            lowest_positive_barCount_calculatesCorrectValues();
            lowest_invalid_barCount_throwsIllegalArgumentException();
            lowest_barCount_exceeds_series_usesAvailableValues();
            lowest_barCount_one_mirrorsSourceValues();
            lowest_on_derived_indicator_calculatesCorrectWindow();
            System.out.println("All tests passed.");
        } finally {
            teardownAfterAll();
        }
    }
}
```