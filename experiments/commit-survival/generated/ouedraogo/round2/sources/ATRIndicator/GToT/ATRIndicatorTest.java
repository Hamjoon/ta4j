/*
 * JUnit 4 test suite for {@link org.ta4j.core.indicators.ATRIndicator}.
 *
 * The tests cover:
 * 1. Public constructors.
 * 2. Public methods: getValue, getUnstableBars, getTRIndicator,
 *    getBarCount, toString.
 * 3. Edge‑case handling (negative/zero barCount, invalid indexes).
 *
 * NOTE: The test uses the concrete implementation {@code BaseBarSeries}
 *       (part of the ta4j library) to create a real {@link BarSeries}
 *       instance. No mocking or subclassing of library interfaces is performed.
 */

package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.indicators.MMAIndicator;

/**
 * Test class for {@link ATRIndicator}.
 */
public class ATRIndicatorTest {

    private static final ZoneId ZONE = ZoneId.of("UTC");
    private BarSeries series;
    private final int BAR_COUNT = 3; // small period to keep calculations easy

    /**
     * Helper method to create a bar with the given OHLC values.
     */
    private void addBar(String time, double open, double high, double low, double close) {
        ZonedDateTime endTime = ZonedDateTime.parse(time);
        series.addBar(endTime,
                series.numOf(open),
                series.numOf(high),
                series.numOf(low),
                series.numOf(close),
                series.numOf(0)); // volume = 0 (not used in ATR calculations)
    }

    @Before
    public void setUp() {
        // Build a deterministic series of 5 bars.
        series = new BaseBarSeries("test-series");

        // Bar 0 – first bar (no previous close, TR = high‑low)
        addBar("2022-01-01T00:00:00Z", 100, 110, 95, 105);
        // Bar 1
        addBar("2022-01-02T00:00:00Z", 105, 112, 102, 108);
        // Bar 2
        addBar("2022-01-03T00:00:00Z", 108, 115, 107, 110);
        // Bar 3
        addBar("2022-01-04T00:00:00Z", 110, 118, 109, 117);
        // Bar 4
        addBar("2022-01-05T00:00:00Z", 117, 120, 115, 119);
    }

    /**
     * -----------------------------------------------------------------------
     * 1. Constructor tests
     * -----------------------------------------------------------------------
     */

    @Test
    public void testConstructorWithSeriesAndBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertNotNull("ATRIndicator instance must not be null", atr);
        assertEquals("Bar count must be stored correctly", BAR_COUNT, atr.getBarCount());
    }

    @Test
    public void testConstructorWithTRIndicatorAndBarCount() {
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, BAR_COUNT);
        assertNotNull("ATRIndicator instance must not be null", atr);
        assertSame("TRIndicator passed to constructor must be returned by getTRIndicator()", tr, atr.getTRIndicator());
        assertEquals("Bar count must be stored correctly", BAR_COUNT, atr.getBarCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroBarCountThrows() {
        // MMAIndicator (used internally) validates barCount > 0 and throws IllegalArgumentException.
        new ATRIndicator(series, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeBarCountThrows() {
        new ATRIndicator(series, -5);
    }

    /**
     * -----------------------------------------------------------------------
     * 2. getValue() tests – compare with a manually created MMAIndicator
     * -----------------------------------------------------------------------
     */

    @Test
    public void testGetValueMatchesMMAIndicator() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        TRIndicator tr = new TRIndicator(series);
        MMAIndicator expectedMMA = new MMAIndicator(tr, BAR_COUNT);

        // Verify each index returns the same Num instance (value equality)
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            Num atrValue = atr.getValue(i);
            Num mmaValue = expectedMMA.getValue(i);
            assertEquals("ATR value must equal MMA value at index " + i, mmaValue, atrValue);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueWithNegativeIndexThrows() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        atr.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueWithIndexBeyondSeriesThrows() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        atr.getValue(series.getBarCount()); // one past the last valid index
    }

    /**
     * -----------------------------------------------------------------------
     * 3. getUnstableBars() – should return the same value as barCount
     * -----------------------------------------------------------------------
     */

    @Test
    public void testGetUnstableBarsEqualsBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertEquals("Unstable bars must equal the bar count used in construction",
                BAR_COUNT, atr.getUnstableBars());
    }

    /**
     * -----------------------------------------------------------------------
     * 4. getBarCount() – simple getter test
     * -----------------------------------------------------------------------
     */

    @Test
    public void testGetBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertEquals("Getter must return the bar count supplied at construction",
                BAR_COUNT, atr.getBarCount());
    }

    /**
     * -----------------------------------------------------------------------
     * 5. getTRIndicator() – returns the TRIndicator used internally
     * -----------------------------------------------------------------------
     */

    @Test
    public void testGetTRIndicator() {
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, BAR_COUNT);
        assertSame("getTRIndicator must return the same instance passed to the constructor",
                tr, atr.getTRIndicator());
    }

    /**
     * -----------------------------------------------------------------------
     * 6. toString() – must contain class name and barCount
     * -----------------------------------------------------------------------
     */

    @Test
    public void testToStringContainsClassNameAndBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        String str = atr.toString();
        assertTrue("toString must contain the simple class name",
                str.contains(ATRIndicator.class.getSimpleName()));
        assertTrue("toString must contain the bar count",
                str.contains("barCount: " + BAR_COUNT));
    }
}
