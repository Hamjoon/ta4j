###Test START##
```java
/**
 * JUnit‑4 test suite for {@link org.ta4j.core.indicators.ATRIndicator}.
 *
 * The tests cover:
 *  • All public constructors and methods.
 *  • Normal behaviour (basic happy‑path usage).
 *  • Edge cases such as invalid indexes and minimal barCount values.
 *
 * The test uses only the public API that is part of the TA‑4J library
 * (BarSeries, BaseBar, BaseBarSeries, DecimalNum, TRIndicator, MMAIndicator,
 *  and the ATRIndicator itself). No mocking frameworks are used and no
 *  subclassing of library abstract classes/interfaces is performed.
 */
package org.ta4j.core.indicators;

import org.junit.Before;
import org.junit.Test;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class ATRIndicatorTest {

    /** A small bar series (5 candles) used for all tests. */
    private BarSeries series;

    /** The ATR indicator under test (barCount = 3). */
    private ATRIndicator atr;

    /** Expected barCount used throughout the tests. */
    private static final int BAR_COUNT = 3;

    /**
     * Build a simple {@link BarSeries} containing five deterministic bars.
     *
     * <p>All bars are built with the same time‑step (1 day) and use {@link DecimalNum}
     * for the price/volume values. The numbers are chosen so that the underlying
     * {@link TRIndicator} returns non‑zero values, which in turn makes the
     * {@link MMAIndicator} (used inside {@code ATRIndicator}) produce a meaningful
     * result.</p>
     */
    @Before
    public void setUp() {
        series = new BaseBarSeries();

        // Helper to create a bar with given OHLC values
        // (open, high, low, close, volume)
        ZonedDateTime now = ZonedDateTime.now();

        series.addBar(new BaseBar(Duration.ofDays(1), now,
                DecimalNum.valueOf(10), DecimalNum.valueOf(12),
                DecimalNum.valueOf(9), DecimalNum.valueOf(11),
                DecimalNum.valueOf(1000)));

        series.addBar(new BaseBar(Duration.ofDays(1), now.plusDays(1),
                DecimalNum.valueOf(11), DecimalNum.valueOf(13),
                DecimalNum.valueOf(10), DecimalNum.valueOf(12),
                DecimalNum.valueOf(1100)));

        series.addBar(new BaseBar(Duration.ofDays(1), now.plusDays(2),
                DecimalNum.valueOf(12), DecimalNum.valueOf(14),
                DecimalNum.valueOf(11), DecimalNum.valueOf(13),
                DecimalNum.valueOf(1200)));

        series.addBar(new BaseBar(Duration.ofDays(1), now.plusDays(3),
                DecimalNum.valueOf(13), DecimalNum.valueOf(15),
                DecimalNum.valueOf(12), DecimalNum.valueOf(14),
                DecimalNum.valueOf(1300)));

        series.addBar(new BaseBar(Duration.ofDays(1), now.plusDays(4),
                DecimalNum.valueOf(14), DecimalNum.valueOf(16),
                DecimalNum.valueOf(13), DecimalNum.valueOf(15),
                DecimalNum.valueOf(1400)));

        // ATR with a 3‑period average true range
        atr = new ATRIndicator(series, BAR_COUNT);
    }

    /* --------------------------------------------------------------------- *
     *  PUBLIC CONSTRUCTORS
     * --------------------------------------------------------------------- */

    @Test
    public void constructorWithSeriesAndBarCount_createsIndicator() {
        ATRIndicator indicator = new ATRIndicator(series, BAR_COUNT);
        assertNotNull("ATRIndicator instance must not be null", indicator);
        assertEquals("Bar count must be stored correctly", BAR_COUNT, indicator.getBarCount());
    }

    @Test
    public void constructorWithTRIndicatorAndBarCount_createsIndicator() {
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator indicator = new ATRIndicator(tr, BAR_COUNT);
        assertNotNull("ATRIndicator instance must not be null", indicator);
        assertEquals("Bar count must be stored correctly", BAR_COUNT, indicator.getBarCount());
        assertSame("TRIndicator should be the one supplied to the constructor",
                tr, indicator.getTRIndicator());
    }

    /* --------------------------------------------------------------------- *
     *  BASIC FUNCTIONALITY
     * --------------------------------------------------------------------- */

    @Test
    public void getValue_returnsNonNullNumWithinSeriesRange() {
        // Test the whole valid index range of the series
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            Num value = atr.getValue(i);
            assertNotNull("ATR value at index " + i + " must not be null", value);
        }
    }

    @Test
    public void getUnstableBars_returnsBarCount() {
        assertEquals("Unstable bars must equal the configured barCount",
                BAR_COUNT, atr.getUnstableBars());
    }

    @Test
    public void getTRIndicator_returnsUnderlyingTRIndicator() {
        TRIndicator tr = atr.getTRIndicator();
        assertNotNull("TRIndicator must be non‑null", tr);
        // The TRIndicator must be linked to the same series as the ATRIndicator
        assertSame("TRIndicator's series must be the same as the ATR series",
                series, tr.getBarSeries());
    }

    @Test
    public void getBarCount_returnsConfiguredBarCount() {
        assertEquals("getBarCount() must return the barCount supplied to the constructor",
                BAR_COUNT, atr.getBarCount());
    }

    @Test
    public void toString_containsClassNameAndBarCount() {
        String txt = atr.toString();
        assertTrue("toString() must contain the simple class name",
                txt.contains("ATRIndicator"));
        assertTrue("toString() must contain the barCount value",
                txt.contains(String.valueOf(BAR_COUNT)));
    }

    /* --------------------------------------------------------------------- *
     *  EDGE CASES & EXCEPTION HANDLING
     * --------------------------------------------------------------------- */

    @Test(expected = IndexOutOfBoundsException.class)
    public void getValue_negativeIndex_throwsException() {
        atr.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getValue_indexBeyondSeries_throwsException() {
        atr.getValue(series.getBarCount()); // one past the last valid index
    }

    @Test
    public void constructorWithBarCountOne_works() {
        // A barCount of 1 should be accepted (it reduces to a simple moving average of TR)
        ATRIndicator oneBar = new ATRIndicator(series, 1);
        assertEquals(1, oneBar.getBarCount());

        // The first calculable value should be equal to the underlying TR value
        Num expected = oneBar.getTRIndicator().getValue(series.getBeginIndex());
        Num actual = oneBar.getValue(series.getBeginIndex());
        assertEquals("With barCount=1, ATR should equal TR at the same index", expected, actual);
    }

    @Test
    public void constructorWithZeroBarCount_behaviour() {
        // The library does not explicitly forbid a barCount of 0, but MMAIndicator
        // would treat it as a division by zero. We verify that an IllegalArgumentException
        // is thrown (the actual implementation of MMAIndicator does this).
        try {
            new ATRIndicator(series, 0);
            fail("Expected IllegalArgumentException when barCount is zero");
        } catch (IllegalArgumentException e) {
            // Expected path – the message is not asserted because it may differ across versions
        }
    }

    /* --------------------------------------------------------------------- *
     *  CONSISTENCY BETWEEN TWO CONSTRUCTORS
     * --------------------------------------------------------------------- */

    @Test
    public void bothConstructorsProduceIdenticalResults() {
        // Build an ATR using the series‑based constructor
        ATRIndicator viaSeries = new ATRIndicator(series, BAR_COUNT);
        // Build an ATR using the TRIndicator‑based constructor
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator viaTR = new ATRIndicator(tr, BAR_COUNT);

        // Compare the values for the whole valid index range
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            Num v1 = viaSeries.getValue(i);
            Num v2 = viaTR.getValue(i);
            assertEquals("ATR values must be identical for both constructors at index " + i,
                    v1, v2);
        }
    }
}
```
###Test END##