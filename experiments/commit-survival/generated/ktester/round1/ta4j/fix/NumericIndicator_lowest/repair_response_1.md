```java
package org.ta4j.core.indicators.numeric;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#lowest(int)}.
 *
 * The original test class used JUnit‑5 and some helper classes that are not
 * available in the current build environment.  It has been rewritten to use
 * only JUnit‑4 and the core TA4J API, keeping the original test logic intact.
 */
public class NumericIndicator_lowest_Test {

    /**
     * Function used by {@link org.ta4j.core.BaseBarSeriesBuilder} to create {@link Num}
     * instances.
     */
    private static Function<Number, Num> numFunction;

    /**
     * Series used by the tests.
     */
    private static BarSeries series;

    /**
     * Close‑price indicator based on {@link #series}.
     */
    private static ClosePriceIndicator closePrice;

    @BeforeClass
    public static void setupBeforeAll() {
        // DecimalNum is the default Num implementation in ta4j.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());
        // A simple series with positive, zero and negative values.
        series = buildSeries(1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
    }

    @AfterClass
    public static void teardownAfterAll() {
        series = null;
        closePrice = null;
        numFunction = null;
    }

    /**
     * Build a simple {@link BarSeries} from the supplied double values.
     */
    private static BarSeries buildSeries(double... values) {
        // Use the builder that ships with ta4j
        BarSeries s = new org.ta4j.core.BaseBarSeriesBuilder()
                .withName("test")
                .build();

        ZonedDateTime time = ZonedDateTime.now();
        Duration barDuration = Duration.ofMinutes(1);

        for (double v : values) {
            Num n = numFunction.apply(v);
            // For simplicity all price fields are the same and volume is zero
            org.ta4j.core.BaseBar bar = new org.ta4j.core.BaseBar(
                    barDuration,
                    time,
                    n, // open
                    n, // high
                    n, // low
                    n, // close
                    n, // volume
                    DecimalNum.ZERO // amount (price * volume, not used here)
            );
            s.addBar(bar);
            time = time.plusMinutes(1);
        }
        return s;
    }

    @Test
    public void lowestCreatesCorrectDelegateAndValues() {
        // GIVEN a NumericIndicator wrapping the close‑price indicator
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        // WHEN we ask for the lowest value over a window of 3 bars
        int barCount = 3;
        NumericIndicator lowest = numeric.lowest(barCount);
        // THEN the underlying delegate must be a LowestValueIndicator
        Indicator<Num> delegate = lowest.delegate();
        assertTrue(delegate instanceof LowestValueIndicator);
        // AND the computed values must match those of a direct LowestValueIndicator
        LowestValueIndicator expected = new LowestValueIndicator(closePrice, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(expected.getValue(i), lowest.getValue(i));
        }
    }

    @Test
    public void lowestWithBarCountGreaterThanSeriesLength() {
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

    @Test
    public void lowest_positive_barCount_calculatesCorrectValues() {
        BarSeries s = buildSeries(10, 8, 12, 5, 7);
        ClosePriceIndicator source = new ClosePriceIndicator(s);
        NumericIndicator numeric = NumericIndicator.of(source);
        NumericIndicator lowest = numeric.lowest(3);
        Indicator<Num> delegate = lowest.delegate();
        assertTrue(delegate instanceof LowestValueIndicator);
        Num[] expected = {
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(8),
                DecimalNum.valueOf(8),
                DecimalNum.valueOf(5),
                DecimalNum.valueOf(5)
        };
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(expected[i], lowest.getValue(i));
        }
    }

    @Test
    public void lowest_invalid_barCount_throwsIllegalArgumentException() {
        int[] invalidValues = {0, -5};
        for (int barCount : invalidValues) {
            BarSeries s = buildSeries(1, 2, 3);
            NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
            try {
                numeric.lowest(barCount);
                fail("Expected IllegalArgumentException for barCount = " + barCount);
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    @Test
    public void lowest_barCount_exceeds_series_usesAvailableValues() {
        BarSeries s = buildSeries(4, 9, 2, 7, 5);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
        NumericIndicator lowest = numeric.lowest(10);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        Num[] expected = {
                DecimalNum.valueOf(4),
                DecimalNum.valueOf(4),
                DecimalNum.valueOf(2),
                DecimalNum.valueOf(2),
                DecimalNum.valueOf(2)
        };
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(expected[i], lowest.getValue(i));
        }
    }

    @Test
    public void lowest_barCount_one_mirrorsSourceValues() {
        BarSeries s = buildSeries(3, 6, 1, 8);
        ClosePriceIndicator source = new ClosePriceIndicator(s);
        NumericIndicator numeric = NumericIndicator.of(source);
        NumericIndicator lowest = numeric.lowest(1);
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(source.getValue(i), lowest.getValue(i));
        }
    }

    @Test
    public void lowest_on_derived_indicator_calculatesCorrectWindow() {
        BarSeries s = buildSeries(5, 1, 4, 9, 2);
        ClosePriceIndicator source = new ClosePriceIndicator(s);
        NumericIndicator derived = NumericIndicator.of(source).plus(2);
        NumericIndicator lowest = derived.lowest(3);
        Num[] expected = {
                DecimalNum.valueOf(7),
                DecimalNum.valueOf(3),
                DecimalNum.valueOf(3),
                DecimalNum.valueOf(3),
                DecimalNum.valueOf(3)
        };
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(expected[i], lowest.getValue(i));
        }
    }

    @Test
    public void lowest_invalid_barCount_zero_throwsIllegalArgumentException() {
        BarSeries s = buildSeries(1, 2, 3);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
        try {
            numeric.lowest(0);
            fail("Expected IllegalArgumentException for barCount = 0");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void lowest_invalid_barCount_negative_throwsIllegalArgumentException() {
        BarSeries s = buildSeries(1, 2, 3);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
        try {
            numeric.lowest(-5);
            fail("Expected IllegalArgumentException for negative barCount");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
```