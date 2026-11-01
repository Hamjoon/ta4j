package org.ta4j.core.indicators;
import org.ta4j.core.indicators.helpers.TRIndicator;


import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;

/**
 * JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>Construction via both public constructors</li>
 *   <li>Correct propagation of the {@code barCount}</li>
 *   <li>Behaviour of {@link #getUnstableBars()}, {@link #getTRIndicator()},
 *       {@link #getBarCount()} and {@link #toString()}</li>
 *   <li>Typical value calculation (ATR = MMA of TR)</li>
 *   <li>Edge case – an empty {@link BarSeries}</li>
 * </ul>
 *
 * <p>
 * Only the public API listed in the problem statement is used – no mocking,
 * no subclassing of library classes and no external libraries.
 */
public class ATRIndicatorTest {

    /** a tiny in‑memory {@link BarSeries} implementation used only for the tests */
    private static class SimpleBarSeries implements BarSeries {
        private final String name;
        private final List<Bar> bars = new ArrayList<>();
        private final Num zero;
        private final Num one;
        private final Num hundred;

        SimpleBarSeries(String name) {
            this.name = name;
            // use DecimalNum (the default Num implementation of ta4j)
            this.zero = DecimalNum.valueOf(0);
            this.one = DecimalNum.valueOf(1);
            this.hundred = DecimalNum.valueOf(100);
        }

        /** add a bar built from the supplied price values */
        void addBar(ZonedDateTime endTime,
                    Number open, Number high, Number low, Number close) {
            Bar bar = new SimpleBar(endTime, open, high, low, close);
            bars.add(bar);
        }

        // -----------------------------------------------------------------
        // BarSeries interface – only the methods required by the indicators
        // -----------------------------------------------------------------
        @Override public String getName() { return name; }

        @Override public Num num() { return zero; } // the concrete value is irrelevant;
                                                    // the indicators obtain a Num via
                                                    // BarSeries.num(), then create new
                                                    // instances through Num#plus etc.

        @Override public Bar getBar(int i) { return bars.get(i); }

        @Override public int getBarCount() { return bars.size(); }

        @Override public List<Bar> getBarData() { return bars; }

        @Override public int getBeginIndex() { return 0; }

        @Override public int getEndIndex() { return bars.isEmpty() ? -1 : bars.size() - 1; }

        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }

        @Override public void setMaximumBarCount(int maximumBarCount) { /* no‑op */ }

        @Override public int getRemovedBarsCount() { return 0; }

        @Override public BarSeries getSubSeries(int startIndex, int endIndex) {
            SimpleBarSeries sub = new SimpleBarSeries(name + "-sub");
            for (int i = startIndex; i <= endIndex && i < bars.size(); i++) {
                sub.bars.add(bars.get(i));
            }
            return sub;
        }

        // -----------------------------------------------------------------
        // Default methods of the interface are inherited – we do not need to
        // re‑implement them for the test suite.
        // -----------------------------------------------------------------
        @Override public Num zero() { return zero; }

        @Override public Num one() { return one; }

        @Override public Num hundred() { return hundred; }

        @Override public Num numOf(Number number) { return DecimalNum.valueOf(number); }

        @Override public void addBar(Bar bar) { bars.add(bar); }

