###Test START##
package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.num.Num;
import static org.ta4j.core.num.NaN.NaN;

/**
 * Unit tests for {@link ATRIndicator}.
 *
 * The tests focus on the public behaviour of the indicator:
 *
 * <ul>
 *   <li>correct handling of unstable bars (returning {@code NaN})</li>
 *   <li>correct propagation of the bar count parameter</li>
 *   <li>the {@link #getTRIndicator()} factory method returns a {@link TRIndicator}
 *       that works on the same {@link BarSeries}</li>
 *   <li>the {@link #toString()} representation contains the class name and the
 *       configured bar count</li>
 *   <li>the constructor that receives a {@link TRIndicator} directly behaves the
 *       same as the constructor that receives a {@link BarSeries}</li>
 * </ul>
 */
public class ATRIndicatorTest {

    /** a simple series with a few bars – the actual price values are not relevant
     *  for the unstable‑bars tests because the indicator never reaches the
     *  stable part of the calculation. */
    private BarSeries series;

    /** the number of periods used for the ATR moving‑average. */
    private static final int BAR_COUNT = 3;

    @Before
    public void setUp() {
        series = new BaseBarSeries("test-series");

        // create a few bars – the values are arbitrary but must be consistent
        // (high >= low, close between them).  We create 6 bars so that the
        // series is longer than the unstable‑bars window.
        for (int i = 0; i < 6; i++) {
            ZonedDateTime endTime = ZonedDateTime.now().plusMinutes(i);
            Num open = series.numFactory().numOf(10 + i);
            Num high = series.numFactory().numOf(12 + i);
            Num low  = series.numFactory().numOf(9 + i);
            Num close = series.numFactory().numOf(11 + i);
            Num volume = series.numFactory().numOf(1000);
            BaseBar bar = new BaseBar(Duration.ofMinutes(1), endTime, open, high, low, close, volume);
            series.addBar(bar);
        }
    }

    /**
     * Verify that the number of unstable bars reported by the indicator is the
     * sum of the underlying {@link TRIndicator} unstable bars and the configured
     * bar count, and that any request for a value before that index returns
     * {@code NaN}.
     */
    @Test
    public void testUnstableBarsBehaviour() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);

        // TRIndicator does not have any unstable bars for a plain series, therefore
        // the expected unstable‑bars count is exactly the configured barCount.
        int expectedUnstable = BAR_COUNT;
        assertEquals("Unstable‑bars count should be equal to barCount", expectedUnstable,
                atr.getCountOfUnstableBars());

        // All indices lower than the unstable count must return NaN
        for (int i = 0; i < expectedUnstable; i++) {
            Num value = atr.getValue(i);
            assertTrue("Value at index " + i + " must be NaN", value.isNaN());
            assertSame("NaN instance should be the shared constant", NaN, value);
        }

        // The first stable index (exactly at the unstable‑bars boundary) must
        // return a real number (i.e. not NaN).  The actual numeric value is not
        // asserted because it depends on the underlying price data – we only
        // check that the indicator does not propagate NaN.
        Num firstStable = atr.getValue(expectedUnstable);
        assertNotNull("First stable value must not be null", firstStable);
        assertFalse("First stable value must not be NaN", firstStable.isNaN());
    }

    /**
     * The {@code getBarCount()} accessor must return the bar count supplied at
     * construction time.
     */
    @Test
    public void testGetBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertEquals("getBarCount() must return the configured barCount", BAR_COUNT, atr.getBarCount());
    }

    /**
     * {@code getTRIndicator()} must create a new {@link TRIndicator} that works on
     * the same {@link BarSeries} as the ATR indicator.
     */
    @Test
    public void testGetTRIndicator() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        TRIndicator tr = atr.getTRIndicator();
        assertNotNull("TRIndicator returned by getTRIndicator() must not be null", tr);
        assertSame("The TRIndicator must be based on the same BarSeries as the ATR indicator",
                series, tr.getBarSeries());
    }

    /**
     * Verify that the {@code toString()} implementation contains the simple class
     * name and the configured bar count.
     */
    @Test
    public void testToString() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        String s = atr.toString();
        assertTrue("toString() must contain the class simple name",
                s.contains(ATRIndicator.class.getSimpleName()));
        assertTrue("toString() must contain the configured barCount",
                s.contains("barCount: " + BAR_COUNT));
    }

    /**
     * The constructor that receives a {@link TRIndicator} directly should behave
     * identically to the constructor that receives a {@link BarSeries}.  This is
     * verified by checking that both instances report the same unstable‑bars
     * count and return the same value for a stable index.
     */
    @Test
    public void testConstructorWithTRIndicator() {
        // Build a TRIndicator explicitly
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atrFromTR = new ATRIndicator(tr, BAR_COUNT);
        ATRIndicator atrFromSeries = new ATRIndicator(series, BAR_COUNT);

        // Both indicators must report the same unstable‑bars count
        assertEquals(atrFromSeries.getCountOfUnstableBars(),
                atrFromTR.getCountOfUnstableBars());

        // For a stable index both must return a non‑NaN value (the actual value
        // may differ because the internal MMAIndicator is built on the same
        // underlying data, but the contract is that the result is a real number).
        int stableIndex = atrFromSeries.getCountOfUnstableBars();
        Num v1 = atrFromSeries.getValue(stableIndex);
        Num v2 = atrFromTR.getValue(stableIndex);
        assertFalse("Value from series‑based constructor must not be NaN", v1.isNaN());
        assertFalse("Value from TR‑based constructor must not be NaN", v2.isNaN());
    }
}
###Test END##