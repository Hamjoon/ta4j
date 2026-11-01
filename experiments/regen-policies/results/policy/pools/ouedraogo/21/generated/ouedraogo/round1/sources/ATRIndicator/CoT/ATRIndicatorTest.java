package org.ta4j.core.indicators;
import org.ta4j.core.indicators.helpers.TRIndicator;


import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link ATRIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>All public constructors</li>
 *   <li>Getter methods</li>
 *   <li>Basic functional behaviour (ATR calculation)</li>
 *   <li>Edge cases such as zero/negative barCount and out‑of‑range indexes</li>
 *   <li>String representation</li>
 * </ul>
 *
 * The test data uses a minimal {@link BarSeries} built with {@link BaseBarSeries}
 * and {@link BaseBar}.  All numeric values are created with {@link DecimalNum}
 * which implements {@link Num}.
 */
public class ATRIndicatorTest {

    private BarSeries series;
    private static final int BAR_COUNT = 3; // typical period for ATR

    /**
     * Creates a simple BarSeries with three bars.
     *
     * <pre>
     * Index 0 : close = 10, high = 12, low = 9
     * Index 1 : close = 11, high = 13, low = 10
     * Index 2 : close = 12, high = 14, low = 11
     * </pre>
     *
     * The series is sufficient for the true‑range and ATR calculations used
     * in the tests.
     */
    @Before
    public void setUp() {
        series = new BaseBarSeries("testSeries");

        // Bar 0 (first bar – no previous close, true range = high - low)
        Bar bar0 = new BaseBar(
                ZonedDateTime.now(),
                DecimalNum.valueOf(12), // high
                DecimalNum.valueOf(9),  // low
                DecimalNum.valueOf(10), // open (not used)
                DecimalNum.valueOf(10), // close
                DecimalNum.valueOf(0),  // volume (unused)
                DecimalNum.valueOf(0)   // amount (unused)
        );

        // Bar 1
        Bar bar1 = new BaseBar(
                ZonedDateTime.now().plusDays(1),
                DecimalNum.valueOf(13),
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(11),
                DecimalNum.valueOf(11),
                DecimalNum.valueOf(0),
                DecimalNum.valueOf(0)
        );

        // Bar 2
        Bar bar2 = new BaseBar(
                ZonedDateTime.now().plusDays(2),
                DecimalNum.valueOf(14),
                DecimalNum.valueOf(11),
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(0),
                DecimalNum.valueOf(0)
        );

        series.addBar(bar0);
        series.addBar(bar1);
        series.addBar(bar2);
    }

    /* --------------------------------------------------------------------- *
     *  Public constructors
     * --------------------------------------------------------------------- */

