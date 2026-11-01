package org.ta4j.core.indicators;
import org.ta4j.core.indicators.averages.MMAIndicator;
import org.ta4j.core.indicators.helpers.TRIndicator;


import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>Typical usage – ATR with a barCount of 1 (should equal the raw TR value).</li>
 *   <li>Comparison with the underlying {@link MMAIndicator} for a larger barCount.</li>
 *   <li>Correct exposure of configuration values (barCount, unstable bars).</li>
 *   <li>String representation.</li>
 *   <li>Constructor argument validation (null series, negative barCount).</li>
 * </ul>
 *
 * <p>All objects are built using only the public API listed in the
 * problem statement (or concrete implementations that are part of the
 * same library, such as {@code BaseBarSeries} and {@code BaseBar}).
 */
public class ATRIndicatorTest {

    private BarSeries series;
    private static final ZoneId ZONE = ZoneId.of("UTC");

    /**
     * Helper to create a {@link BaseBar} from primitive double values.
     */
    private BaseBar newBar(int index, double open, double high, double low, double close) {
        // each bar spans one day – the exact duration is irrelevant for the indicator logic
        Duration oneDay = Duration.ofDays(1);
        ZonedDateTime endTime = ZonedDateTime.of(2020, 1, 1 + index, 0, 0, 0, 0, ZONE);
        Num openNum = series.numFactory().numOf(open);
        Num highNum = series.numFactory().numOf(high);
        Num lowNum  = series.numFactory().numOf(low);
        Num closeNum = series.numFactory().numOf(close);
        Num volume = series.numFactory().numOf(0); // volume is not used by ATR
        return new BaseBar(oneDay, endTime, openNum, highNum, lowNum, closeNum, volume);
    }

    @Before
    public void setUp() {
        // Build a small deterministic series (3 bars) that will be reused by several tests.
        series = new BaseBarSeries("test-series");
        series.addBar(newBar(0, 10, 12, 9, 11));   // bar 0
        series.addBar(newBar(1, 11, 13, 10, 12));  // bar 1
        series.addBar(newBar(2, 12, 14, 11, 13));  // bar 2
    }

    /**
     * With a barCount of 1 the ATR should be exactly the same as the TR value
     * for the same index.
     */
    @Test
    public void testATREqualsTRWhenBarCountIsOne() {
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, 1);

        // index 1 (second bar) – we can calculate the expected TR manually:
        // high - low = 13 - 10 = 3
        // |high - prevClose| = |13 - 11| = 2
        // |low  - prevClose| = |10 - 11| = 1
        // TR = max(3,2,1) = 3
        Num expectedTR = series.numFactory().numOf(3);
        Num actualTR = tr.getValue(1);
        Num actualATR = atr.getValue(1);

        assertEquals("TR value at index 1 should be 3", 0, expectedTR.compareTo(actualTR));
        assertEquals("ATR (barCount=1) should equal TR at same index", 0,
                expectedTR.compareTo(actualATR));
    }

    /**
     * For a larger barCount the ATR must follow the MMA (modified moving average)
     * of the TR values. This test creates both indicators independently and checks
     * that they return identical results for all valid indexes.
     */
    @Test
    public void testATRMatchesMMAIndicator() {
        final int barCount = 3;
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, barCount);
        MMAIndicator mma = new MMAIndicator(tr, barCount);

        // The first {@code barCount-1} values are considered unstable; after that
        // both indicators should be stable and produce the same numbers.
        for (int i = 0; i < series.getBarCount(); i++) {
            Num atrVal = atr.getValue(i);
            Num mmaVal = mma.getValue(i);
            assertEquals("ATR and MMA must match at index " + i, 0, atrVal.compareTo(mmaVal));
        }
    }

    @Test
    public void testGetBarCountReturnsConstructorValue() {
        final int barCount = 5;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        assertEquals("getBarCount() must return the value supplied to the constructor",
                barCount, atr.getBarCount());
    }

    @Test
    public void testGetCountOfUnstableBarsEqualsBarCount() {
        final int barCount = 4;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        assertEquals("Unstable bar count must be equal to the configured barCount",
                barCount, atr.getCountOfUnstableBars());
    }

    @Test
    public void testToStringContainsClassNameAndBarCount() {
        final int barCount = 2;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        String s = atr.toString();
        assertTrue("toString() should contain class simple name",
                s.contains(ATRIndicator.class.getSimpleName()));
        assertTrue("toString() should contain the barCount value",
                s.contains(String.valueOf(barCount)));
    }

    /**
     * Passing a {@code null} BarSeries to the public constructor must result in a
     * {@link NullPointerException}. The library does not define a custom exception,
     * so we verify the standard JDK behaviour.
     */
    @Test(expected = NullPointerException.class)
    public void testConstructorRejectsNullSeries() {
        new ATRIndicator((BarSeries) null, 10);
    }

    /**
     * A non‑positive barCount does not make sense for a moving‑average based indicator.
     * The underlying {@link MMAIndicator} validates this and throws an
     * {@link IllegalArgumentException}. The ATR constructor forwards the same exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNonPositiveBarCount() {
        new ATRIndicator(series, 0);
    }

    /**
     * Verify that the {@link #getTRIndicator()} accessor returns the exact instance
     * used internally (important for users that need the raw TR values).
     */
    @Test
    public void testGetTRIndicatorReturnsSameInstance() {
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, 3);
        assertSame("getTRIndicator() must return the TRIndicator passed to the constructor",
                tr, atr.getTRIndicator());
    }
}
