###Test START##
```java
/**
 * JUnit‑4 test suite for {@link org.ta4j.core.indicators.ATRIndicator}.
 *
 * The tests cover:
 *  <ul>
 *      <li>All public methods and their basic behaviour</li>
 *      <li>Edge‑case handling (null arguments, illegal barCount, out‑of‑range index)</li>
 *      <li>Exception scenarios that the implementation may raise</li>
 *  </ul>
 *
 * The test uses only the public API that is listed in the task description.
 * The concrete {@code BarSeries} implementation used is {@code BaseBarSeries},
 * which is the standard implementation shipped with ta4j.
 */
package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.Num;

/**
 * Test class for {@link ATRIndicator}.
 */
public class ATRIndicatorTest {

    /** a small series with deterministic price data */
    private BarSeries series;

    /** number of bars used for the moving‑average (the “barCount”) */
    private static final int BAR_COUNT = 3;

    /** timestamps for the bars – the actual value is irrelevant for the indicator */
    private static final ZonedDateTime START_TIME = ZonedDateTime.of(2023, 1, 1,
            0, 0, 0, 0, ZoneId.of("UTC"));

    @Before
    public void setUp() {
        series = new BaseBarSeries("test-series");
        // create 6 bars with simple increasing prices
        // open, high, low, close, volume (volume is not used by TRIndicator)
        for (int i = 0; i < 6; i++) {
            ZonedDateTime time = START_TIME.plusDays(i);
            // price values are chosen so that true range is easy to compute:
            // high = close + 2, low = close - 2, open = close
            double close = 100 + i * 10;               // 100,110,…,150
            double high = close + 2;
            double low = close - 2;
            double open = close;
            series.addBar(time, open, high, low, close, 0);
        }
    }

    /** -----------------------------------------------------------------------
     *  1.  PUBLIC METHOD LIST
     *  -----------------------------------------------------------------------
     *  - ATRIndicator(BarSeries series, int barCount)
     *  - ATRIndicator(TRIndicator tr, int barCount)
     *  - Num getValue(int index)
     *  - int getUnstableBars()
     *  - TRIndicator getTRIndicator()
     *  - int getBarCount()
     *  - String toString()
     *  -----------------------------------------------------------------------
     */

    /** -----------------------------------------------------------------------
     *  2.  BASIC FUNCTIONAL TESTS
     *  -----------------------------------------------------------------------
     */

    @Test
    public void testConstructorWithSeriesAndBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertNotNull("ATRIndicator must be created", atr);
        assertEquals("Bar count must be stored correctly", BAR_COUNT, atr.getBarCount());
        assertNotNull("TRIndicator must be initialised", atr.getTRIndicator());
        assertEquals("Unstable bars must equal the bar count", BAR_COUNT, atr.getUnstableBars());
    }

    @Test
    public void testConstructorWithTRIndicator() {
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, BAR_COUNT);
        assertNotNull(atr);
        assertSame("TRIndicator supplied must be returned by getter", tr, atr.getTRIndicator());
        assertEquals(BAR_COUNT, atr.getBarCount());
    }

    @Test
    public void testGetValueMatchesMMAIndicator() {
        // Build ATRIndicator using the TRIndicator constructor
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, BAR_COUNT);

        // Build a stand‑alone MMAIndicator with the same TRIndicator and barCount
        MMAIndicator mma = new MMAIndicator(tr, BAR_COUNT);

        // Compare the values for every valid index
        for (int i = 0; i < series.getBarCount(); i++) {
            Num atrValue = atr.getValue(i);
            Num mmaValue = mma.getValue(i);
            assertEquals("ATR value must equal MMA value at index " + i, mmaValue, atrValue);
        }
    }

    @Test
    public void testToStringContainsBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        String s = atr.toString();
        assertTrue("toString must contain class name", s.contains("ATRIndicator"));
        assertTrue("toString must contain barCount", s.contains("barCount: " + BAR_COUNT));
    }

    /** -----------------------------------------------------------------------
     *  3.  EDGE‑CASE & EXCEPTION TESTS
     *  -----------------------------------------------------------------------
     */

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullSeriesThrowsNPE() {
        new ATRIndicator((BarSeries) null, BAR_COUNT);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullTRIndicatorThrowsNPE() {
        new ATRIndicator((TRIndicator) null, BAR_COUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroBarCountThrowsException() {
        new ATRIndicator(series, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeBarCountThrowsException() {
        new ATRIndicator(series, -5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueWithNegativeIndexThrowsException() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        atr.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueWithIndexBeyondSeriesThrowsException() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        atr.getValue(series.getBarCount()); // valid indices are 0 .. size‑1
    }

    /** -----------------------------------------------------------------------
     *  4.  COMBINED TEST CASE (typical usage scenario)
     *  -----------------------------------------------------------------------
     */
    @Test
    public void testTypicalUsageScenario() {
        // Build the indicator
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);

        // Verify that the first BAR_COUNT‑1 values are equal to zero,
        // because the underlying MMAIndicator (EMA‑type) returns zero until it
        // has enough data points.
        for (int i = 0; i < BAR_COUNT - 1; i++) {
            assertTrue("Values before enough data should be zero",
                    atr.getValue(i).isZero());
        }

        // From index BAR_COUNT‑1 onward we expect a positive value.
        for (int i = BAR_COUNT - 1; i < series.getBarCount(); i++) {
            Num value = atr.getValue(i);
            assertFalse("ATR should be positive after enough data", value.isZero());
            assertTrue("ATR must be non‑negative", value.isPositiveOrZero());
        }
    }
}
```
###Test END##