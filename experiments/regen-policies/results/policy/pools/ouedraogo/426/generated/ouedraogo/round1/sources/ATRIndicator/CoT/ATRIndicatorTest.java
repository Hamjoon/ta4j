package org.ta4j.core.indicators;

import static org.junit.Assert.*;
import static org.ta4j.core.num.NaN.NaN;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.num.Num;

/**
 * JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>All public constructors and methods.</li>
 *   <li>Normal behaviour (e.g. bar count handling, string representation).</li>
 *   <li>Edge cases such as {@code null} arguments that must raise {@link NullPointerException}.</li>
 *   <li>Unstable‑bars logic – the indicator must return {@code NaN} for indices that are
 *       still unstable.</li>
 * </ul>
 *
 * <p>Only the public API shown in the problem statement is used; no external
 * library classes are referenced.  A very small in‑memory {@link BarSeries}
 * implementation is provided solely for the purpose of constructing the
 * indicator – its methods are stubbed because the tests never need real price
 * data.
 */
public class ATRIndicatorTest {

    /** Minimal {@link BarSeries} implementation used only for construction. */
    private static class MinimalBarSeries implements BarSeries, Serializable {

        private static final long serialVersionUID = 1L;

        @Override public org.ta4j.core.num.NumFactory numFactory() { return null; }
        @Override public org.ta4j.core.BarBuilder barBuilder() { return null; }
        @Override public String getName() { return "minimal"; }
        @Override public org.ta4j.core.Bar getBar(int i) { return null; }
        @Override public int getBarCount() { return 0; }
        @Override public List<org.ta4j.core.Bar> getBarData() { return Collections.emptyList(); }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return -1; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) { }
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public void addBar(org.ta4j.core.Bar bar) { }
        @Override public void addBar(org.ta4j.core.Bar bar, boolean replace) { }
        @Override public void addTrade(Number tradeVolume, Number tradePrice) { }
        @Override public void addTrade(Num tradeVolume, Num tradePrice) { }
        @Override public void addPrice(Num price) { }
        @Override public void addPrice(Number price) { }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { return this; }
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public String getSeriesPeriodDescriptionInSystemTimeZone() { return ""; }
        @Override public String toString() { return "MinimalBarSeries"; }
    }

    /** Helper to create a fresh ATR indicator based on a minimal series. */
    private ATRIndicator createAtr(int barCount) {
        BarSeries series = new MinimalBarSeries();
        return new ATRIndicator(series, barCount);
    }

    /* --------------------------------------------------------------------- */
    /*  Public constructors                                                   */
    /* --------------------------------------------------------------------- */

    @Test
    public void testConstructorWithSeries() {
        BarSeries series = new MinimalBarSeries();
        int barCount = 5;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        assertNotNull("ATRIndicator should be created", atr);
        assertEquals("Bar count must be stored correctly", barCount, atr.getBarCount());
    }

    @Test
    public void testConstructorWithTRIndicator() {
        BarSeries series = new MinimalBarSeries();
        TRIndicator tr = new TRIndicator(series);
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(tr, barCount);
        assertNotNull("ATRIndicator should be created from TRIndicator", atr);
        assertEquals("Bar count must be stored correctly", barCount, atr.getBarCount());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullSeriesThrowsNPE() {
        new ATRIndicator(null, 4);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullTRThrowsNPE() {
        new ATRIndicator((TRIndicator) null, 4);
    }

    /* --------------------------------------------------------------------- */
    /*  getCountOfUnstableBars()                                              */
    /* --------------------------------------------------------------------- */

    @Test
    public void testGetCountOfUnstableBars() {
        int barCount = 7;
        ATRIndicator atr = createAtr(barCount);
        // TRIndicator’s unstable bars are 0 for a fresh series (default behaviour)
        int expected = barCount; // trueRangeUnstableBars (0) + barCount
        assertEquals("Unstable bars count must be barCount + TR unstable bars",
                     expected, atr.getCountOfUnstableBars());
    }

    /* --------------------------------------------------------------------- */
    /*  getValue(int) – unstable index returns NaN                           */
    /* --------------------------------------------------------------------- */

    @Test
    public void testGetValueReturnsNaNForUnstableIndex() {
        int barCount = 4;
        ATRIndicator atr = createAtr(barCount);
        // any index less than the unstable‑bars threshold must yield NaN
        for (int i = 0; i < barCount; i++) {
            Num value = atr.getValue(i);
            assertTrue("Value at unstable index " + i + " should be NaN", value.isNaN());
        }
    }

    /* --------------------------------------------------------------------- */
    /*  getTRIndicator() – returns a TRIndicator bound to the same series    */
    /* --------------------------------------------------------------------- */

    @Test
    public void testGetTRIndicator() {
        BarSeries series = new MinimalBarSeries();
        ATRIndicator atr = new ATRIndicator(series, 2);
        TRIndicator tr = atr.getTRIndicator();
        assertNotNull("TRIndicator should not be null", tr);
        // the returned TRIndicator must be linked to the same BarSeries instance
        assertSame("TRIndicator must be built on the same BarSeries as ATRIndicator",
                   series, tr.getBarSeries());
    }

    /* --------------------------------------------------------------------- */
    /*  getBarCount() – simple getter                                         */
    /* --------------------------------------------------------------------- */

    @Test
    public void testGetBarCount() {
        int barCount = 9;
        ATRIndicator atr = createAtr(barCount);
        assertEquals("getBarCount must return the constructor value", barCount, atr.getBarCount());
    }

    /* --------------------------------------------------------------------- */
    /*  toString() – human readable representation                           */
    /* --------------------------------------------------------------------- */

    @Test
    public void testToString() {
        int barCount = 6;
        ATRIndicator atr = createAtr(barCount);
        String expected = "ATRIndicator barCount: " + barCount;
        assertEquals("toString must match the expected format", expected, atr.toString());
    }
}
