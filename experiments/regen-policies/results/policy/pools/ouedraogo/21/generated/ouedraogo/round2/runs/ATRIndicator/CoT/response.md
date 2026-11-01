###Test START##
```java
package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;

/**
 * Unit tests for {@link ATRIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>Public constructors</li>
 *   <li>Public methods {@code getValue}, {@code getUnstableBars},
 *       {@code getTRIndicator}, {@code getBarCount}, {@code toString}</li>
 *   <li>Edge cases such as negative/zero barCount and out‑of‑range indexes</li>
 * </ul>
 *
 * The test suite builds a very small in‑memory {@link BarSeries} with three
 * dummy {@link Bar} objects.  The {@link TRIndicator} is instantiated with this
 * series, and the {@link ATRIndicator} is then created on top of it.
 *
 * All numeric values are created with {@link DecimalNum} – the default
 * implementation of {@link Num} shipped with ta4j.
 */
public class ATRIndicatorTest {

    /** Simple in‑memory series used for all tests */
    private BarSeries series;

    /** A TRIndicator based on the series – used to build the ATRIndicator */
    private TRIndicator trIndicator;

    /** The ATRIndicator under test */
    private ATRIndicator atr;

    /** Number of bars used for the moving‑average calculation */
    private static final int BAR_COUNT = 2;

    /**
     * Minimal {@link Bar} implementation needed for the tests.
     * Only the price‑related methods are required for {@link TRIndicator}.
     */
    private static class SimpleBar implements Bar {

        private final Num openPrice;
        private final Num highPrice;
        private final Num lowPrice;
        private final Num closePrice;
        private final Num volume;

        SimpleBar(Num open, Num high, Num low, Num close, Num volume) {
            this.openPrice = open;
            this.highPrice = high;
            this.lowPrice = low;
            this.closePrice = close;
            this.volume = volume;
        }

        @Override public Num getOpenPrice() { return openPrice; }
        @Override public Num getHighPrice() { return highPrice; }
        @Override public Num getLowPrice() { return lowPrice; }
        @Override public Num getClosePrice() { return closePrice; }
        @Override public Num getVolume() { return volume; }

        // The remaining Bar methods are not used by TRIndicator or ATRIndicator.
        @Override public int getBeginTime() { return 0; }
        @Override public int getEndTime() { return 0; }
        @Override public Num getBidVolume() { return DecimalNum.ZERO; }
        @Override public Num getAskVolume() { return DecimalNum.ZERO; }
        @Override public Num getBidOpenPrice() { return openPrice; }
        @Override public Num getAskOpenPrice() { return openPrice; }
        @Override public Num getBidClosePrice() { return closePrice; }
        @Override public Num getAskClosePrice() { return closePrice; }
        @Override public Num getBidHighPrice() { return highPrice; }
        @Override public Num getAskHighPrice() { return highPrice; }
        @Override public Num getBidLowPrice() { return lowPrice; }
        @Override public Num getAskLowPrice() { return lowPrice; }
        @Override public int getTrades() { return 0; }
        @Override public Num getTradeVolume() { return volume; }
        @Override public Num getTradePrice() { return closePrice; }
        @Override public Num getOpenBidPrice() { return openPrice; }
        @Override public Num getOpenAskPrice() { return openPrice; }
        @Override public Num getCloseBidPrice() { return closePrice; }
        @Override public Num getCloseAskPrice() { return closePrice; }
        @Override public Num getMidPrice() { return openPrice.plus(closePrice).dividedBy(DecimalNum.valueOf(2)); }
    }

    /**
     * Very small concrete {@link BarSeries} implementation.
     * Only the methods required by the tests are overridden.
     */
    private static class SimpleBarSeries implements BarSeries {

        private final List<Bar> bars = new ArrayList<>();

        SimpleBarSeries(List<Bar> initialBars) {
            bars.addAll(initialBars);
        }

        @Override public NumFactory numFactory() { return DecimalNum::valueOf; }

        @Override public BaseBarConvertibleBuilder barBuilder() {
            throw new UnsupportedOperationException("Not needed for these tests");
        }

        @Override public String getName() { return "simple-series"; }

        @Override public Bar getBar(int i) { return bars.get(i); }

        @Override public int getBarCount() { return bars.size(); }

        @Override public List<Bar> getBarData() { return Collections.unmodifiableList(bars); }

        @Override public int getBeginIndex() { return 0; }

        @Override public int getEndIndex() { return getBarCount() - 1; }

        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }

        @Override public void setMaximumBarCount(int maximumBarCount) { /* no‑op */ }

        @Override public int getRemovedBarsCount() { return 0; }

        @Override public void addBar(Bar bar) { bars.add(bar); }

        @Override public void addBar(Bar bar, boolean replace) { bars.add(bar); }

        @Override public void addTrade(Number tradeVolume, Number tradePrice) { /* no‑op */ }

        @Override public void addTrade(Num tradeVolume, Num tradePrice) { /* no‑op */ }

        @Override public void addPrice(Num price) { /* no‑op */ }

        @Override public void addPrice(Number price) { /* no‑op */ }

        @Override public BarSeries getSubSeries(int startIndex, int endIndex) {
            return new SimpleBarSeries(bars.subList(startIndex, endIndex + 1));
        }

        @Override public String getSeriesPeriodDescription() { return "N/A"; }
    }

    /**
     * Helper method that creates a series with three bars having deterministic
     * price data.  The values are chosen so that the true‑range calculation
     * yields known numbers (see {@link TRIndicator} implementation in ta4j).
     */
    private static BarSeries createTestSeries() {
        List<Bar> bars = new ArrayList<>();
        // Bar 0
        bars.add(new SimpleBar(
                DecimalNum.valueOf(10),   // open
                DecimalNum.valueOf(12),   // high
                DecimalNum.valueOf(9),    // low
                DecimalNum.valueOf(11),    // close
                DecimalNum.ONE));         // volume
        // Bar 1
        bars.add(new SimpleBar(
                DecimalNum.valueOf(11),
                DecimalNum.valueOf(13),
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(12),
                DecimalNum.ONE));
        // Bar 2
        bars.add(new SimpleBar(
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(14),
                DecimalNum.valueOf(11),
                DecimalNum.valueOf(13),
                DecimalNum.ONE));
        return new SimpleBarSeries(bars);
    }

    @Before
    public void setUp() {
        series = createTestSeries();
        trIndicator = new TRIndicator(series);
        atr = new ATRIndicator(trIndicator, BAR_COUNT);
    }

    /*** --------------------------------------------------------------------
     *  PUBLIC METHOD LIST (extracted from ATRIndicator)
     *  -------------------------------------------------------------------- */
    // public ATRIndicator(BarSeries series, int barCount)
    // public ATRIndicator(TRIndicator tr, int barCount)
    // public Num getValue(int index)
    // public int getUnstableBars()
    // public TRIndicator getTRIndicator()
    // public int getBarCount()
    // public String toString()
    /*** -------------------------------------------------------------------- */

    /*** --------------------------------------------------------------------
     *  BASIC FUNCTIONALITY TESTS
     *  -------------------------------------------------------------------- */

    @Test
    public void testConstructor_WithSeries() {
        ATRIndicator indicator = new ATRIndicator(series, BAR_COUNT);
        assertNotNull(indicator);
        assertEquals(BAR_COUNT, indicator.getBarCount());
        assertSame(series, indicator.getBarSeries());
    }

    @Test
    public void testConstructor_WithTRIndicator() {
        ATRIndicator indicator = new ATRIndicator(trIndicator, BAR_COUNT);
        assertNotNull(indicator);
        assertEquals(BAR_COUNT, indicator.getBarCount());
        assertSame(trIndicator, indicator.getTRIndicator());
    }

    @Test
    public void testGetValue_DelegatesToMMAIndicator() {
        // With the deterministic data above, the first true‑range values are:
        // TR(0) = high - low = 12 - 9 = 3
        // TR(1) = max(13-10, |13-11|, |10-11|) = 3
        // TR(2) = max(14-11, |14-12|, |11-12|) = 3
        // MMA (simple moving average with BAR_COUNT=2) will give:
        // index 0 -> 3 (first value)
        // index 1 -> (3+3)/2 = 3
        // index 2 -> (3+3)/2 = 3
        Num v0 = atr.getValue(0);
        Num v1 = atr.getValue(1);
        Num v2 = atr.getValue(2);
        assertEquals(DecimalNum.valueOf(3), v0);
        assertEquals(DecimalNum.valueOf(3), v1);
        assertEquals(DecimalNum.valueOf(3), v2);
    }

    @Test
    public void testGetUnstableBars_EqualsBarCount() {
        assertEquals(BAR_COUNT, atr.getUnstableBars());
    }

    @Test
    public void testGetTRIndicator_ReturnsSameInstance() {
        assertSame(trIndicator, atr.getTRIndicator());
    }

    @Test
    public void testGetBarCount_ReturnsMMABarCount() {
        assertEquals(BAR_COUNT, atr.getBarCount());
    }

    @Test
    public void testToString_ContainsClassNameAndBarCount() {
        String s = atr.toString();
        assertTrue(s.contains("ATRIndicator"));
        assertTrue(s.contains("barCount: " + BAR_COUNT));
    }

    /*** --------------------------------------------------------------------
     *  EDGE CASES & EXCEPTION TESTS
     *  -------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NegativeBarCount_Throws() {
        // MMAIndicator validates the barCount and throws IllegalArgumentException
        // when it is less than 1.
        new ATRIndicator(trIndicator, -5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ZeroBarCount_Throws() {
        new ATRIndicator(trIndicator, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_NullSeries_Throws() {
        // The first constructor forwards to the second one, which will try to
        // create a TRIndicator with a null series – this results in NPE.
        new ATRIndicator((BarSeries) null, BAR_COUNT);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_NullTRIndicator_Throws() {
        new ATRIndicator((TRIndicator) null, BAR_COUNT);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_IndexOutOfRange_Throws() {
        // The series has only 3 bars (indices 0‑2). Asking for index 10 must fail.
        atr.getValue(10);
    }

    @Test
    public void testGetValue_FirstIndexWhenBarCountGreaterThanSeriesSize() {
        // Create an ATR with a barCount larger than the series length.
        ATRIndicator largeBarCountAtr = new ATRIndicator(trIndicator, 10);
        // The MMAIndicator implementation returns the first TR value for any
        // index when there are not enough previous values.
        Num first = largeBarCountAtr.getValue(0);
        Num second = largeBarCountAtr.getValue(1);
        assertEquals(first, second);
    }
}
```
###Test END##