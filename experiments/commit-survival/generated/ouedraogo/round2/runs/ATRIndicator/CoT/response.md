###Test START##
```java
package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link ATRIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>Public constructors</li>
 *   <li>Public methods – normal behaviour</li>
 *   <li>Edge‑case handling (null arguments, negative/zero barCount)</li>
 * </ul>
 *
 * The library provides concrete implementations of {@link BarSeries} and {@link Num}
 * (e.g. {@code BaseBarSeries} and {@code DecimalNum}). Those implementations are used
 * to build a minimal series with a few bars so that the indicator can be instantiated
 * without triggering internal calculation errors.
 */
public class ATRIndicatorTest {

    /** Helper to create a very small BarSeries (2 bars) with deterministic values. */
    private BarSeries createSimpleSeries() {
        // BaseBarSeries is the standard concrete implementation of BarSeries in ta4j.
        // It provides a default constructor that takes a name.
        BarSeries series = new org.ta4j.core.BaseBarSeries("test-series");

        // Add two bars – the values themselves are not important for the tests that
        // do not invoke getValue().
        ZonedDateTime now = ZonedDateTime.now();
        series.addBar(now.minusMinutes(2), 10, 12, 9, 11, 1000);
        series.addBar(now.minusMinutes(1), 11, 13, 10, 12, 1100);
        return series;
    }

    /**--------------------------------------------------------------
     *  1.  Public constructors
     *---------------------------------------------------------------*/

    @Test
    public void testConstructorWithSeriesAndBarCount() {
        BarSeries series = createSimpleSeries();
        int barCount = 2;
        ATRIndicator atr = new ATRIndicator(series, barCount);

        // Basic sanity checks
        assertNotNull("TRIndicator must be created", atr.getTRIndicator());
        assertEquals("Bar count must be stored correctly", barCount, atr.getBarCount());
        assertEquals("Unstable bars should equal bar count", barCount, atr.getUnstableBars());
        assertTrue("toString should contain class name and barCount",
                atr.toString().contains("ATRIndicator") && atr.toString().contains(String.valueOf(barCount)));
    }

    @Test
    public void testConstructorWithTRIndicatorAndBarCount() {
        BarSeries series = createSimpleSeries();
        TRIndicator tr = new TRIndicator(series);
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(tr, barCount);

        // The TRIndicator passed to the constructor must be returned by getter
        assertSame("getTRIndicator should return the same instance passed to the ctor", tr, atr.getTRIndicator());
        assertEquals("Bar count must be stored correctly", barCount, atr.getBarCount());
    }

    /**--------------------------------------------------------------
     *  2.  Normal method behaviour
     *---------------------------------------------------------------*/

    @Test
    public void testGetValueDelegatesToMMAIndicator() {
        BarSeries series = createSimpleSeries();
        int barCount = 2;
        ATRIndicator atr = new ATRIndicator(series, barCount);

        // Since the underlying MMAIndicator uses a moving‑average, the value at
        // index 0 should be equal to the TR value at index 0.
        Num expected = atr.getTRIndicator().getValue(0);
        Num actual = atr.getValue(0);
        assertEquals("ATR value at index 0 must equal the underlying TR value", expected, actual);
    }

    @Test
    public void testGetBarCountAndUnstableBarsConsistency() {
        BarSeries series = createSimpleSeries();
        int barCount = 5;
        ATRIndicator atr = new ATRIndicator(series, barCount);

        assertEquals("getBarCount should return the barCount supplied to ctor", barCount, atr.getBarCount());
        assertEquals("getUnstableBars should return the same value as getBarCount", barCount, atr.getUnstableBars());
    }

    @Test
    public void testToStringFormat() {
        BarSeries series = createSimpleSeries();
        int barCount = 4;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        String s = atr.toString();

        assertTrue("toString must contain class simple name", s.contains("ATRIndicator"));
        assertTrue("toString must contain the barCount", s.contains(String.valueOf(barCount)));
    }

    /**--------------------------------------------------------------
     *  3.  Edge‑case and exception handling
     *---------------------------------------------------------------*/

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullSeriesThrowsException() {
        // The first constructor should reject a null BarSeries – the underlying
        // TRIndicator constructor will throw NPE.
        new ATRIndicator((BarSeries) null, 2);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullTRIndicatorThrowsException() {
        // Passing a null TRIndicator must result in NPE when the constructor tries
        // to access tr.getBarSeries().
        new ATRIndicator((TRIndicator) null, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeBarCountThrowsException() {
        BarSeries series = createSimpleSeries();
        // Negative barCount is not meaningful for a moving average and the
        // underlying MMAIndicator validates the argument.
        new ATRIndicator(series, -1);
    }

    @Test
    public void testConstructorWithZeroBarCountBehaviour() {
        BarSeries series = createSimpleSeries();
        // Zero is allowed by MMAIndicator (it creates an indicator that always
        // returns zero). The test verifies that no exception is thrown and that
        // the internal state reflects the requested barCount.
        ATRIndicator atr = new ATRIndicator(series, 0);
        assertEquals("Bar count should be 0", 0, atr.getBarCount());
        assertEquals("Unstable bars should also be 0", 0, atr.getUnstableBars());

        // The value of an ATR with zero period is defined as zero by the MMAIndicator.
        Num zero = series.num().zero();
        assertEquals("ATR value with zero period should be zero", zero, atr.getValue(0));
    }

    /**--------------------------------------------------------------
     *  4.  Additional sanity checks for getValue out‑of‑bounds indices
     *---------------------------------------------------------------*/

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueWithNegativeIndexThrowsException() {
        BarSeries series = createSimpleSeries();
        ATRIndicator atr = new ATRIndicator(series, 2);
        atr.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueWithIndexBeyondSeriesThrowsException() {
        BarSeries series = createSimpleSeries();
        ATRIndicator atr = new ATRIndicator(series, 2);
        // series has only 2 bars (indices 0 and 1); index 2 is out of range.
        atr.getValue(2);
    }
}
```
###Test END##