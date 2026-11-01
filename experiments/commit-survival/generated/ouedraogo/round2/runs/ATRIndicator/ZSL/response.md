###Test START##
```java
package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>Both public constructors.</li>
 *   <li>Delegation of {@code getValue(int)} to the internal {@link MMAIndicator}.</li>
 *   <li>Correct reporting of {@code getBarCount()} and {@code getUnstableBars()}.</li>
 *   <li>Accessors {@code getTRIndicator()} and {@code toString()}.</li>
 * </ul>
 *
 * <p>Real {@link BarSeries} and {@link org.ta4j.core.Bar} objects are used
 * (via {@link BaseBarSeriesBuilder} / {@link BaseBar}) – no mocking or
 * subclassing of library interfaces is performed, complying with the
 * given constraints.
 */
public class ATRIndicatorTest {

    private static final int BAR_COUNT = 3;      // the ATR time‑frame used in the tests
    private BarSeries series;                    // the series on which the indicators operate

    /**
     * Builds a small deterministic series (5 bars) that yields
     * reproducible {@link TRIndicator} values.
     *
     * <p>Each bar i (0‑based) has:
     * <ul>
     *   <li>open  = 100 + i</li>
     *   <li>high  = 105 + i</li>
     *   <li>low   =  95 + i</li>
     *   <li>close = 100 + i</li>
     *   <li>volume = 1000</li>
     * </ul>
     */
    @Before
    public void setUp() {
        series = new BaseBarSeriesBuilder()
                .withName("test-series")
                .build();

        ZonedDateTime start = ZonedDateTime.now(ZoneId.of("UTC")).minusDays(5);
        for (int i = 0; i < 5; i++) {
            ZonedDateTime end = start.plusDays(i + 1);
            Num open   = series.numOf(100 + i);
            Num high   = series.numOf(105 + i);
            Num low    = series.numOf(95  + i);
            Num close  = series.numOf(100 + i);
            Num volume = series.numOf(1_000);
            BaseBar bar = new BaseBar(Duration.ofDays(1), end, open, high, low, close, volume);
            series.addBar(bar);
        }
    }

    /**
     * Helper that builds an {@link MMAIndicator} that uses the same
     * {@link TRIndicator} as the {@link ATRIndicator} under test.
     */
    private MMAIndicator expectedMMA() {
        TRIndicator tr = new TRIndicator(series);
        return new MMAIndicator(tr, BAR_COUNT);
    }

    /* --------------------------------------------------------------
     *  Constructor tests
     * -------------------------------------------------------------- */

    @Test
    public void testConstructorWithSeries() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        // the internal TRIndicator must be instantiated with the same series
        assertNotNull("TRIndicator should not be null", atr.getTRIndicator());
        assertSame("TRIndicator must be built on the supplied series",
                series, atr.getTRIndicator().getBarSeries());
    }

    @Test
    public void testConstructorWithTRIndicator() {
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, BAR_COUNT);
        // the accessor must return the exact instance we supplied
        assertSame("getTRIndicator() must return the TRIndicator passed to the constructor",
                tr, atr.getTRIndicator());
    }

    /* --------------------------------------------------------------
     *  Core functional tests
     * -------------------------------------------------------------- */

    @Test
    public void testGetValueDelegatesToMMAIndicator() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        MMAIndicator expected = expectedMMA();

        // Values are only defined from (barCount‑1) onward because of the
        // moving‑average window.  We compare every index that the ATR can
        // actually produce.
        for (int i = BAR_COUNT - 1; i < series.getBarCount(); i++) {
            Num atrValue = atr.getValue(i);
            Num mmaValue = expected.getValue(i);
            assertTrue(
                    String.format("ATR value at index %d differs from expected MMA value", i),
                    atrValue.isEqual(mmaValue));
        }
    }

    @Test
    public void testGetBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertEquals("ATRIndicator#getBarCount must return the barCount supplied to the constructor",
                BAR_COUNT, atr.getBarCount());
    }

    @Test
    public void testGetUnstableBars() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        // By definition ATRIndicator reports its own barCount as unstable bars
        assertEquals("ATRIndicator#getUnstableBars must equal its barCount",
                BAR_COUNT, atr.getUnstableBars());
    }

    /* --------------------------------------------------------------
     *  toString test
     * -------------------------------------------------------------- */

    @Test
    public void testToStringContainsClassNameAndBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        String s = atr.toString();
        assertTrue("toString should contain the simple class name",
                s.contains(ATRIndicator.class.getSimpleName()));
        assertTrue("toString should contain the configured barCount",
                s.contains("barCount: " + BAR_COUNT));
    }

    /* --------------------------------------------------------------
     *  Defensive test – index out of range
     * -------------------------------------------------------------- */

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueWithNegativeIndexThrowsException() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        // The underlying CachedIndicator (parent of TRIndicator) throws
        // IndexOutOfBoundsException for negative indexes – we verify the same
        // behaviour propagates.
        atr.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueBeyondSeriesLengthThrowsException() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        // series has 5 bars, valid indices are 0‑4
        atr.getValue(series.getBarCount()); // should be out of range
    }
}
```
###Test END##