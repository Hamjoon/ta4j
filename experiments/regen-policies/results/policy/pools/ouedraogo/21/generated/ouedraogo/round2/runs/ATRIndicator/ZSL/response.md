###Test START##
```java
package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.indicators.MMAIndicator;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link ATRIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>both constructors (Series‑based and TRIndicator‑based)</li>
 *   <li>delegation of {@code getValue}</li>
 *   <li>correct reporting of bar count and unstable bars</li>
 *   <li>accessor for the underlying {@link TRIndicator}</li>
 *   <li>the {@code toString} representation</li>
 *   <li>behaviour on invalid indexes</li>
 * </ul>
 */
public class ATRIndicatorTest {

    private static final int BAR_COUNT = 3;          // time frame used for the ATR
    private BarSeries series;
    private ATRIndicator atrFromSeries;
    private ATRIndicator atrFromTR;

    @Before
    public void setUp() {
        // Build a simple series with deterministic high / low / close values.
        series = new BaseBarSeries();

        // Helper to create a bar at a given index.
        // The series uses the same NumFactory for all numeric values.
        ZonedDateTime time = ZonedDateTime.now();

        // Bar 0
        series.addBar(createBar(time.plusMinutes(0), 10, 8, 9, 9));
        // Bar 1
        series.addBar(createBar(time.plusMinutes(1), 12, 7, 11, 10));
        // Bar 2
        series.addBar(createBar(time.plusMinutes(2), 13, 9, 12, 11));
        // Bar 3
        series.addBar(createBar(time.plusMinutes(3), 15, 10, 14, 13));
        // Bar 4
        series.addBar(createBar(time.plusMinutes(4), 16, 12, 15, 14));

        // Instance built via the (BarSeries, int) constructor
        atrFromSeries = new ATRIndicator(series, BAR_COUNT);

        // Instance built via the (TRIndicator, int) constructor
        TRIndicator tr = new TRIndicator(series);
        atrFromTR = new ATRIndicator(tr, BAR_COUNT);
    }

    /**
     * Creates a {@link BaseBar} with the given parameters.
     */
    private Bar createBar(ZonedDateTime endTime,
                          double high,
                          double low,
                          double open,
                          double close) {
        Num numHigh = series.numFactory().numOf(high);
        Num numLow = series.numFactory().numOf(low);
        Num numOpen = series.numFactory().numOf(open);
        Num numClose = series.numFactory().numOf(close);
        // volume, amount and number of trades are not relevant for the TR/ATR
        Num zero = series.numFactory().numOf(0);
        return new BaseBar(Duration.ofMinutes(1),
                endTime,
                numOpen,
                numHigh,
                numLow,
                numClose,
                zero,   // volume
                zero);  // amount
    }

    /**
     * The ATR value is computed by an {@link MMAIndicator} over the {@link TRIndicator}.
     * We therefore compare the result of {@code ATRIndicator#getValue} with the
     * result of a directly created {@link MMAIndicator} that uses the same TR.
     */
    @Test
    public void testGetValueDelegatesToMMAIndicator() {
        // Direct MMA over the same TRIndicator – this is the expected source of values.
        MMAIndicator expectedMMA = new MMAIndicator(new TRIndicator(series), BAR_COUNT);

        for (int i = 0; i < series.getBarCount(); i++) {
            Num atrValueSeries = atrFromSeries.getValue(i);
            Num atrValueTR = atrFromTR.getValue(i);
            Num expected = expectedMMA.getValue(i);

            // All three values must be equal (within a tiny tolerance)
            assertEquals("ATR (series ctor) value mismatch at index " + i,
                    expected.doubleValue(), atrValueSeries.doubleValue(), 1e-10);
            assertEquals("ATR (TR ctor) value mismatch at index " + i,
                    expected.doubleValue(), atrValueTR.doubleValue(), 1e-10);
        }
    }

    @Test
    public void testGetBarCount() {
        assertEquals("Bar count should match the constructor argument",
                BAR_COUNT, atrFromSeries.getBarCount());
        assertEquals("Bar count should match the constructor argument (TR ctor)",
                BAR_COUNT, atrFromTR.getBarCount());
    }

    @Test
    public void testGetUnstableBars() {
        // According to the implementation, unstable bars == bar count
        assertEquals("Unstable bars must be equal to bar count",
                BAR_COUNT, atrFromSeries.getUnstableBars());
        assertEquals("Unstable bars must be equal to bar count (TR ctor)",
                BAR_COUNT, atrFromTR.getUnstableBars());
    }

    @Test
    public void testGetTRIndicator() {
        TRIndicator trFromSeries = atrFromSeries.getTRIndicator();
        TRIndicator trFromTR = atrFromTR.getTRIndicator();

        // The TRIndicator inside the series‑based ATR must be built from the same series.
        assertNotNull(trFromSeries);
        assertEquals(series, trFromSeries.getBarSeries());

        // The TRIndicator inside the TR‑based ATR must be the exact instance we passed.
        assertNotNull(trFromTR);
        assertSame("TRIndicator reference should be the one supplied to the constructor",
                new TRIndicator(series), trFromTR);
    }

    @Test
    public void testToStringContainsBarCount() {
        String s1 = atrFromSeries.toString();
        String s2 = atrFromTR.toString();

        String expected = "ATRIndicator barCount: " + BAR_COUNT;
        assertEquals("toString output mismatch (series ctor)", expected, s1);
        assertEquals("toString output mismatch (TR ctor)", expected, s2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueWithNegativeIndexThrows() {
        atrFromSeries.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueBeyondSeriesLengthThrows() {
        int outOfBounds = series.getBarCount(); // valid indices are 0..size-1
        atrFromSeries.getValue(outOfBounds);
    }

    /**
     * Additional sanity test: when the bar count is larger than the series size,
     * the indicator should still be constructible and return values for the
     * existing indexes (the internal MMAIndicator will handle the warm‑up period).
     */
    @Test
    public void testLargeBarCountDoesNotFailConstruction() {
        int hugeBarCount = 100; // far larger than the series (5 bars)
        ATRIndicator largeBarCountAtr = new ATRIndicator(series, hugeBarCount);
        // The indicator must be able to return a value for the last index.
        Num value = largeBarCountAtr.getValue(series.getBarCount() - 1);
        assertNotNull("ATR value should be non‑null even with huge bar count", value);
    }
}
```
###Test END##