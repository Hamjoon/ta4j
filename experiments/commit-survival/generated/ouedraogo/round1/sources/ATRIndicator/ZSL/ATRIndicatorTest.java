package org.ta4j.core.indicators;
import org.ta4j.core.indicators.helpers.TRIndicator;


import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>Construction with both available constructors.</li>
 *   <li>Correct delegation to the internal {@link TRIndicator}.</li>
 *   <li>Correct behaviour of {@link #getBarCount()}, {@link #getUnstableBars()}
 *       and {@link #toString()}.</li>
 *   <li>Verification that the value returned by {@link #getValue(int)} is
 *       exactly the same as the value computed by an independent {@link MMAIndicator}
 *       built on the same {@link TRIndicator} and bar‑count.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The test uses the concrete {@link BaseBarSeries} and {@link BaseBar}
 * implementations that are part of the public TA‑4J API (they are not
 * mocked, they are real objects).  All interactions are performed through
 * the public constructors and methods listed in the supplied API
 * specifications.
 * </p>
 */
public class ATRIndicatorTest {

    /** Series used for all tests – 6 bars with deterministic prices. */
    private BarSeries series;

    /** Helper constants for readability. */
    private static final ZoneId ZONE = ZoneId.of("UTC");

    /**
     * Builds a {@link BaseBarSeries} containing six bars.
     *
     * <p>
     * Prices are chosen so that the true range can be calculated manually
     * and verified if needed:
     * <pre>
     *   Bar 0 : open=10, high=12, low=9,  close=11
     *   Bar 1 : open=11, high=13, low=10, close=12
     *   Bar 2 : open=12, high=15, low=11, close=14
     *   Bar 3 : open=14, high=16, low=13, close=15
     *   Bar 4 : open=15, high=18, low=14, close=17
     *   Bar 5 : open=17, high=19, low=16, close=18
     * </pre>
     * </p>
     */
    @Before
    public void setUp() {
        series = new BaseBarSeries("test-series");

        // Helper to create a bar with double values – the series supplies its own Num factory.
        Num[] opens =   {num(10), num(11), num(12), num(14), num(15), num(17)};
        Num[] highs =   {num(12), num(13), num(15), num(16), num(18), num(19)};
        Num[] lows =    {num(9),  num(10), num(11), num(13), num(14), num(16)};
        Num[] closes = {num(11), num(12), num(14), num(15), num(17), num(18)};

        for (int i = 0; i < opens.length; i++) {
            ZonedDateTime endTime = ZonedDateTime.now(ZONE).plusDays(i);
            // BaseBar constructor: duration, endTime, open, high, low, close, volume
            Bar bar = new BaseBar(Duration.ofDays(1), endTime,
                    opens[i], highs[i], lows[i], closes[i], series.numOf(0));
            series.addBar(bar);
        }
    }

    /**
     * Shortcut to obtain a {@link Num} from the series' Num factory.
     */
    private Num num(Number value) {
        return series.numOf(value);
    }

    /** --------------------------------------------------------------------- **/
    /** --------------------------- Constructor Tests ------------------------ **/
    /** --------------------------------------------------------------------- **/

    @Test
    public void testConstructorWithSeries() {
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(series, barCount);

        // The internal TRIndicator must not be null and must reference the same series.
        assertNotNull("TRIndicator should be created", atr.getTRIndicator());
        assertSame("TRIndicator must use the provided series",
                series, atr.getTRIndicator().getBarSeries());

        // Bar count must be stored correctly.
        assertEquals("Bar count must be stored", barCount, atr.getBarCount());
    }

    @Test
    public void testConstructorWithTRIndicator() {
        int barCount = 4;
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, barCount);

        // The ATRIndicator should expose the exact TRIndicator instance passed in.
        assertSame("getTRIndicator must return the same instance used in construction", tr, atr.getTRIndicator());

        // Bar count must be stored correctly.
        assertEquals("Bar count must be stored", barCount, atr.getBarCount());
    }

    /** --------------------------------------------------------------------- **/
    /** --------------------------- Simple Getters -------------------------- **/
    /** --------------------------------------------------------------------- **/

    @Test
    public void testGetUnstableBarsEqualsBarCount() {
        int barCount = 5;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        assertEquals("Unstable bars should be equal to bar count", barCount, atr.getUnstableBars());
    }

    @Test
    public void testToStringContainsClassNameAndBarCount() {
        int barCount = 2;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        String s = atr.toString();

        assertTrue("toString should contain class name", s.contains("ATRIndicator"));
        assertTrue("toString should contain the barCount value", s.contains(String.valueOf(barCount)));
    }

    /** --------------------------------------------------------------------- **/
    /** --------------------------- Value Delegation ------------------------ **/
    /** --------------------------------------------------------------------- **/

    @Test
    public void testGetValueDelegatesToMMAIndicator() {
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(series, barCount);

        // Build an independent MMAIndicator using the same TRIndicator and barCount.
        TRIndicator tr = new TRIndicator(series);
        MMAIndicator expectedMMA = new MMAIndicator(tr, barCount);

        // Compare each value returned by ATRIndicator with the expected MMAIndicator.
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            Num atrValue = atr.getValue(i);
            Num mmaValue = expectedMMA.getValue(i);
            assertEquals(
                    String.format("ATR value at index %d must match MMA value", i),
                    mmaValue, atrValue);
        }
    }

    /** --------------------------------------------------------------------- **/
    /** --------------------------- Edge Cases ------------------------------ **/
    /** --------------------------------------------------------------------- **/

    @Test
    public void testGetValueBeforeEnoughData() {
        // barCount larger than the number of bars forces the indicator to use
        // the first values of the underlying MMAIndicator (which are based on the
        // first TR values). The test ensures no exception is thrown and the
        // returned Num is not null.
        int barCount = 10; // larger than series size (6)
        ATRIndicator atr = new ATRIndicator(series, barCount);

        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            Num v = atr.getValue(i);
            assertNotNull("Value should never be null even when data is insufficient", v);
        }
    }

    /** --------------------------------------------------------------------- **/
    /** --------------------------- Helper Methods -------------------------- **/
    /** --------------------------------------------------------------------- **/

    /**
     * Utility method to create a {@link Num} from a primitive double using the
     * series' factory. It exists solely to keep the test data definition tidy.
     */
    private Num n(double value) {
        return series.numOf(value);
    }
}
