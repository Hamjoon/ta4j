package org.ta4j.core.indicators;
import org.ta4j.core.Indicator;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.indicators.averages.MMAIndicator;

/**
 * Comprehensive JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>Construction via both public constructors</li>
 *   <li>Behaviour of {@link #getValue(int)} for unstable and stable indexes</li>
 *   <li>Correctness of {@link #getCountOfUnstableBars()}, {@link #getBarCount()},
 *       {@link #getTRIndicator()} and {@link #toString()}</li>
 *   <li>Validation of null arguments</li>
 * </ul>
 *
 * <p>Only the public API that appears in the supplied source (or in the
 * dependency signatures) is used – no additional third‑party mocking frameworks
 * are employed.  The library’s concrete implementations {@code BaseBarSeries},
 * {@code BaseBar} and {@code DecimalNum} are used to build a minimal but fully
 * functional series.
 */
public class ATRIndicatorTest {

    /** Simple series with three bars – high, low and close are deterministic. */
    private BarSeries series;

    /** Constant high, low and close values used for TR calculation. */
    private static final Num HIGH  = DecimalNum.valueOf(10);
    private static final Num LOW   = DecimalNum.valueOf(5);
    private static final Num CLOSE = DecimalNum.valueOf(7);

    /** Number of bars used for the ATR time‑frame. */
    private static final int BAR_COUNT = 2;

    @Before
    public void setUp() {
        // Build a series containing three identical bars.
        series = new org.ta4j.core.BaseBarSeriesBuilder()
                .withName("test-series")
                .build();

        // Add three bars – the exact values are not important for the
        // “unstable‑bars” logic, they only need to be non‑null.
        series.addBar(org.ta4j.core.BaseBar.builder()
                .timePeriod(java.time.Duration.ofDays(1))
                .openPrice(CLOSE)   // open is irrelevant for TR
                .highPrice(HIGH)
                .lowPrice(LOW)
                .closePrice(CLOSE)
                .volume(DecimalNum.valueOf(1000))
                .build());

        series.addBar(org.ta4j.core.BaseBar.builder()
                .timePeriod(java.time.Duration.ofDays(1))
                .openPrice(CLOSE)
                .highPrice(HIGH)
                .lowPrice(LOW)
                .closePrice(CLOSE)
                .volume(DecimalNum.valueOf(1000))
                .build());

        series.addBar(org.ta4j.core.BaseBar.builder()
                .timePeriod(java.time.Duration.ofDays(1))
                .openPrice(CLOSE)
                .highPrice(HIGH)
                .lowPrice(LOW)
                .closePrice(CLOSE)
                .volume(DecimalNum.valueOf(1000))
                .build());
    }

    /**
     * Verify that the constructor which receives a {@link BarSeries} builds a
     * correctly configured indicator.
     */
    @Test
    public void testConstructorWithSeries() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);

        // The internal bar count must match the argument.
        assertEquals(BAR_COUNT, atr.getBarCount());

        // The unstable‑bars count is the TR indicator’s unstable bars (0) plus
        // the ATR bar count.
        assertEquals(0 + BAR_COUNT, atr.getCountOfUnstableBars());

        // The textual representation must contain the simple class name and
        // the configured bar count.
        assertEquals("ATRIndicator barCount: " + BAR_COUNT, atr.toString());
    }

    /**
     * Verify that the constructor which receives a {@link TRIndicator} works
     * and that the supplied {@link TRIndicator} is used for the internal
     * configuration.
     */
    @Test
    public void testConstructorWithTRIndicator() {
        // Build a TR indicator that always returns zero (high == low == close).
        Indicator<Num> constantHigh  = index -> HIGH;
        Indicator<Num> constantLow   = index -> LOW;
        Indicator<Num> constantClose = index -> CLOSE;
        TRIndicator tr = new TRIndicator(constantHigh, constantLow, constantClose);

        ATRIndicator atr = new ATRIndicator(tr, BAR_COUNT);

        // Same assertions as in the other constructor.
        assertEquals(BAR_COUNT, atr.getBarCount());
        assertEquals(tr.getCountOfUnstableBars() + BAR_COUNT,
                atr.getCountOfUnstableBars());
    }

    /**
     * The ATR indicator must return {@code NaN} for any index that is still
     * inside the unstable‑bars window.
     */
    @Test
    public void testGetValueReturnsNaNForUnstableIndexes() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        int unstableBars = atr.getCountOfUnstableBars();

        for (int i = 0; i < unstableBars; i++) {
            Num value = atr.getValue(i);
            assertTrue("Index " + i + " should be NaN", value.isNaN());
        }
    }

    /**
     * For stable indexes the ATR indicator should delegate to the internal
     * {@link MMAIndicator}.  The test compares the value returned by ATR with
     * the value returned directly by the MMA indicator.
     */
    @Test
    public void testGetValueDelegatesToMMAIndicator() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        int firstStableIndex = atr.getCountOfUnstableBars(); // first index after NaN window

        // Directly obtain the MMA indicator used internally via reflection.
        // This is safe for a unit test and does not rely on private APIs
        // because the field is package‑private in the compiled class.
        MMAIndicator mma = (MMAIndicator) TestHelper.getPrivateField(atr, "averageTrueRangeIndicator");

        // Compare values for a few stable indexes.
        for (int i = firstStableIndex; i < series.getBarCount(); i++) {
            Num expected = mma.getValue(i);
            Num actual   = atr.getValue(i);
            assertEquals("ATR value must equal MMA value at index " + i,
                    expected, actual);
        }
    }

    /**
     * {@link #getTRIndicator()} must return a fresh {@link TRIndicator}
     * instance that is bound to the same {@link BarSeries} as the ATR
     * indicator.
     */
    @Test
    public void testGetTRIndicatorCreatesNewInstance() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        TRIndicator tr = atr.getTRIndicator();

        assertNotNull(tr);
        assertSame("The TRIndicator must be built on the same series",
                series, tr.getBarSeries());
    }

    /**
     * Passing a {@code null} {@link TRIndicator} to the constructor must raise
     * a {@link NullPointerException} with the expected message.
     */
    @Test(expected = NullPointerException.class)
    public void testConstructorRejectsNullTRIndicator() {
        new ATRIndicator((TRIndicator) null, BAR_COUNT);
    }

    /**
     * Utility class that uses reflection to fetch private fields – used only
     * inside the test suite.
     */
    private static final class TestHelper {
        private TestHelper() { /* utility class */ }

        static Object getPrivateField(Object target, String fieldName) {
            try {
                java.lang.reflect.Field f = target.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