        @Override public void addBar(Bar bar, boolean replace) {
            if (replace && !bars.isEmpty()) {
                bars.set(bars.size() - 1, bar);
            } else {
                bars.add(bar);
            }
        }

        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime) {
            // not needed for the tests
        }

        @Override public void addBar(ZonedDateTime endTime, Number openPrice,
                                    Number highPrice, Number lowPrice, Number closePrice) {
            addBar(endTime, openPrice, highPrice, lowPrice, closePrice);
        }

        @Override public void addBar(ZonedDateTime endTime, Number openPrice,
                                    Number highPrice, Number lowPrice, Number closePrice,
                                    Number volume) {
            addBar(endTime, openPrice, highPrice, lowPrice, closePrice);
        }

        @Override public void addBar(ZonedDateTime endTime, Number openPrice,
                                    Number highPrice, Number lowPrice, Number closePrice,
                                    Number volume, Number amount) {
            addBar(endTime, openPrice, highPrice, lowPrice, closePrice);
        }

        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                    Number openPrice, Number highPrice, Number lowPrice,
                                    Number closePrice, Number volume) {
            addBar(endTime, openPrice, highPrice, lowPrice, closePrice);
        }

        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                    Number openPrice, Number highPrice, Number lowPrice,
                                    Number closePrice, Number volume, Number amount) {
            addBar(endTime, openPrice, highPrice, lowPrice, closePrice);
        }

        @Override public void addBar(ZonedDateTime endTime, String openPrice,
                                    String highPrice, String lowPrice, String closePrice) {
            addBar(endTime,
                    new DecimalNum(openPrice),
                    new DecimalNum(highPrice),
                    new DecimalNum(lowPrice),
                    new DecimalNum(closePrice));
        }

        @Override public void addBar(ZonedDateTime endTime, String openPrice,
                                    String highPrice, String lowPrice, String closePrice,
                                    String volume) {
            addBar(endTime, openPrice, highPrice, lowPrice, closePrice);
        }

        @Override public void addBar(ZonedDateTime endTime, String openPrice,
                                    String highPrice, String lowPrice, String closePrice,
                                    String volume, String amount) {
            addBar(endTime, openPrice, highPrice, lowPrice, closePrice);
        }

        @Override public void addBar(ZonedDateTime endTime, Num openPrice,
                                    Num highPrice, Num lowPrice, Num closePrice,
                                    Num volume) {
            addBar(endTime, openPrice, highPrice, lowPrice, closePrice);
        }

        @Override public void addBar(ZonedDateTime endTime, Num openPrice,
                                    Num highPrice, Num lowPrice, Num closePrice,
                                    Num volume, Num amount) {
            addBar(endTime, openPrice, highPrice, lowPrice, closePrice);
        }

        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                    Num openPrice, Num highPrice, Num lowPrice,
                                    Num closePrice, Num volume) {
            addBar(endTime, openPrice, highPrice, lowPrice, closePrice);
        }

        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                    Num openPrice, Num highPrice, Num lowPrice,
                                    Num closePrice, Num volume, Num amount) {
            addBar(endTime, openPrice, highPrice, lowPrice, closePrice);
        }

        @Override public void addTrade(Number tradeVolume, Number tradePrice) { /* no‑op */ }

        @Override public void addTrade(String tradeVolume, String tradePrice) { /* no‑op */ }

        @Override public void addTrade(Num tradeVolume, Num tradePrice) { /* no‑op */ }

        @Override public void addPrice(Num price) { /* no‑op */ }

        @Override public void addPrice(String price) { /* no‑op */ }

        @Override public void addPrice(Number price) { /* no‑op */ }

        @Override public String getSeriesPeriodDescription() { return ""; }

        @Override public String getSeriesPeriodDescription() { return ""; }
    }

    /** a minimal {@link Bar} implementation – only the data required by TRIndicator */
    private static class SimpleBar implements Bar {
        private final ZonedDateTime endTime;
        private final Num open;
        private final Num high;
        private final Num low;
        private final Num close;

        SimpleBar(ZonedDateTime endTime,
                  Number open, Number high, Number low, Number close) {
            this.endTime = endTime;
            this.open   = DecimalNum.valueOf(open);
            this.high   = DecimalNum.valueOf(high);
            this.low    = DecimalNum.valueOf(low);
            this.close  = DecimalNum.valueOf(close);
        }

        @Override public ZonedDateTime getEndTime() { return endTime; }

        @Override public Num getOpenPrice() { return open; }

        @Override public Num getHighPrice() { return high; }

        @Override public Num getLowPrice() { return low; }

        @Override public Num getClosePrice() { return close; }

        // All remaining methods are not used by the indicators in this test suite.
        @Override public Num getTradeVolume() { return DecimalNum.valueOf(0); }

        @Override public Num getTradePrice() { return DecimalNum.valueOf(0); }

        @Override public Num getAmount() { return DecimalNum.valueOf(0); }

        @Override public Num getBidVolume() { return DecimalNum.valueOf(0); }

        @Override public Num getAskVolume() { return DecimalNum.valueOf(0); }

        @Override public Num getBidPrice() { return DecimalNum.valueOf(0); }

        @Override public Num getAskPrice() { return DecimalNum.valueOf(0); }

        @Override public Duration getTimePeriod() { return Duration.ZERO; }

        @Override public Bar getPreviousBar() { return null; }

        @Override public Bar getNextBar() { return null; }

        @Override public boolean isUpBar() { return close.isGreaterThan(open); }

        @Override public boolean isDownBar() { return close.isLessThan(open); }

        @Override public boolean isEqualBar() { return close.isEqual(open); }
    }

    private SimpleBarSeries series;
    private static final int BAR_COUNT = 3;

    @Before
    public void setUp() {
        series = new SimpleBarSeries("test-series");
        ZonedDateTime now = ZonedDateTime.now();

        // add 5 bars with deterministic values
        series.addBar(now.minusDays(5), 10, 12, 9, 11);
        series.addBar(now.minusDays(4), 11, 13, 10, 12);
        series.addBar(now.minusDays(3), 12, 14, 11, 13);
        series.addBar(now.minusDays(2), 13, 15, 12, 14);
        series.addBar(now.minusDays(1), 14, 16, 13, 15);
    }

    /** Test that both constructors build a functional indicator and expose the correct barCount. */
    @Test
    public void testConstructionAndBasicGetters() {
        // constructor with BarSeries
        ATRIndicator atr1 = new ATRIndicator(series, BAR_COUNT);
        assertNotNull("TRIndicator must not be null", atr1.getTRIndicator());
        assertEquals("Bar count must match the one supplied to the constructor",
                BAR_COUNT, atr1.getBarCount());
        assertEquals("Unstable bars must equal the bar count", BAR_COUNT, atr1.getUnstableBars());

        // constructor with explicit TRIndicator
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr2 = new ATRIndicator(tr, BAR_COUNT);
        assertSame("TRIndicator returned by getter must be the one supplied", tr, atr2.getTRIndicator());
        assertEquals("Bar count must be propagated", BAR_COUNT, atr2.getBarCount());
        assertEquals("Unstable bars must be equal to bar count", BAR_COUNT, atr2.getUnstableBars());
    }

    /** Verify that {@code toString()} contains the class name and the configured bar count. */
    @Test
    public void testToString() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        String s = atr.toString();
        assertTrue("toString must contain class name", s.contains("ATRIndicator"));
        assertTrue("toString must contain the bar count", s.contains("barCount: " + BAR_COUNT));
    }

    /**
     * Typical value test – compare the ATR value returned by the indicator with the
     * value produced directly by the underlying MMAIndicator (which is what ATRIndicator
     * delegates to).  This ensures that the delegation logic is correct.
     */
    @Test
    public void testGetValueMatchesUnderlyingMMA() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        // pick a few indices that are safely after the warm‑up period
        for (int i = BAR_COUNT; i < series.getBarCount(); i++) {
            Num atrValue = atr.getValue(i);
            // compute the expected value by using the MMAIndicator directly
            MMAIndicator expectedMMA = new MMAIndicator(new TRIndicator(series), BAR_COUNT);
            Num expected = expectedMMA.getValue(i);
            assertEquals("ATR value must equal MMA of TR at index " + i,
                    0, atrValue.compareTo(expected));
        }
    }

    /**
     * Edge case – an empty series.  The indicator should be constructible,
     * {@link #getUnstableBars()} and {@link #getBarCount()} must still return the
     * configured bar count, but any attempt to retrieve a value must fail
     * with an {@link IndexOutOfBoundsException}.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testEmptySeriesBehaviour() {
        SimpleBarSeries emptySeries = new SimpleBarSeries("empty");
        ATRIndicator atr = new ATRIndicator(emptySeries, 1);
        assertEquals("Bar count must be as supplied even for empty series", 1, atr.getBarCount());
        assertEquals("Unstable bars must be as supplied even for empty series", 1, atr.getUnstableBars());
        // this call must throw because there is no bar at index 0
        atr.getValue(0);
    }
}