    @Test
    public void testConstructor_WithSeriesAndBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertNotNull("ATRIndicator should be instantiated", atr);
        assertEquals("Bar count must be stored correctly", BAR_COUNT, atr.getBarCount());
    }

    @Test
    public void testConstructor_WithTRIndicatorAndBarCount() {
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, BAR_COUNT);
        assertNotNull("ATRIndicator should be instantiated with TRIndicator", atr);
        assertEquals("Bar count must be stored correctly", BAR_COUNT, atr.getBarCount());
        assertSame("TRIndicator getter must return the same instance", tr, atr.getTRIndicator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NegativeBarCount_ShouldThrow() {
        // MMAIndicator (used internally) validates the period; a negative period
        // results in IllegalArgumentException.
        new ATRIndicator(series, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ZeroBarCount_ShouldThrow() {
        new ATRIndicator(series, 0);
    }

    /* --------------------------------------------------------------------- *
     *  Getter methods
     * --------------------------------------------------------------------- */

    @Test
    public void testGetTRIndicator() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        TRIndicator tr = atr.getTRIndicator();
        assertNotNull("TRIndicator must not be null", tr);
        // the TRIndicator should be based on the same series
        assertSame("TRIndicator should use the original series", series, tr.getBarSeries());
    }

    @Test
    public void testGetBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertEquals("getBarCount must return the period used in the constructor",
                BAR_COUNT, atr.getBarCount());
    }

    @Test
    public void testGetUnstableBars() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        // According to the implementation, unstable bars equal the bar count
        assertEquals("Unstable bars should be equal to the bar count",
                BAR_COUNT, atr.getUnstableBars());
    }

    @Test
    public void testToString() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        String s = atr.toString();
        assertTrue("toString must contain the class name", s.contains("ATRIndicator"));
        assertTrue("toString must contain the bar count", s.contains(String.valueOf(BAR_COUNT)));
    }

    /* --------------------------------------------------------------------- *
     *  Functional tests – ATR calculation
     * --------------------------------------------------------------------- */

    /**
     * Helper method that calculates the true range for a given index using the
     * same formula employed by {@link TRIndicator}.
     */
    private Num trueRange(int index) {
        Bar current = series.getBar(index);
        Num high = current.getHighPrice();
        Num low = current.getLowPrice();
        Num range = high.minus(low); // high - low

        if (index == 0) {
            return range;
        }

        Bar previous = series.getBar(index - 1);
        Num prevClose = previous.getClosePrice();

        Num highPrevClose = high.minus(prevClose).abs();
        Num lowPrevClose = low.minus(prevClose).abs();

        // max(high - low, |high - prevClose|, |low - prevClose|)
        return List.of(range, highPrevClose, lowPrevClose)
                .stream()
                .reduce((a, b) -> a.isGreaterThan(b) ? a : b)
                .orElse(range);
    }

    @Test
    public void testGetValue_CorrectATR_ComputedWithMMA() {
        // Use a small bar count to make the expected value easy to compute
        final int period = 2;
        ATRIndicator atr = new ATRIndicator(series, period);

        // Manually compute the first two true‑range values
        Num tr0 = trueRange(0); // first bar: high - low = 12 - 9 = 3
        Num tr1 = trueRange(1);
        // Expected ATR after two periods using the MMA (which for period=2
        // behaves like an exponential moving average with α = 1/2)
        // ATR(0) = TR(0)
        // ATR(1) = (ATR(0) * (period-1) + TR(1)) / period
        Num expectedAtr1 = tr0.multipliedBy(DecimalNum.valueOf(period - 1))
                .plus(tr1)
                .dividedBy(DecimalNum.valueOf(period));

        Num actualAtr1 = atr.getValue(1);
        assertEquals("ATR value at index 1 must match manual calculation",
                expectedAtr1.doubleValue(), actualAtr1.doubleValue(), 1e-8);
    }

    @Test
    public void testGetValue_IndexOutOfBounds_ShouldThrow() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        int outOfRangeIndex = series.getBarCount(); // one past the last index
        try {
            atr.getValue(outOfRangeIndex);
            fail("Expected IndexOutOfBoundsException for index " + outOfRangeIndex);
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    /* --------------------------------------------------------------------- *
     *  Edge‑case tests
     * --------------------------------------------------------------------- */

    @Test
    public void testGetValue_OnFirstBar_EqualsTrueRange() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        Num tr0 = trueRange(0);
        Num atr0 = atr.getValue(0);
        assertEquals("ATR on the first bar should be equal to the true range of that bar",
                tr0.doubleValue(), atr0.doubleValue(), 1e-8);
    }

    @Test
    public void testGetValue_WithSingleBarSeries() {
        // Build a series that contains only one bar
        BarSeries singleBarSeries = new BaseBarSeries("single");
        Bar onlyBar = new BaseBar(
                ZonedDateTime.now(),
                DecimalNum.valueOf(5),
                DecimalNum.valueOf(3),
                DecimalNum.valueOf(4),
                DecimalNum.valueOf(4),
                DecimalNum.valueOf(0),
                DecimalNum.valueOf(0)
        );
        singleBarSeries.addBar(onlyBar);

        ATRIndicator atr = new ATRIndicator(singleBarSeries, 1);
        Num expected = onlyBar.getHighPrice().minus(onlyBar.getLowPrice()); // 2
        Num actual = atr.getValue(0);
        assertEquals("ATR for a single‑bar series must equal its true range",
                expected.doubleValue(), actual.doubleValue(), 1e-8);
    }
}
